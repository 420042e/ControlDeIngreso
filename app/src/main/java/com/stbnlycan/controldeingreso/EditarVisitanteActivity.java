package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.stbnlycan.adapters.EmpresaAdapter;
import com.stbnlycan.models.AreaR;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditarVisitanteActivity extends AppCompatActivity {

    private ImageView mimageView;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private List<Recinto> areas;
    ArrayList<Empresa> empresas;
    ArrayList<TipoVisitante> tiposVisitante;

    ArrayAdapter<Empresa> adapterEmpresa;
    ArrayAdapter<TipoVisitante> adapterTipoVisitante;

    private ImageView fotoIV;
    private EditText ciET;
    private EditText nombreET;
    private EditText apellidosET;
    private EditText telcelET;
    private EditText emailET;
    private EditText direccionET;
    private Spinner empresaS;
    private Spinner tipoVisitanteS;

    private Visitante visitanteRecibido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_visitante);

        setTitle("Editar visitante");
        fotoIV = findViewById(R.id.fotoVisitante);
        ciET = findViewById(R.id.ci);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        telcelET = findViewById(R.id.telcel);
        emailET = findViewById(R.id.email);
        direccionET = findViewById(R.id.direccion);
        empresaS = findViewById(R.id.empresa);
        tipoVisitanteS = findViewById(R.id.tipo_visitante);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        iniciarSpinnerEmpresa();
        iniciarSpinnerTipoVisitante();

        visitanteRecibido = (Visitante) getIntent().getSerializableExtra("Visitante");
        //Log.d("msgV", ""+visitanteRecibido.getVteNombre());

        getDataEmpresa();
        getDataTipoVisitante();

        Picasso.get().load("http://dineroclub.net/wp-content/uploads/2019/11/DEVELOPER3-696x465.jpg").centerCrop().resize(150, 150).into(fotoIV);
        ciET.setText(visitanteRecibido.getVteCi());
        nombreET.setText(visitanteRecibido.getVteNombre());
        apellidosET.setText(visitanteRecibido.getVteApellidos());
        telcelET.setText(visitanteRecibido.getVteTelefono());
        emailET.setText(visitanteRecibido.getVteCorreo());
        direccionET.setText(visitanteRecibido.getVteDireccion());

        //tipoVisitanteS.setSelection(1);

        //Log.d("msgSel",""+empresaS.getAdapter().getCount());
    }

    public void iniciarSpinnerEmpresa() {
        empresas = new ArrayList<>();
        Empresa empresa = new Empresa();
        empresa.setEmpCod("cod");
        empresa.setEmpNombre("Selecciona una empresa");
        empresa.setEmpObs("obs");
        empresas.add(empresa);
        adapterEmpresa = new ArrayAdapter<Empresa>(this, android.R.layout.simple_spinner_dropdown_item, empresas);
        adapterEmpresa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empresaS.setAdapter(adapterEmpresa);
        empresaS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Empresa empresa = (Empresa) parent.getSelectedItem();
                displayEmpresaData(empresa);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void iniciarSpinnerTipoVisitante() {
        tiposVisitante = new ArrayList<>();
        TipoVisitante tipoVisitante = new TipoVisitante();
        tipoVisitante.setTviCod("cod");
        tipoVisitante.setTviNombre("Selecciona tipo de visitante");
        tipoVisitante.setTviDescripcion("obs");
        tipoVisitante.setHorEstado("estado");
        tiposVisitante.add(tipoVisitante);
        adapterTipoVisitante = new ArrayAdapter<TipoVisitante>(this, android.R.layout.simple_spinner_dropdown_item, tiposVisitante);
        adapterTipoVisitante.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoVisitanteS.setAdapter(adapterTipoVisitante);
        tipoVisitanteS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TipoVisitante tipoVisitante = (TipoVisitante) parent.getSelectedItem();
                displayTipoVisitanteData(tipoVisitante);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void displayEmpresaData(Empresa empresa) {
        String cod = empresa.getEmpCod();
        String nombre = empresa.getEmpNombre();
        String obs = empresa.getEmpObs();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + obs;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    private void displayTipoVisitanteData(TipoVisitante tipoVisitante) {
        String cod = tipoVisitante.getTviCod();
        String nombre = tipoVisitante.getTviNombre();
        String descripcion = tipoVisitante.getTviDescripcion();
        String estado = tipoVisitante.getHorEstado();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + descripcion + "\nEstado: " + estado;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    public void takePicture(View view)
    {
        Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(imageTakeIntent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(imageTakeIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            mimageView.setImageBitmap(imageBitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    private void getDataEmpresa() {
        String url = "http://172.16.0.22:8080/ingresoVisitantes/empresa/lista";
        JsonArrayRequest jsonArrayRequest  = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), "hola "+response.toString(), Toast.LENGTH_LONG).show();

                        int index = -1;
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                Empresa empresa = new Empresa();
                                empresa.setEmpCod(jsonObject.getString("empCod"));
                                empresa.setEmpNombre(jsonObject.getString("empNombre"));
                                empresa.setEmpObs(jsonObject.getString("empObs"));
                                empresas.add(empresa);

                                if(jsonObject.getString("empCod").equals(visitanteRecibido.getEmpresa().getEmpCod()))
                                {
                                    index = i + 1;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //adapterEmpresa.notifyDataSetChanged();
                        empresaS.setSelection(index);
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

    private void getDataTipoVisitante() {
        String url = "http://172.16.0.22:8080/ingresoVisitantes/tipoVisitante/lista";
        JsonArrayRequest jsonArrayRequest  = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), "hola "+response.toString(), Toast.LENGTH_LONG).show();

                        int index = -1;
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                TipoVisitante tipoVisitante = new TipoVisitante();
                                tipoVisitante.setTviCod(jsonObject.getString("tviCod"));
                                tipoVisitante.setTviNombre(jsonObject.getString("tviNombre"));
                                tipoVisitante.setTviDescripcion(jsonObject.getString("tviDescripcion"));
                                tipoVisitante.setHorEstado(jsonObject.getString("horEstado"));
                                tiposVisitante.add(tipoVisitante);

                                if(jsonObject.getString("tviCod").equals(visitanteRecibido.getTipoVisitante().getTviCod()))
                                {
                                    index = i + 1;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //adapterEmpresa.notifyDataSetChanged();
                        tipoVisitanteS.setSelection(index);
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

    public void guardarVisitante(View view) {
        //JSONObject js = new JSONObject();
        JSONObject visitante = new JSONObject();
        try {
            //JSONObject visitante = new JSONObject();
            visitante.put("vteCi", ciET.getText().toString());
            visitante.put("vteCorreo", emailET.getText().toString());
            //visitante.put("vteImagen", "");
            visitante.put("vteNombre", nombreET.getText().toString());
            visitante.put("vteApellidos", apellidosET.getText().toString());
            visitante.put("vteTelefono", telcelET.getText().toString());
            visitante.put("vteDireccion", direccionET.getText().toString());
            //visitante.put("vteEstado", "ACT");

            TipoVisitante tipoVisitanteSS = (TipoVisitante) tipoVisitanteS.getSelectedItem();
            JSONObject tipoVisitante = new JSONObject();
            tipoVisitante.put("tviCod", tipoVisitanteSS.getTviCod());
            tipoVisitante.put("tviNombre", tipoVisitanteSS.getTviNombre());
            tipoVisitante.put("tviDescripcion", tipoVisitanteSS.getTviDescripcion());
            tipoVisitante.put("horEstado", tipoVisitanteSS.getHorEstado());

            Empresa empresaSS = (Empresa) empresaS.getSelectedItem();
            JSONObject empresa = new JSONObject();
            empresa.put("empCod", empresaSS.getEmpCod());
            empresa.put("empNombre", empresaSS.getEmpNombre());
            empresa.put("empObs", empresaSS.getEmpObs());

            visitante.put("tipoVisitante", tipoVisitante);
            visitante.put("empresa", empresa);

            //js.put("data", visitante.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("obVisitante",""+visitante.toString());

        String url = "http://172.16.0.22:8080/ingresoVisitantes/visitante/editar";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT, url, visitante,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("msg", response.toString());
                        try {
                            JSONObject jsonObjectE = new JSONObject(response.toString());
                            if(jsonObjectE.getString("vteCi").equals(visitanteRecibido.getVteCi()))
                            {
                                Toast.makeText(getApplicationContext(), "El visitante fué actualizado", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
