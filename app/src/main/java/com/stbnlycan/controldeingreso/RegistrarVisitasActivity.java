package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.fragments.BusquedaCiDialogFragment;
import com.stbnlycan.models.Accion;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistrarVisitasActivity extends AppCompatActivity implements RecintoAdapter.OnEventoListener, BusquedaCiDialogFragment.OnBusquedaCiListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_visitas);

        setTitle("Registrar Visita");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        List<Accion> cards = new ArrayList<>();
        cards.add(new Accion(0, "Escanear QR", R.drawable.icono_scan_qr));
        cards.add(new Accion(1, "Buscar CI", R.drawable.icono_carnet));

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
                //Toast.makeText(this,  "ID Participante "+result.getContents().toString(), Toast.LENGTH_LONG).show();
                //dialogLoading.show();
                //ciExiste(session_key, sid, result.getContents().toString());
                buscarXQR(result.getContents().toString());
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void buscarXQR(String llave) {
        String url = "http://172.16.0.22:8080/ingresoVisitantes/visitante/buscarXQR?llave="+llave;
        JsonObjectRequest jsonObjectRequest  = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(getApplicationContext(), "hola "+response.toString(), Toast.LENGTH_LONG).show();
                        try {
                            JSONObject jsonObject = new JSONObject(response.toString());
                            Visitante visitante = new Visitante();
                            visitante.setVteCi(jsonObject.getString("vteCi"));
                            visitante.setVteCorreo(jsonObject.getString("vteCorreo"));
                            visitante.setVteImagen(jsonObject.getString("vteImagen"));
                            visitante.setVteNombre(jsonObject.getString("vteNombre"));
                            visitante.setVteApellidos(jsonObject.getString("vteApellidos"));
                            visitante.setVteTelefono(jsonObject.getString("vteTelefono"));
                            visitante.setVteDireccion(jsonObject.getString("vteDireccion"));
                            visitante.setVteEstado(jsonObject.getString("vteEstado"));
                            visitante.setVteLlave(jsonObject.getString("vteLlave"));
                            visitante.setVteFecha(jsonObject.getString("vteFecha"));
                            JSONObject jsonObjectTV = new JSONObject(jsonObject.getString("tipoVisitante"));
                            TipoVisitante tipoVisitante = new TipoVisitante();
                            tipoVisitante.setTviCod(jsonObjectTV.getString("tviCod"));
                            tipoVisitante.setTviNombre(jsonObjectTV.getString("tviNombre"));
                            tipoVisitante.setTviDescripcion(jsonObjectTV.getString("tviDescripcion"));
                            tipoVisitante.setHorEstado(jsonObjectTV.getString("horEstado"));
                            visitante.setTipoVisitante(tipoVisitante);
                            JSONObject jsonObjectE = new JSONObject(jsonObject.getString("empresa"));
                            Empresa empresa = new Empresa();
                            empresa.setEmpCod(jsonObjectE.getString("empCod"));
                            empresa.setEmpNombre(jsonObjectE.getString("empNombre"));
                            empresa.setEmpObs(jsonObjectE.getString("empObs"));
                            visitante.setEmpresa(empresa);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getApplicationContext(), "error "+error, Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onBusquedaCiListener(String ci) {
        Log.d("result", ""+ci);
        Toast.makeText(getApplicationContext(),  "ID Participante "+ci, Toast.LENGTH_LONG).show();

        //Si existe, iniciamos activity de detalles del asistente
        if(ci.equals("1"))
        {
            Intent intent = new Intent(RegistrarVisitasActivity.this, RegistraVisitaActivity.class);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
            //finish();
            //showRegistrarDialog();
        }
        else if(ci.equals("2"))
        {
            Intent intent = new Intent(RegistrarVisitasActivity.this, NuevoVisitanteActivity.class);
            startActivity(intent);
            //finish();
        }
    }

}
