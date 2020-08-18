package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.interfaces.EnviarCorreoIAPIs;
import com.stbnlycan.interfaces.ListaVisitantesXNombreAPIs;
import com.stbnlycan.interfaces.ListaVisitantesAPIs;
import com.stbnlycan.models.ListaVisitantes;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

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
    private TextView tvNoData;

    private int currentItems, totalItems, scrollOutItems;
    private boolean isScrolling = false;
    private LinearLayoutManager manager;
    private int nPag;

    private String nombre;
    private ArrayList<Visitante> dataArr;

    private SearchView searchView;
    private List<Visitante> suggestions;
    private CursorAdapter suggestionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitantes);

        nPag = 0;

        setTitle("Visitantes");

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        tvNoData = (TextView) findViewById(R.id.tvNoData);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        manager = new LinearLayoutManager(this);

        visitantes = new ArrayList<>();
        visitantesAdapter = new VisitantesAdapter(visitantes);
        visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);
        visitantesAdapter.setOnVQRClickListener(Visitantes.this);
        visitantesAdapter.setOnEEClickListener(Visitantes.this);

        recyclerView.setAdapter(visitantesAdapter);

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

        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);

        actualizarVisitantes();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                //tvNoData.setVisibility(View.GONE);
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
                startActivityForResult(intent, REQUEST_CODE_NV);
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
                    int position = b.getInt("position", -1);
                    visitantes.set(position, visitanteResult);
                    visitantesAdapter.notifyItemChanged(position);
                    recyclerView.scrollToPosition(position);
                }
            }
        }
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
                visitantes.clear();
                ListaVisitantes listaVisitantes = response.body();
                if(listaVisitantes.getlVisitante().size() == 0)
                {
                    tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    bar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaVisitantes.getlVisitante().size() ; i++)
                    {
                        visitantes.add(listaVisitantes.getlVisitante().get(i));
                    }
                    visitantesAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
                nPag = 0;
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
                suggestions.clear();
                ListaVisitantes listaVisitantes = response.body();
                if(listaVisitantes.getlVisitante().size() == 0)
                {
                    tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaVisitantes.getlVisitante().size() ; i++)
                    {
                        suggestions.add(listaVisitantes.getlVisitante().get(i));
                        String[] columns = { BaseColumns._ID,
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                        };
                        MatrixCursor cursor = new MatrixCursor(columns);
                        for (int j = 0; j < suggestions.size(); j++) {
                            String[] tmp = {Integer.toString(j), suggestions.get(j).getVteNombre() + " " + suggestions.get(j).getVteApellidos(), suggestions.get(j).getVteNombre()};
                            cursor.addRow(tmp);
                        }
                        suggestionAdapter.swapCursor(cursor);
                    }
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
        searchView = (SearchView) searchItem.getActionView();

        // Solution
        int autoCompleteTextViewID = getResources().getIdentifier("search_src_text", "id", getPackageName());
        AutoCompleteTextView searchAutoCompleteTextView = (AutoCompleteTextView) searchView.findViewById(autoCompleteTextViewID);
        searchAutoCompleteTextView.setThreshold(0);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        suggestions = new ArrayList<>();

        suggestionAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1},
                new int[]{android.R.id.text1},
                0);

        searchView.setSuggestionsAdapter(suggestionAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchView.setQuery(suggestions.get(position).getVteNombre(), true);
                searchView.clearFocus();

                visitantes.clear();
                visitantes.add(suggestions.get(position));
                visitantesAdapter.notifyDataSetChanged();
                return true;
            }
        });

        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Ingresa nombre del visitante");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                nombre = query;
                //buscarVisitanteXNombre();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                nombre = newText.toUpperCase();
                if(newText.equals("")){
                    nPag = 0;
                    actualizarVisitantes();
                }
                else
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buscarVisitanteXNombre();
                        }
                    }, 300);
                    return false;
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public void onEventoClick(Visitante visitante, int position) {
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
