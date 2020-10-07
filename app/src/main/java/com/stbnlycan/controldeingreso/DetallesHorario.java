package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.models.Horario;
import com.stbnlycan.models.Visita;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class DetallesHorario extends AppCompatActivity {
    private Toolbar toolbar;
    private Horario horarioRecibido;
    EditText tipoVisitante;
    EditText horNombre;
    EditText horDescripcion;
    EditText horDias;
    EditText horaEntrada;
    EditText horaSalida;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_horario);

        setTitle("Detalles");
        toolbar = findViewById(R.id.toolbar);
        horNombre = findViewById(R.id.horNombre);
        horDescripcion = findViewById(R.id.horDescripcion);
        horDias = findViewById(R.id.horDias);
        horaEntrada = findViewById(R.id.horaEntrada);
        horaSalida = findViewById(R.id.horaSalida);
        tipoVisitante = findViewById(R.id.tipoVisitante);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        horarioRecibido = (Horario) getIntent().getSerializableExtra("horario");

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);


        String new_days = horarioRecibido.getHorDias();
        new_days = new_days.replace("MONDAY","Lunes");
        new_days = new_days.replace("TUESDAY","Martes");
        new_days = new_days.replace("WEDNESDAY","Miércoles");
        new_days = new_days.replace("THURSDAY","Jueves");
        new_days = new_days.replace("FRIDAY","Viernes");

        tipoVisitante.setText(horarioRecibido.getTipoVisitante().getTviNombre());
        horNombre.setText(horarioRecibido.getHorNombre());
        horDescripcion.setText(horarioRecibido.getHorDescripcion());
        horDias.setText(new_days);
        horaEntrada.setText(horarioRecibido.getHorHoraEntrada()+":"+horarioRecibido.getHorMinEntrada());
        horaSalida.setText(horarioRecibido.getHorHoraSalida()+":"+horarioRecibido.getHorMinSalida());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_salir:
                cerrarSesion();
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
                Intent intentS = new Intent(DetallesHorario.this, LoginActivity.class);
                startActivity(intentS);
                finish();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }
}
