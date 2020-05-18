package com.stbnlycan.controldeingreso;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.stbnlycan.models.AreaR;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RegistraVisitaActivity extends AppCompatActivity {

    ArrayList<AreaR> areaR;
    ArrayAdapter<AreaR> adapterAreaR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_visita);

        setTitle("Confirmar Visita");

        iniciarSpinnerArea();
        getDataAreaR(getIntent().getStringExtra("recCod"));
    }

    public void iniciarSpinnerArea() {
        Spinner spinner = findViewById(R.id.area_recinto);
        areaR = new ArrayList<>();

        AreaR area = new AreaR();
        area.setAreaCod("cod");
        area.setAreaNombre("Selecciona area del recinto");
        area.setAreaDescripcion("descripcion");
        area.setAreaEstado("estado");

        areaR.add(area);
        adapterAreaR = new ArrayAdapter<AreaR>(this, android.R.layout.simple_spinner_dropdown_item, areaR);
        adapterAreaR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterAreaR);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AreaR areaR = (AreaR) parent.getSelectedItem();
                displayAreaRData(areaR);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void getDataAreaR(String recCod) {
        String url = "http://172.16.0.22:8080/ingresoVisitantes/areaRecinto/listaPorRecinto?recCod="+recCod;
        JsonArrayRequest jsonArrayRequest  = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), "hola "+response.toString(), Toast.LENGTH_LONG).show();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                AreaR area = new AreaR();
                                area.setAreaCod(jsonObject.getString("areaCod"));
                                area.setAreaNombre(jsonObject.getString("areaNombre"));
                                area.setAreaDescripcion(jsonObject.getString("areaDescripcion"));
                                area.setAreaEstado(jsonObject.getString("areaEstado"));

                                areaR.add(area);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //adapterEmpresa.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getApplicationContext(), "error "+error, Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    private void displayAreaRData(AreaR areaR) {
        String cod = areaR.getAreaCod();
        String nombre = areaR.getAreaNombre();
        String descripcion = areaR.getAreaDescripcion();
        String estado = areaR.getAreaEstado();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + descripcion + "\nEstado: " + estado;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    public void registrarVisita(View view) {
        JSONObject registroIngreso = new JSONObject();
        try {
            JSONObject visitante = new JSONObject();
            visitante.put("vteCi", "6580366");
            visitante.put("vteCorreo", "live626@gmail.com");
            visitante.put("vteImagen", "logoAlbo_07-05-2020_13_39_23_784.png");
            visitante.put("vteNombre", "Daniel");
            visitante.put("vteApellidos", "Romero Velasco");
            visitante.put("vteTelefono", "68384383");
            visitante.put("vteDireccion", "Calle 17");
            visitante.put("vteEstado", "ACT");
            visitante.put("vteLlave", "7f66c2927bab25eaa6e6c450eae5d267d87ecadba0f44e92fbd0fdd941779ca4503bc58468b4de9cb80f8c7872a99e141c991edda701e139428b539e92b2e1d3");
            visitante.put("vteFecha", "2020-05-07T11:42:49.636");
            JSONObject tipoVisitante = new JSONObject();
            tipoVisitante.put("tviCod", 2);
            tipoVisitante.put("tviNombre", "TRAMITADOR");
            tipoVisitante.put("tviDescripcion", "TRAMITADORES");
            tipoVisitante.put("horEstado", "ACT");
            JSONObject empresa = new JSONObject();
            empresa.put("empCod", 1);
            empresa.put("empNombre", "CHASQUI");
            empresa.put("empObs", "EMPRESA DE TRANSPORTE CHASQUI");
            visitante.put("tipoVisitante", tipoVisitante);
            visitante.put("empresa", empresa);
            JSONObject areaRecinto = new JSONObject();
            areaRecinto.put("areaCod", 2);
            areaRecinto.put("areaNombre", "ALMACEN 2");
            areaRecinto.put("areaDescripcion", "ALMACEN 2 DONDE SE GUARDA X COSAS");
            areaRecinto.put("areaEstado", "ACT");
            JSONObject recinto = new JSONObject();
            recinto.put("recCod", "YAC01");
            recinto.put("recNombre", "PAJOSO - YACUIBA");
            recinto.put("recNombrea", "FRONTERA YACUIBA");
            recinto.put("recEstado", "ACT");
            recinto.put("recTipo", "FRONTERA");
            JSONObject aduana = new JSONObject();
            aduana.put("aduCod", 621);
            aduana.put("aduNombre", "FRONTERA YACUIBA");
            aduana.put("aduPais", "BOLIVIA");
            aduana.put("aduEstado", "ACT");
            recinto.put("aduana", aduana);
            areaRecinto.put("recinto", recinto);
            registroIngreso.put("visObs", "La persona ingresa con un maletin negro de cuero");
            registroIngreso.put("visitante", visitante);
            registroIngreso.put("areaRecinto", areaRecinto);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("ob",""+registroIngreso.toString());

        String url = "http://172.16.0.22:8080/ingresoVisitantes/visita/registrarIngreso";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, registroIngreso,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("msg", response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Toast.makeText(getApplicationContext(), "error " + error, Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

}
