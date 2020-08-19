package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.squareup.picasso.Picasso;
import com.stbnlycan.interfaces.AreaRecintoAPIs;
import com.stbnlycan.interfaces.RegistrarIngresoAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaAPIs;
import com.stbnlycan.models.Aduana;
import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Error;
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

public class RegistraVisitaActivity extends AppCompatActivity implements Validator.ValidationListener {

    private ArrayList<AreaRecinto> areaRecinto;
    private ArrayAdapter<AreaRecinto> adapterAreaR;
    private Visitante visitanteRecibido;
    private Recinto recintoRecibido;

    @Select
    private Spinner areaRecintoS;

    private EditText ciET;
    private EditText nombreET;
    private EditText apellidosET;

    @NotEmpty
    private EditText observacion;
    private Validator validator;

    private Toolbar toolbar;

    private ImageView visitanteIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_visita);

        setTitle("Registrar Ingreso");
        visitanteIV = findViewById(R.id.visitanteIV);
        ciET = findViewById(R.id.ci);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        areaRecintoS = findViewById(R.id.area_recinto);
        observacion = findViewById(R.id.observacion);

        validator = new Validator(this);
        validator.setValidationListener(this);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitanteRecibido = (Visitante) getIntent().getSerializableExtra("visitante");
        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        Picasso.get().load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + visitanteRecibido.getVteImagen()).into(visitanteIV);

        ciET.setText(visitanteRecibido.getVteCi());
        nombreET.setText(visitanteRecibido.getVteNombre());
        apellidosET.setText(visitanteRecibido.getVteApellidos());

        ciET.setEnabled(false);
        ciET.setFocusable(false);
        nombreET.setEnabled(false);
        nombreET.setFocusable(false);
        apellidosET.setEnabled(false);
        apellidosET.setFocusable(false);


        iniciarSpinnerArea();
        fetchAreaRecintos();

    }

    public void iniciarSpinnerArea() {
        areaRecinto = new ArrayList<>();

        AreaRecinto area = new AreaRecinto();
        area.setAreaCod("cod");
        area.setAreaNombre("Selecciona area del recinto");
        area.setAreaDescripcion("descripcion");
        area.setAreaEstado("estado");

        areaRecinto.add(area);
        adapterAreaR = new ArrayAdapter<AreaRecinto>(this, android.R.layout.simple_spinner_dropdown_item, areaRecinto);
        adapterAreaR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaRecintoS.setAdapter(adapterAreaR);
        areaRecintoS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AreaRecinto areaRecinto = (AreaRecinto) parent.getSelectedItem();
                displayAreaRData(areaRecinto);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchAreaRecintos() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        AreaRecintoAPIs areaRecintoAPIs = retrofit.create(AreaRecintoAPIs.class);
        Call<List<AreaRecinto>> call = areaRecintoAPIs.listaPorRecinto(recintoRecibido.getRecCod());
        call.enqueue(new Callback<List<AreaRecinto>>() {
            @Override
            public void onResponse(Call <List<AreaRecinto>> call, retrofit2.Response<List<AreaRecinto>> response) {
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    areaRecinto.add(response.body().get(i));
                }
            }
            @Override
            public void onFailure(Call <List<AreaRecinto>> call, Throwable t) {

            }
        });
    }

    private void displayAreaRData(AreaRecinto areaRecinto) {
        String cod = areaRecinto.getAreaCod();
        String nombre = areaRecinto.getAreaNombre();
        String descripcion = areaRecinto.getAreaDescripcion();
        String estado = areaRecinto.getAreaEstado();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + descripcion + "\nEstado: " + estado;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    //borrar
    /*public void registrarVisita(View view) {

        validator.validate();
    }*/

    @Override
    public void onValidationSucceeded() {
        Visita visita = new Visita();
        AreaRecinto areaRecinto = (AreaRecinto) areaRecintoS.getSelectedItem();
        visita.setVisObs(observacion.getText().toString().toUpperCase());
        visita.setVisitante(visitanteRecibido);
        visita.setAreaRecinto(areaRecinto);
        registrarIngreso(visita);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            // Display error messages
            if (view instanceof EditText) {
                //((EditText) view).setError(message);
                ((EditText) view).setError("Este campo es requerido");
            }else if (view instanceof Spinner) {
                //((TextView) ((Spinner) view).getSelectedView()).setError(message);
                ((TextView) ((Spinner) view).getSelectedView()).setError("Este campo es requerido");
            }else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registrarIngreso(Visita visita) {

        /*Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarIngresoAPIs registrarIngresoAPIs = retrofit.create(RegistrarIngresoAPIs.class);
        Call<Visita> call = registrarIngresoAPIs.registrarIngreso(visita);
        call.enqueue(new Callback<Visita>() {
            @Override
            public void onResponse(Call <Visita> call, retrofit2.Response<Visita> response) {
                Gson gson = new Gson();
                String descripcion = gson.toJson(response.body());
                Log.d("msg",""+descripcion);

                Visita visitaRecibida = response.body();
                if(visitaRecibida.getVisCod() != null)
                {
                    Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha ingresado a " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "El Visitante tiene Salidas Pendientes", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call <Visita> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });*/

        /*Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarIngresoAPIs registrarIngresoAPIs = retrofit.create(RegistrarIngresoAPIs.class);
        Call<Object> call = registrarIngresoAPIs.registrarIngreso(visita);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call <Object> call, retrofit2.Response<Object> response) {

                Log.d("msg0",""+response.body());
                if (response.body() instanceof Visita )
                {
                    Log.d("msg1","objeto tipo Visita");
                    Visita visitaRecibida = (Visita) response.body();
                    if(visitaRecibida.getVisCod() != null)
                    {
                        Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha ingresado a " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                else if (response.body() instanceof Error)
                {
                    Log.d("msg2","objeto tipo Error");
                    Error errorRecibido = (Error) response.body();
                    //handle error object
                    Toast.makeText(getApplicationContext(), ""+errorRecibido.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
            @Override
            public void onFailure(Call <Object> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });*/

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarIngresoAPIs registrarIngresoAPIs = retrofit.create(RegistrarIngresoAPIs.class);
        Call<JsonObject> call = registrarIngresoAPIs.registrarIngreso(visita);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                String jsonString = response.body().toString();
                if (jsonString.contains("visCod")) {
                    Visita visitaRecibida = new Gson().fromJson(jsonString, Visita.class);
                    Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha ingresado a " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Error error = new Gson().fromJson(jsonString, Error.class);
                    Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_registrar_visitante:
                // Registrar visitante
                validator.validate();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_rv, menu);
        return true;
    }

}
