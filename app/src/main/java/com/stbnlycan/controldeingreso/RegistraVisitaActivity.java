package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
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
    private Button btnRV;

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
        btnRV = findViewById(R.id.btnRV);

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

        Picasso.get().load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + visitanteRecibido.getVteImagen()).centerCrop().resize(150, 150).into(visitanteIV);

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

        btnRV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
                //Log.d("msg", "hola");
            }
        });
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

                Log.d("msg",""+response.body());
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
        visita.setVisObs(observacion.getText().toString());
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
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarIngresoAPIs registrarIngresoAPIs = retrofit.create(RegistrarIngresoAPIs.class);
        Call<Visita> call = registrarIngresoAPIs.registrarIngreso(visita);
        call.enqueue(new Callback<Visita>() {
            @Override
            public void onResponse(Call <Visita> call, retrofit2.Response<Visita> response) {
                Visita visitaRecibida = response.body();
                if(visitaRecibida.getVisCod() != null)
                {
                    Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha ingresado a " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "El Visitante tiene Salidas Pendientes", Toast.LENGTH_SHORT).show();
                    finish();
                }
                //Log.d("msg",""+horarioRecibido.getHorNombre());
                /*Toast.makeText(getApplicationContext(), "La salida fué registrada", Toast.LENGTH_SHORT).show();
                finish();*/
            }
            @Override
            public void onFailure(Call <Visita> call, Throwable t) {
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
        }
        return super.onOptionsItemSelected(item);
    }

}
