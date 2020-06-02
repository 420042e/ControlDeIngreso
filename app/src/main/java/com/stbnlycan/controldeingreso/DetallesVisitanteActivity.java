package com.stbnlycan.controldeingreso;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class DetallesVisitanteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_visitante);

        setTitle("Detalles visitante");

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/

        Spinner spinner = findViewById(R.id.empresa);
        ArrayList<String> empresas = new ArrayList<String>();
        empresas.add("Empresa 1");
        empresas.add("Empresa 2");
        empresas.add("Empresa 3");
        empresas.add("Empresa 4");
        empresas.add("Empresa 5");

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, empresas);
        spinner.setAdapter(aa);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Spinner", ""+adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        Spinner spinner2 = findViewById(R.id.tipo_visitante);
        ArrayList<String> tiposVisitante = new ArrayList<String>();
        tiposVisitante.add("Chofer");
        tiposVisitante.add("Agente");
        tiposVisitante.add("Tipo 3");
        tiposVisitante.add("Tipo 4");
        tiposVisitante.add("Tipo 5");

        ArrayAdapter aa2 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposVisitante);
        spinner2.setAdapter(aa2);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Spinner", ""+adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
