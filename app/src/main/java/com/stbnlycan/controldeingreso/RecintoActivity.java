package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.models.Accion;
import com.stbnlycan.models.Recinto;

import java.util.ArrayList;
import java.util.List;

public class RecintoActivity extends AppCompatActivity implements RecintoAdapter.OnEventoListener {

    private Toolbar toolbar;
    private Recinto recintoRecibido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recinto);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        setTitle(recintoRecibido.getRecNombre());

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<Accion> cards = new ArrayList<>();
        cards.add(new Accion(0, "Visitas", R.drawable.icono_registro_visita));
        cards.add(new Accion(1, "Salidas", R.drawable.icono_registro_salida));
        cards.add(new Accion(2, "Visitantes", R.drawable.icono_visitantes));
        cards.add(new Accion(3, "Horarios", R.drawable.icono_horario));



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

        /*if(position == 0)
        {
            Intent intent = new Intent(RecintoActivity.this, RegistrarVisitasActivity.class);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 1)
        {
            Intent intent = new Intent(RecintoActivity.this, RegistrarSalidasActivity.class);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 2)
        {
            Intent intent = new Intent(RecintoActivity.this, Visitantes.class);
            //intent.putExtra("titulo",areaRecinto.getNombreEvento());
            startActivity(intent);
        }
        else if(position == 3)
        {
            Intent intent = new Intent(RecintoActivity.this, NuevoVisitanteActivity.class);
            //intent.putExtra("titulo",areaRecinto.getNombreEvento());
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 4)
        {
            Intent intent = new Intent(RecintoActivity.this, Horarios.class);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            //intent.putExtra("recNombre", getIntent().getStringExtra("recNombre"));
            startActivity(intent);
        }
        else if(position == 5)
        {
            Intent intent = new Intent(RecintoActivity.this, NuevoHorarioActivity.class);
            //intent.putExtra("titulo",areaRecinto.getNombreEvento());
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }*/
        if(position == 0)
        {
            Intent intent = new Intent(RecintoActivity.this, RegistrarVisitasActivity.class);
            intent.putExtra("recinto", recintoRecibido);
            //intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 1)
        {
            Intent intent = new Intent(RecintoActivity.this, RegistrarSalidasActivity.class);
            intent.putExtra("recinto", recintoRecibido);
            //intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            startActivity(intent);
        }
        else if(position == 2)
        {
            Intent intent = new Intent(RecintoActivity.this, Visitantes.class);
            //intent.putExtra("titulo",areaRecinto.getNombreEvento());
            startActivity(intent);
        }
        else if(position == 3)
        {
            Intent intent = new Intent(RecintoActivity.this, Horarios.class);
            intent.putExtra("recinto", recintoRecibido);
            intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
            //intent.putExtra("recNombre", getIntent().getStringExtra("recNombre"));
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
