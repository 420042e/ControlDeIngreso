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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetallesVisita extends AppCompatActivity {

    private Toolbar toolbar;
    private Visita visitaRecibida;

    private ImageView visitanteIV;
    @NotEmpty
    private EditText nombreET;
    @NotEmpty
    private EditText apellidosET;
    @NotEmpty
    private EditText telcelET;
    @NotEmpty
    private EditText empresa;
    @NotEmpty
    private EditText tipoVisitante;
    @NotEmpty
    private EditText observacion;
    @NotEmpty
    private EditText fIngreso;
    @NotEmpty
    private EditText fSalida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_visita);

        setTitle("Detalles visita");
        toolbar = findViewById(R.id.toolbar);
        visitanteIV = findViewById(R.id.visitanteIV);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        telcelET = findViewById(R.id.telcel);
        empresa = findViewById(R.id.empresa);
        tipoVisitante = findViewById(R.id.tipoVisitante);
        fIngreso = findViewById(R.id.fIngreso);
        fSalida = findViewById(R.id.fSalida);
        observacion = findViewById(R.id.observacion);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitaRecibida = (Visita) getIntent().getSerializableExtra("visita");

        Picasso.get().load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + visitaRecibida.getVisitante().getVteImagen()).into(visitanteIV);

        String dtIngreso = visitaRecibida.getVisIngreso();
        String dtSalida = visitaRecibida.getVisSalida();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        SimpleDateFormat dd_MM_yyyy = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat hh_mm = new SimpleDateFormat("HH:mm");
        Date date = null;
        Date date2 = null;
        try {
            date = format.parse(dtIngreso);
            if(dtSalida == null)
            {
                fIngreso.setText(dd_MM_yyyy.format(date)+" "+hh_mm.format(date));
                fSalida.setText("Con salida");
            }
            else
            {
                date2 = format.parse(dtSalida);
                fIngreso.setText(dd_MM_yyyy.format(date)+" "+hh_mm.format(date));
                fSalida.setText(dd_MM_yyyy.format(date2)+" "+hh_mm.format(date2));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        nombreET.setText(visitaRecibida.getVisitante().getVteNombre());
        apellidosET.setText(visitaRecibida.getVisitante().getVteApellidos());
        telcelET.setText(visitaRecibida.getVisitante().getVteTelefono());
        empresa.setText(visitaRecibida.getVisitante().getEmpresa().getEmpNombre());
        tipoVisitante.setText(visitaRecibida.getVisitante().getTipoVisitante().getTviNombre());
        /*fIngreso.setText(visitaRecibida.getVisIngreso());
        fSalida.setText(visitaRecibida.getVisSalida());*/
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
