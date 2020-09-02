package com.stbnlycan.controldeingreso;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.fragments.BusquedaCiDialogFragment;
import com.stbnlycan.fragments.DFIngreso;
import com.stbnlycan.fragments.DFSalida;
import com.stbnlycan.interfaces.BuscarXQRAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaXCiAPIs;
import com.stbnlycan.models.Accion;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Error;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.Rol;
import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class RecintoActivity extends AppCompatActivity implements RecintoAdapter.OnEventoListener, BusquedaCiDialogFragment.OnBusquedaCiListener {

    private Toolbar toolbar;
    private Recinto recintoRecibido;
    private BusquedaCiDialogFragment dialogFragment;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean doubleBackToExitPressedOnce;
    private final static int REQUEST_CODE_RV = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recinto);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        setTitle(recintoRecibido.getRecNombre());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/
        doubleBackToExitPressedOnce = false;

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

        List<Accion> cards = new ArrayList<>();
        cards.add(new Accion(0, "ESCANEAR QR", R.drawable.icono_scan_qr));
        cards.add(new Accion(1, "BUSCAR CI", R.drawable.icono_carnet));
        cards.add(new Accion(2, "VISITAS", R.drawable.icono_visita));
        cards.add(new Accion(3, "VISITANTES", R.drawable.icono_visitantes));
        cards.add(new Accion(4, "HORARIOS", R.drawable.icono_horario));
        cards.add(new Accion(5, "EMPRESAS", R.drawable.icono_empresa));
        if(!rol.equals("USER"))
        {
            cards.add(new Accion(6, "USUARIOS", R.drawable.icono_usuarios));
        }
        //cards.add(new Accion(6, "USUARIOS", R.drawable.icono_usuarios));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecintoAdapter adapter = new RecintoAdapter(cards);
        adapter.setOnEventoClickListener(RecintoActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEventoDetailsClick(int position) {
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
            iniciarAVisitas();
        }
        else if(position == 3)
        {
            Intent intent = new Intent(RecintoActivity.this, Visitantes.class);
            startActivity(intent);
        }
        else if(position == 4)
        {
            Intent intent = new Intent(RecintoActivity.this, Horarios.class);
            intent.putExtra("recinto", recintoRecibido);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 5)
        {
            iniciarAEmpresas();
        }
        else if(position == 6)
        {
            iniciarAUsuarios();
        }
    }

    public void iniciarAVisitas()
    {
        Intent intent = new Intent(RecintoActivity.this, Visitas.class);
        intent.putExtra("recinto", recintoRecibido);
        startActivity(intent);
    }

    public void iniciarAEmpresas()
    {
        Intent intent = new Intent(RecintoActivity.this, Empresas.class);
        startActivity(intent);
    }

    public void iniciarAUsuarios()
    {
        Intent intent = new Intent(RecintoActivity.this, Usuarios.class);
        intent.putExtra("recinto", recintoRecibido);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recinto_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /*case android.R.id.home:
                finish();
                return false;*/
            case R.id.action_salir:
                cerrarSesion();
                Intent intent = new Intent(RecintoActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        LogoutAPIs logoutAPIs = retrofit.create(LogoutAPIs.class);
        Call<Void> call = logoutAPIs.logout(pref.getString("access_token", null));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call <Void> call, retrofit2.Response<Void> response) {
                editor.putString("access_token", "");
                editor.putString("token_type", "");
                editor.putString("rol", "");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Sesión finalizada", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
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
                registrarSalida(result.getContents());
            }
        }
        /*else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }*/

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_RV) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Visita visitaResult = (Visita) b.getSerializable("visitaResult");
                    showDFIngreso(visitaResult);
                }
            }
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }

    public void showCiDialog() {
        dialogFragment = new BusquedaCiDialogFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putBoolean("notAlertDialog", true);
        bundle.putSerializable("recinto", recintoRecibido);
        dialogFragment.setArguments(bundle);
        dialogFragment.setOnEventoClickListener(RecintoActivity.this);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }

    public void showDFIngreso(Visita visita) {
        DFIngreso dfIngreso = new DFIngreso();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putBoolean("notAlertDialog", true);
        bundle.putSerializable("visita", visita);
        dfIngreso.setArguments(bundle);
        //dialogFragment.setOnEventoClickListener(RecintoActivity.this);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogAdvertenciaI");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dfIngreso.show(ft, "dialogAdvertenciaI");
    }

    public void showDFSalida(Visita visita) {
        DFSalida dfSalida = new DFSalida();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putBoolean("notAlertDialog", true);
        bundle.putSerializable("visita", visita);
        dfSalida.setArguments(bundle);
        //dialogFragment.setOnEventoClickListener(RecintoActivity.this);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogAdvertencia");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dfSalida.show(ft, "dialogAdvertencia");
    }

    @Override
    public void onBusquedaCiListener(Visitante visitante) {
        registrarSalidaXCi(visitante);
    }

    private void registrarSalida(final String llave) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarSalidaAPIs registrarSalidaAPIs = retrofit.create(RegistrarSalidaAPIs.class);
        Call<JsonObject> call = registrarSalidaAPIs.registrarSalida(recintoRecibido.getRecCod(), llave, authorization);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                String jsonString = response.body().toString();
                if (jsonString.contains("visCod")) {
                    Visita visitaRecibida = new Gson().fromJson(jsonString, Visita.class);
                    //Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha salido de " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                    showDFSalida(visitaRecibida);
                } else {
                    Error error = new Gson().fromJson(jsonString, Error.class);
                    Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_LONG).show();
                    buscarXQR(llave);
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });
    }

    private void registrarSalidaXCi(final Visitante visitante) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarSalidaXCiAPIs registrarSalidaXCiAPIs = retrofit.create(RegistrarSalidaXCiAPIs.class);
        Call<JsonObject> call = registrarSalidaXCiAPIs.registrarSalidaXCi(recintoRecibido.getRecCod(), visitante.getVteCi(), authorization);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                String jsonString = response.body().toString();
                if (jsonString.contains("visCod")) {
                    Visita visitaRecibida = new Gson().fromJson(jsonString, Visita.class);
                    //Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha salido de " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();

                    dialogFragment.dismiss();
                    showDFSalida(visitaRecibida);
                } else {
                    Error error = new Gson().fromJson(jsonString, Error.class);
                    Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_LONG).show();
                    iniciarRVActivity(visitante);
                    dialogFragment.dismiss();
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });
    }

    private void buscarXQR(String llave) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        BuscarXQRAPIs buscarXQRAPIs = retrofit.create(BuscarXQRAPIs.class);
        Call<Visitante> call = buscarXQRAPIs.buscarXQR(llave, authorization);
        call.enqueue(new Callback<Visitante>() {
            @Override
            public void onResponse(Call <Visitante> call, retrofit2.Response<Visitante> response) {
                Visitante visitanteRecibido = response.body();
                if(visitanteRecibido.getVteCi() != null)
                {
                    Toast.makeText(getApplicationContext(), "Se encontró el visitante", Toast.LENGTH_LONG).show();
                    iniciarRVActivity(visitanteRecibido);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No se encontró el visitante en la base de datos", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call <Visitante> call, Throwable t) {

            }
        });
    }

    public void iniciarRVActivity(Visitante visitanteRecibido)
    {
        Intent intent = new Intent(RecintoActivity.this, RegistraVisitaActivity.class);
        intent.putExtra("visitante", visitanteRecibido);
        intent.putExtra("recinto", recintoRecibido);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_RV);
    }

    @Override
    public void onBackPressed() {
        if(doubleBackToExitPressedOnce)
        {
            this.finishAffinity();
        }
        else
        {
            Toast.makeText(this, "Presiona el botón Atrás nuevamente para salir de la aplicación", Toast.LENGTH_LONG).show();
        }
        doubleBackToExitPressedOnce = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

}
