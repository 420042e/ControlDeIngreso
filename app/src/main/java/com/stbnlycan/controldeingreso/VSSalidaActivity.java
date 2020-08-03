package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stbnlycan.adapters.VisitasAdapter;
import com.stbnlycan.interfaces.ListaVCSalidaAPIs;
import com.stbnlycan.interfaces.ListaVSSalidaAPIs;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.Visita;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class VSSalidaActivity extends AppCompatActivity implements VisitasAdapter.OnVisitanteClickListener {

    private Recinto recintoRecibido;
    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private ArrayList<Visita> visitas;
    private VisitasAdapter visitasAdapter;

    private ProgressBar bar;
    private TextView tvFallo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_s_salida);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        setTitle("Visitantes sin salidas pendientes");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitas = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));

        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);

        fetchVisitas();
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

    private void fetchVisitas() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVSSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVSSalidaAPIs.class);
        Call<List<Visita>> call = listaVSSalidaAPIs.listaVSSalida("01-06-2020", "01-06-2020", recintoRecibido.getRecCod(), "2");
        call.enqueue(new Callback<List<Visita>>() {
            @Override
            public void onResponse(Call <List<Visita>> call, retrofit2.Response<List<Visita>> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    visitas.add(response.body().get(i));
                }
                visitasAdapter = new VisitasAdapter(visitas);
                visitasAdapter.setOnVisitanteClickListener(VSSalidaActivity.this);

                recyclerView.setAdapter(visitasAdapter);
            }
            @Override
            public void onFailure(Call <List<Visita>> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEventoClick(Visita Visita, int position) {

    }
}
