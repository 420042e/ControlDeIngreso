package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.RegistrarHorarioAPIs;
import com.stbnlycan.models.Horario;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NuevoHorarioActivity extends AppCompatActivity implements Validator.ValidationListener {

    private List<Recinto> recintos;
    private List<TipoVisitante> tipoVisitantes;

    @NotEmpty
    private EditText horNombre;

    @NotEmpty
    private EditText horDescripcion;

    private TimePicker entrada;

    private TimePicker salida;

    /*@Select
    private Spinner recintoS;

    @Select
    private Spinner tipoVisitanteS;*/

    private ArrayAdapter<Recinto> adapterRecinto;
    private ArrayAdapter<TipoVisitante> adapterTipoVisitante;

    private Validator validator;
    private Toolbar toolbar;
    private Recinto recintoRecibido;
    private TipoVisitante tipoVisitanteRecibido;
    private Horario horario;

    private TextView diasTV;
    private CheckBox dia1;
    private CheckBox dia2;
    private CheckBox dia3;
    private CheckBox dia4;
    private CheckBox dia5;

    private Button btnNH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_horario);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");
        tipoVisitanteRecibido = (TipoVisitante) getIntent().getSerializableExtra("tipoVisitante");

        setTitle("Nuevo Horario");

        validator = new Validator(this);
        validator.setValidationListener(this);

        horNombre = findViewById(R.id.horNombre);
        horDescripcion = findViewById(R.id.horDescripcion);
        diasTV = findViewById(R.id.diasTV);
        dia1 = findViewById(R.id.dia1);
        dia2 = findViewById(R.id.dia2);
        dia3 = findViewById(R.id.dia3);
        dia4 = findViewById(R.id.dia4);
        dia5 = findViewById(R.id.dia5);
        entrada = findViewById(R.id.entrada);
        salida = findViewById(R.id.salida);

        btnNH = findViewById(R.id.btnNH);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        entrada = (TimePicker)findViewById(R.id.entrada);
        entrada.setIs24HourView(true);

        salida = (TimePicker)findViewById(R.id.salida);
        salida.setIs24HourView(true);

        btnNH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
                //Log.d("msg", "hola");
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

    private void registrarHorario() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarHorarioAPIs registrarHorarioAPIs = retrofit.create(RegistrarHorarioAPIs.class);
        Call<Horario> call = registrarHorarioAPIs.registrarHorario(horario);
        call.enqueue(new Callback <Horario> () {
            @Override
            public void onResponse(Call <Horario> call, Response <Horario> response) {
                Horario horarioRecibido = response.body();
                //Log.d("msg",""+horarioRecibido.getHorNombre());
                Toast.makeText(getApplicationContext(), "El nuevo horario fué registrado", Toast.LENGTH_SHORT).show();
                horario.setHorEstado("0");
                Intent intent = new Intent();
                intent.putExtra("horarioResult", horario);
                setResult(RESULT_OK, intent);
                finish();
                //finish();
            }
            @Override
            public void onFailure(Call <Horario> call, Throwable t) {

            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        Log.d("msgd", ""+ dia1.isChecked());
        if (!dia1.isChecked() && !dia2.isChecked() && !dia3.isChecked() && !dia4.isChecked() && !dia5.isChecked()) {
            diasTV.setError("Este campo es requerido");
            Toast.makeText(this, "Se debe seleccionar 1 día por lo menos", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String dias = "";
            if(dia1.isChecked())
            {
                dias = dias + "MONDAY,";
            }
            if(dia2.isChecked())
            {
                dias = dias + "TUESDAY,";
            }
            if(dia3.isChecked())
            {
                dias = dias + "WEDNESDAY,";
            }
            if(dia4.isChecked())
            {
                dias = dias + "THURSDAY,";
            }
            if(dia5.isChecked())
            {
                dias = dias + "FRIDAY,";
            }
            dias = dias.substring(0, dias.length() - 1);
            //Log.d("msgdias",""+dias);

            diasTV.setError(null);
            horario = new Horario();
            horario.setHorNombre(horNombre.getText().toString());
            horario.setHorDescripcion(horDescripcion.getText().toString());
            horario.setHorDias(dias);
            horario.setHorHoraEntrada(Integer.toString(entrada.getCurrentHour()));
            horario.setHorMinEntrada(Integer.toString(entrada.getCurrentMinute()));
            horario.setHorHoraSalida(Integer.toString(salida.getCurrentHour()));
            horario.setHorMinSalida(Integer.toString(salida.getCurrentHour()));
            horario.setRecinto(recintoRecibido);
            horario.setTipoVisitante(tipoVisitanteRecibido);
            showLoadingwDialog();
            registrarHorario();


        }

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

    void showLoadingwDialog() {

        final LoadingFragment dialogFragment = new LoadingFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putInt("tiempo", 0);
        dialogFragment.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }
}
