package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.models.Accion;

import java.util.ArrayList;
import java.util.List;

public class RecintoActivity extends AppCompatActivity implements RecintoAdapter.OnEventoListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recinto);

        setTitle(getIntent().getStringExtra("titulo"));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        List<Accion> cards = new ArrayList<>();
        cards.add(new Accion(0, "Registrar Visita", R.drawable.icono_registrar_visita));
        cards.add(new Accion(1, "Visitantes", R.drawable.icono_reportes));
        cards.add(new Accion(2, "Nuevo visitante", R.drawable.icono_nuevo_registro));



        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecintoAdapter adapter = new RecintoAdapter(cards);
        adapter.setOnEventoClickListener(RecintoActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEventoDetailsClick(int position) {
        Log.d("EventoDetails",""+position);

        if(position == 0)
        {
            Intent intent = new Intent(RecintoActivity.this, RegistrarVisitasActivity.class);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 1)
        {
            Intent intent = new Intent(RecintoActivity.this, Visitantes.class);
            //intent.putExtra("titulo",areaRecinto.getNombreEvento());
            startActivity(intent);
        }
        else if(position == 2)
        {
            Intent intent = new Intent(RecintoActivity.this, NuevoVisitanteActivity.class);
            //intent.putExtra("titulo",areaRecinto.getNombreEvento());
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
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
