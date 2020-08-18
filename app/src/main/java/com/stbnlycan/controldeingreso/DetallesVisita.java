package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.squareup.picasso.Picasso;
import com.stbnlycan.models.Visita;

public class DetallesVisita extends AppCompatActivity {

    private Toolbar toolbar;
    private Visita visitaRecibida;

    private ImageView visitanteIV;
    @NotEmpty
    private EditText ciET;
    @NotEmpty
    private EditText nombreET;
    @NotEmpty
    private EditText apellidosET;
    @NotEmpty
    private EditText telcelET;
    @NotEmpty
    private EditText emailET;
    @NotEmpty
    private EditText empresa;
    @NotEmpty
    private EditText tipoVisitante;
    @NotEmpty
    private EditText observacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_visita);

        setTitle("Detalles visita");
        toolbar = findViewById(R.id.toolbar);
        visitanteIV = findViewById(R.id.visitanteIV);
        ciET = findViewById(R.id.ci);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        telcelET = findViewById(R.id.telcel);
        emailET = findViewById(R.id.email);
        empresa = findViewById(R.id.empresa);
        tipoVisitante = findViewById(R.id.tipoVisitante);
        observacion = findViewById(R.id.observacion);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitaRecibida = (Visita) getIntent().getSerializableExtra("visita");

        Picasso.get().load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + visitaRecibida.getVisitante().getVteImagen()).into(visitanteIV);


        ciET.setText(visitaRecibida.getVisitante().getVteCi());
        nombreET.setText(visitaRecibida.getVisitante().getVteNombre());
        apellidosET.setText(visitaRecibida.getVisitante().getVteApellidos());
        telcelET.setText(visitaRecibida.getVisitante().getVteTelefono());
        emailET.setText(visitaRecibida.getVisitante().getVteCorreo());
        empresa.setText(visitaRecibida.getVisitante().getEmpresa().getEmpNombre());
        tipoVisitante.setText(visitaRecibida.getVisitante().getTipoVisitante().getTviNombre());
        observacion.setText(visitaRecibida.getVisObs());
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
