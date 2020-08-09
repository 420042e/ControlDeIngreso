package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.interfaces.EnviarCorreoIAPIs;
import com.stbnlycan.interfaces.ListaVisitantesXNombreAPIs;
import com.stbnlycan.interfaces.ListaVisitantesAPIs;
import com.stbnlycan.models.ListaVisitantes;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class Visitantes extends AppCompatActivity implements VisitantesAdapter.OnVisitanteClickListener, VisitantesAdapter.OnVQRClickListener, VisitantesAdapter.OnEEClickListener{

    private ArrayList<Visitante> visitantes;
    private VisitantesAdapter visitantesAdapter;
    private Toolbar toolbar;
    private final static int REQUEST_CODE_NV = 1;
    private final static int REQUEST_CODE_EV = 2;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar bar;
    private TextView tvFallo;

    private int currentItems, totalItems, scrollOutItems;
    private boolean isScrolling = false;
    private LinearLayoutManager manager;
    private int nPag;

    private String nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitantes);

        nPag = 0;

        setTitle("Visitantes");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitantes = new ArrayList<>();

        /*visitantesAdapter = new VisitantesAdapter(visitantes);
        visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(visitantesAdapter);*/

        manager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems = manager.getChildCount();
                totalItems = manager.getItemCount();
                scrollOutItems = manager.findFirstVisibleItemPosition();
                if(isScrolling && (currentItems + scrollOutItems == totalItems))
                {
                    isScrolling = false;
                    nPag++;
                    mostrarMasVisitantes();
                }
            }
        });


        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);

        fetchVisitantes();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tvFallo.setVisibility(View.GONE);
                actualizarVisitantes();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_nuevo_visitante:
                Intent intent = new Intent(Visitantes.this, NuevoVisitanteActivity.class);
                intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_NV);
                //prueba();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NV) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Visitante visitanteResult = (Visitante) b.getSerializable("visitanteResult");
                    visitantes.add(0, visitanteResult);
                    visitantesAdapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                }
            }
            else if (requestCode == REQUEST_CODE_EV) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Visitante visitanteResult = (Visitante) b.getSerializable("visitanteResult");
                    Log.d("msg3", ""+b.getInt("position", -1));
                    int position = b.getInt("position", -1);
                    visitantes.add(position, visitanteResult);
                    visitantesAdapter.notifyItemChanged(position);
                    recyclerView.scrollToPosition(position);
                }
            }
        }
    }

    private void fetchVisitantes() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitantesAPIs listaVisitantesAPIs = retrofit.create(ListaVisitantesAPIs.class);
        Call<ListaVisitantes> call = listaVisitantesAPIs.listaVisitantes(Integer.toString(nPag),"10");
        call.enqueue(new Callback<ListaVisitantes>() {
            @Override
            public void onResponse(Call <ListaVisitantes> call, retrofit2.Response<ListaVisitantes> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ListaVisitantes listaVisitantes = response.body();
                if(listaVisitantes.getlVisitante().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaVisitantes.getlVisitante().size() ; i++)
                    {
                        visitantes.add(listaVisitantes.getlVisitante().get(i));
                        //Log.d("msg1233",""+listaVisitantes.getlVisitante().get(i).getVteNombre());
                    }
                    visitantesAdapter = new VisitantesAdapter(visitantes);
                    visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);
                    visitantesAdapter.setOnVQRClickListener(Visitantes.this);
                    visitantesAdapter.setOnEEClickListener(Visitantes.this);

                    recyclerView.setAdapter(visitantesAdapter);
                }

                /*for(int i = 0 ; i < response.body().size() ; i++)
                {
                    visitantes.add(response.body().get(i));
                }
                visitantesAdapter = new VisitantesAdapter(visitantes);
                visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);

                recyclerView.setAdapter(visitantesAdapter);*/

                //visitantesAdapter.notifyDataSetChanged();
                //swipeRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onFailure(Call <ListaVisitantes> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarMasVisitantes() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitantesAPIs listaVisitantesAPIs = retrofit.create(ListaVisitantesAPIs.class);
        Call<ListaVisitantes> call = listaVisitantesAPIs.listaVisitantes(Integer.toString(nPag),"10");
        call.enqueue(new Callback<ListaVisitantes>() {
            @Override
            public void onResponse(Call <ListaVisitantes> call, retrofit2.Response<ListaVisitantes> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ListaVisitantes listaVisitantes = response.body();
                for(int i = 0 ; i < listaVisitantes.getlVisitante().size() ; i++)
                {
                    visitantes.add(listaVisitantes.getlVisitante().get(i));
                }
                visitantesAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call <ListaVisitantes> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });

    }

    private void actualizarVisitantes() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitantesAPIs listaVisitantesAPIs = retrofit.create(ListaVisitantesAPIs.class);
        Call<ListaVisitantes> call = listaVisitantesAPIs.listaVisitantes("0","5");
        call.enqueue(new Callback<ListaVisitantes>() {
            @Override
            public void onResponse(Call <ListaVisitantes> call, retrofit2.Response<ListaVisitantes> response) {
                //recintos = response.body();
                visitantes.clear();
                ListaVisitantes listaVisitantes = response.body();
                if(listaVisitantes.getlVisitante().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaVisitantes.getlVisitante().size() ; i++)
                    {
                        visitantes.add(listaVisitantes.getlVisitante().get(i));
                    }
                    visitantesAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }


                /*for(int i = 0 ; i < response.body().size() ; i++)
                {
                    visitantes.add(response.body().get(i));
                }
                visitantesAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);*/
            }
            @Override
            public void onFailure(Call <ListaVisitantes> call, Throwable t) {
                tvFallo.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //visitantes.clear();


        //getDataVisitante();
        //Toast.makeText(this, "Hola", Toast.LENGTH_LONG).show();
    }

    private void buscarVisitanteXNombre() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitantesXNombreAPIs listaVisitantesXNombreAPIs = retrofit.create(ListaVisitantesXNombreAPIs.class);
        Call<ListaVisitantes> call = listaVisitantesXNombreAPIs.listaVisitanteXNombre(nombre,"0","5");
        call.enqueue(new Callback<ListaVisitantes>() {
            @Override
            public void onResponse(Call <ListaVisitantes> call, retrofit2.Response<ListaVisitantes> response) {
                //recintos = response.body();
                visitantes.clear();
                ListaVisitantes listaVisitantes = response.body();
                if(listaVisitantes.getlVisitante().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaVisitantes.getlVisitante().size() ; i++)
                    {
                        visitantes.add(listaVisitantes.getlVisitante().get(i));
                    }
                    visitantesAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            @Override
            public void onFailure(Call <ListaVisitantes> call, Throwable t) {
                tvFallo.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu2, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();



        // Get SearchView autocomplete object.
        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(Color.BLUE);
        searchAutoComplete.setTextColor(Color.GREEN);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.holo_blue_light);
        // Create a new ArrayAdapter and add data to search auto complete object.
        String dataArr[] = {"Apple" , "Amazon" , "Amd", "Microsoft", "Microwave", "MicroNews", "Intel", "Intelligence"};
        ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, dataArr);
        searchAutoComplete.setAdapter(newsAdapter);
        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String queryString=(String)adapterView.getItemAtPosition(position);
                searchAutoComplete.setText("" + queryString);
                Toast.makeText(getApplicationContext(), "you clicked " + queryString, Toast.LENGTH_LONG).show();
            }
        });



        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Buscar visitante");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                nombre = query;
                buscarVisitanteXNombre();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //visitantesAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onEventoClick(Visitante visitante, int position) {
        Log.d("msg1",""+position);
        Intent intent = new Intent(Visitantes.this, EditarVisitanteActivity.class);
        intent.putExtra("visitante", visitante);
        intent.putExtra("position", position);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_EV);
    }

    @Override
    public void OnVQRClick(Visitante visitante) {
        iniciarQRActivity(visitante);
    }

    @Override
    public void OnEEClick(Visitante visitante) {
        enviarCorreoIngreso(visitante);
    }

    public void iniciarQRActivity(Visitante visitante)
    {
        Intent intent = new Intent(Visitantes.this, QRActivity.class);
        intent.putExtra("visitante", visitante);
        startActivity(intent);
    }

    private void enviarCorreoIngreso(final Visitante visitante) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        EnviarCorreoIAPIs enviarCorreoIAPIs = retrofit.create(EnviarCorreoIAPIs.class);
        Call call = enviarCorreoIAPIs.enviarCorreo(visitante.getVteCorreo());
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                if (response.body() != null) {
                    Toast.makeText(getApplicationContext(), "Se envió el correo de ingreso a "+visitante.getVteNombre() +" "+visitante.getVteApellidos(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("msg4",""+t);
            }
        });
    }
}
