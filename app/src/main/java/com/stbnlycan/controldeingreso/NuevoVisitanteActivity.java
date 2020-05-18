package com.stbnlycan.controldeingreso;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
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
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.stbnlycan.interfaces.UploadAPIs;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class NuevoVisitanteActivity extends AppCompatActivity {

    private ImageView mimageView;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private List<Recinto> areas;
    ArrayList<Empresa> empresas;
    ArrayList<TipoVisitante> tiposVisitante;

    ArrayAdapter<Empresa> adapterEmpresa;
    ArrayAdapter<TipoVisitante> adapterTipoVisitante;

    private EditText ciET;
    private EditText nombreET;
    private EditText apellidosET;
    private EditText telcelET;
    private EditText emailET;
    private EditText direccionET;
    private Spinner empresaS;
    private Spinner tipoVisitanteS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_visitante);

        setTitle("Nuevo visitante");
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

        getDataEmpresa();
        getDataTipoVisitante();
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

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                Empresa empresa = new Empresa();
                                empresa.setEmpCod(jsonObject.getString("empCod"));
                                empresa.setEmpNombre(jsonObject.getString("empNombre"));
                                empresa.setEmpObs(jsonObject.getString("empObs"));
                                empresas.add(empresa);
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

    private void getDataTipoVisitante() {
        String url = "http://172.16.0.22:8080/ingresoVisitantes/tipoVisitante/lista";
        JsonArrayRequest jsonArrayRequest  = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), "hola "+response.toString(), Toast.LENGTH_LONG).show();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                TipoVisitante tipoVisitante = new TipoVisitante();
                                tipoVisitante.setTviCod(jsonObject.getString("tviCod"));
                                tipoVisitante.setTviNombre(jsonObject.getString("tviNombre"));
                                tipoVisitante.setTviDescripcion(jsonObject.getString("tviDescripcion"));
                                tipoVisitante.setHorEstado(jsonObject.getString("horEstado"));
                                tiposVisitante.add(tipoVisitante);
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

    public void guardarVisitante(View view) {
        /*JSONObject visitante = new JSONObject();
        try {
            //JSONObject visitante = new JSONObject();
            visitante.put("vteCi", ciET.getText().toString());
            visitante.put("vteCorreo", emailET.getText().toString());
            visitante.put("vteNombre", nombreET.getText().toString());
            visitante.put("vteApellidos", apellidosET.getText().toString());
            visitante.put("vteTelefono", telcelET.getText().toString());
            visitante.put("vteDireccion", direccionET.getText().toString());

            visitante.put("vteEstado", "ACT");
            visitante.put("vteLlave", "");
            visitante.put("vteFecha", "2020-05-07T11:42:49.636");

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
        Log.d("ob",""+visitante.toString());

        String url = "http://172.16.0.22:8080/ingresoVisitantes/visitante/registrar";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, visitante,
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
        requestQueue.add(jsonObjectRequest);*/

        String descripcion = "{\n" +
                "    \"vteCi\": \"123768\",\n" +
                "    \"vteCorreo\": \"fercho@gmail.com\",\n" +
                "    \"vteImagen\": \"\",\n" +
                "    \"vteNombre\": \"Fernando\",\n" +
                "    \"vteApellidos\": \"Vasquez Apaza\",\n" +
                "    \"vteTelefono\": \"60577384\",\n" +
                "    \"vteDireccion\": \"Calle Mejillones\",\n" +
                "    \"tipoVisitante\": {\n" +
                "        \"tviCod\": 2,\n" +
                "        \"tviNombre\": \"TRAMITADOR\",\n" +
                "        \"tviDescripcion\": \"TRAMITADORES\",\n" +
                "        \"horEstado\": \"ACT\"\n" +
                "    },\n" +
                "    \"empresa\": {\n" +
                "        \"empCod\": 1,\n" +
                "        \"empNombre\": \"CHASQUI\",\n" +
                "        \"empObs\": \"EMPRESA DE TRANSPORTE CHASQUI\"\n" +
                "    }\n" +
                "}";
        uploadToServer(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/windows.png", descripcion);
        Log.d("msg0",""+ Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void uploadToServer(String filePath, String descripcion) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        //Create a file object using file path
        File file = new File(filePath);
        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        //Create request body with text description and text media type
        //RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), descripcion);
        //
        Call call = uploadAPIs.uploadImage(part, description);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                Log.d("msg1",""+response);
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("msg2",""+t);
            }
        });
    }

}
