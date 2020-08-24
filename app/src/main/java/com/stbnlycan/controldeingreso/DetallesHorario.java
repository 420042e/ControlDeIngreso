package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import com.stbnlycan.models.Horario;
import com.stbnlycan.models.Visita;

public class DetallesHorario extends AppCompatActivity {
    private Toolbar toolbar;
    private Horario horarioRecibido;
    EditText horNombre;
    EditText horDescripcion;
    EditText horDias;
    EditText horaEntrada;
    EditText horaSalida;
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
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        horarioRecibido = (Horario) getIntent().getSerializableExtra("horario");

        String new_days = horarioRecibido.getHorDias();
        new_days = new_days.replace("MONDAY","Lunes");
        new_days = new_days.replace("TUESDAY","Martes");
        new_days = new_days.replace("WEDNESDAY","Miércoles");
        new_days = new_days.replace("THURSDAY","Jueves");
        new_days = new_days.replace("FRIDAY","Viernes");

        horNombre.setText(horarioRecibido.getHorNombre());
        horDescripcion.setText(horarioRecibido.getHorDescripcion());
        horDias.setText(new_days);
        horaEntrada.setText(horarioRecibido.getHorHoraEntrada()+":"+horarioRecibido.getHorMinEntrada());
        horaSalida.setText(horarioRecibido.getHorHoraSalida()+":"+horarioRecibido.getHorMinSalida());
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
