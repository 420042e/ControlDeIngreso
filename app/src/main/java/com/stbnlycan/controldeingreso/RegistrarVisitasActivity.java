package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.fragments.BusquedaCiDialogFragment;
import com.stbnlycan.interfaces.BuscarXCIAPIs;
import com.stbnlycan.interfaces.BuscarXQRAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaAPIs;
import com.stbnlycan.models.Accion;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class RegistrarVisitasActivity extends AppCompatActivity implements RecintoAdapter.OnEventoListener, BusquedaCiDialogFragment.OnBusquedaCiListener{

    //private String recCod;
    private Toolbar toolbar;
    private Recinto recintoRecibido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_visitas);

        setTitle("Visitas");

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");
        //recCod = getIntent().getStringExtra("recCod");

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<Accion> cards = new ArrayList<>();
        cards.add(new Accion(0, "Escanear QR", R.drawable.icono_scan_qr));
        cards.add(new Accion(1, "Buscar CI", R.drawable.icono_carnet));
        cards.add(new Accion(2, "Visitantes Con Salidas", R.drawable.pendientes));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecintoAdapter adapter = new RecintoAdapter(cards);
        adapter.setOnEventoClickListener(RegistrarVisitasActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEventoDetailsClick(int position) {
        Log.d("EventoDetails",""+position);
        if(position == 0)
        {
            escaner();
        }
        else if(position == 1)
        {
            showCiDialog();
        }
        else if(position == 2)
        {
            iniciarAVCSalida();
        }
    }

    public void iniciarAVCSalida()
    {
        Intent intent = new Intent(RegistrarVisitasActivity.this, VCSalidaActivity.class);
        intent.putExtra("recinto", recintoRecibido);
        startActivity(intent);
    }

    //Metodo para escanear
    public void escaner()
    {
        IntentIntegrator intent = new IntentIntegrator( this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);

        intent.setPrompt("Registrando Ingreso");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);
        intent.initiateScan();
    }

    public void showCiDialog() {

        final BusquedaCiDialogFragment dialogFragment = new BusquedaCiDialogFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putBoolean("notAlertDialog", true);
        dialogFragment.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        dialogFragment.setOnEventoClickListener(RegistrarVisitasActivity.this);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            if(result.getContents() == null)
            {
                Toast.makeText(this,  "Cancelaste el escaneo de ingreso", Toast.LENGTH_LONG).show();
            }
            else
            {
                //Toast.makeText(this,  "ID Participante "+result.getContents(), Toast.LENGTH_LONG).show();
                buscarXQR(result.getContents());
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void buscarXQR(String llave) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        BuscarXQRAPIs buscarXQRAPIs = retrofit.create(BuscarXQRAPIs.class);
        Call<Visitante> call = buscarXQRAPIs.buscarXQR(llave);
        call.enqueue(new Callback<Visitante>() {
            @Override
            public void onResponse(Call <Visitante> call, retrofit2.Response<Visitante> response) {
                Visitante visitanteRecibido = response.body();
                if(visitanteRecibido.getVteCi() != null)
                {
                    Toast.makeText(getApplicationContext(), "Se encontró el visitante", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrarVisitasActivity.this, RegistraVisitaActivity.class);
                    intent.putExtra("visitante", visitanteRecibido);
                    intent.putExtra("recinto", recintoRecibido);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No se encontró el visitante", Toast.LENGTH_SHORT).show();
                }
                /*Toast.makeText(getApplicationContext(), "Se encontró el visitante", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegistrarVisitasActivity.this, RegistraVisitaActivity.class);
                intent.putExtra("Visitante", visitanteRecibido);
                intent.putExtra("recCod", recCod);
                startActivity(intent);*/
            }
            @Override
            public void onFailure(Call <Visitante> call, Throwable t) {

            }
        });
    }

    private void buscarXCI(String ci) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        BuscarXCIAPIs buscarXCIAPIs = retrofit.create(BuscarXCIAPIs.class);
        Call<Visitante> call = buscarXCIAPIs.buscarXQR(ci);
        call.enqueue(new Callback<Visitante>() {
            @Override
            public void onResponse(Call <Visitante> call, retrofit2.Response<Visitante> response) {
                Visitante visitanteRecibido = response.body();
                if(visitanteRecibido.getVteCi() != null)
                {
                    Toast.makeText(getApplicationContext(), "Se encontró el visitante", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrarVisitasActivity.this, RegistraVisitaActivity.class);
                    intent.putExtra("visitante", visitanteRecibido);
                    intent.putExtra("recinto", recintoRecibido);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No se encontró el visitante", Toast.LENGTH_SHORT).show();
                }
                /*Toast.makeText(getApplicationContext(), "Se encontró el visitante", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegistrarVisitasActivity.this, RegistraVisitaActivity.class);
                intent.putExtra("Visitante", visitanteRecibido);
                intent.putExtra("recCod", recCod);
                startActivity(intent);*/
            }
            @Override
            public void onFailure(Call <Visitante> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBusquedaCiListener(String ci) {
        buscarXCI(ci);
    }

}
