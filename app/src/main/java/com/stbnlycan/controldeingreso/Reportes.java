package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.stbnlycan.adapters.VisitanteListAdapter;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;

public class Reportes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        setTitle("Reportes");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        ListView reporteVisitantes = findViewById(R.id.reporteVisitantes);

        ArrayList<Visitante> listaVisitantes = new ArrayList<>();

        /*for(int i = 0; i < 10 ; i++)
        {
            Visitante visitante = new Visitante("Visitante Visitante Visitante "+i, "Apellido "+i);
            listaVisitantes.add(visitante);
        }*/

        VisitanteListAdapter adapter = new VisitanteListAdapter(this, R.layout.adapter_view_layout, listaVisitantes);
        reporteVisitantes.setAdapter(adapter);
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
