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
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.os.Bundle;
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

import com.stbnlycan.adapters.EmpresasAdapter;
import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.interfaces.ListaEmpresasAPIs;
import com.stbnlycan.interfaces.ListaEmpresasXNombreAPIs;
import com.stbnlycan.interfaces.ListaVisitantesAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.ListaEmpresas;
import com.stbnlycan.models.ListaVisitantes;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class Empresas extends AppCompatActivity implements EmpresasAdapter.OnVisitanteClickListener{

    private ArrayList<Empresa> empresas;
    private EmpresasAdapter empresasAdapter;
    private Toolbar toolbar;
    private int currentItems, totalItems, scrollOutItems;
    private boolean isScrolling = false;
    private LinearLayoutManager manager;
    private int nPag;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar bar;
    private TextView tvFallo;
    private String nombre;

    private SearchView searchView;
    private List<Empresa> suggestions;
    private CursorAdapter suggestionAdapter;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private final static int REQUEST_CODE_NE = 1;
    private final static int REQUEST_CODE_EE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresas);

        setTitle("Empresas");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        empresas = new ArrayList<>();

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);

        manager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
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
                    mostrarMasEmpresas();
                }
            }
        });

        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);

        fetchEmpresas();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tvFallo.setVisibility(View.GONE);
                actualizarEmpresas();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NE) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Empresa empresaResult = (Empresa) b.getSerializable("empresaResult");
                    empresas.add(0, empresaResult);
                    empresasAdapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                }
            }
            else if (requestCode == REQUEST_CODE_EE) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Empresa empresaResult = (Empresa) b.getSerializable("empresaResult");
                    int position = b.getInt("position", -1);
                    empresas.set(position, empresaResult);
                    empresasAdapter.notifyItemChanged(position);
                    recyclerView.scrollToPosition(position);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_nueva_empresa:
                Intent intent = new Intent(Empresas.this, NuevaEmpresa.class);
                startActivityForResult(intent, REQUEST_CODE_NE);
                return false;
            case R.id.action_salir:
                cerrarSesion();
                Intent intentS = new Intent(Empresas.this, LoginActivity.class);
                startActivity(intentS);
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        LogoutAPIs logoutAPIs = retrofit.create(LogoutAPIs.class);
        Call<Void> call = logoutAPIs.logout(pref.getString("access_token", null));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call <Void> call, retrofit2.Response<Void> response) {
                editor.putString("access_token", "");
                editor.putString("token_type", "");
                editor.putString("rol", "");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Sesión finalizada", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ne, menu);
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
                searchView.setQuery(suggestions.get(position).getEmpNombre(), true);
                searchView.clearFocus();

                empresas.clear();
                empresas.add(suggestions.get(position));
                empresasAdapter.notifyDataSetChanged();
                return true;
            }
        });


        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Buscar empresa");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                nombre = query.toUpperCase();
                //buscarEmpresaXNombre();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    actualizarEmpresas();
                }
                nombre = newText.toUpperCase();
                buscarEmpresaXNombre();
                return false;
            }
        });
        return true;
    }

    private void fetchEmpresas() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaEmpresasAPIs listaEmpresasAPIs = retrofit.create(ListaEmpresasAPIs.class);
        Call<ListaEmpresas> call = listaEmpresasAPIs.listaEmpresas(Integer.toString(nPag),"10", authorization);
        call.enqueue(new Callback<ListaEmpresas>() {
            @Override
            public void onResponse(Call <ListaEmpresas> call, retrofit2.Response<ListaEmpresas> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ListaEmpresas listaEmpresas = response.body();
                if(listaEmpresas.getlEmpresa().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaEmpresas.getlEmpresa().size() ; i++)
                    {
                        empresas.add(listaEmpresas.getlEmpresa().get(i));
                        //Log.d("msg1233",""+listaVisitantes.getlVisitante().get(i).getVteNombre());
                    }
                    empresasAdapter = new EmpresasAdapter(empresas);
                    empresasAdapter.setOnVisitanteClickListener(Empresas.this);

                    recyclerView.setAdapter(empresasAdapter);
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
            public void onFailure(Call <ListaEmpresas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void actualizarEmpresas()
    {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaEmpresasAPIs listaEmpresasAPIs = retrofit.create(ListaEmpresasAPIs.class);
        Call<ListaEmpresas> call = listaEmpresasAPIs.listaEmpresas(Integer.toString(nPag),"10", authorization);
        call.enqueue(new Callback<ListaEmpresas>() {
            @Override
            public void onResponse(Call <ListaEmpresas> call, retrofit2.Response<ListaEmpresas> response) {
                empresas.clear();
                ListaEmpresas listaEmpresas = response.body();
                if(listaEmpresas.getlEmpresa().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaEmpresas.getlEmpresa().size() ; i++)
                    {
                        empresas.add(listaEmpresas.getlEmpresa().get(i));
                        //Log.d("msg1233",""+listaVisitantes.getlVisitante().get(i).getVteNombre());
                    }
                    empresasAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            @Override
            public void onFailure(Call <ListaEmpresas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarMasEmpresas()
    {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaEmpresasAPIs listaEmpresasAPIs = retrofit.create(ListaEmpresasAPIs.class);
        Call<ListaEmpresas> call = listaEmpresasAPIs.listaEmpresas(Integer.toString(nPag),"10", authorization);
        call.enqueue(new Callback<ListaEmpresas>() {
            @Override
            public void onResponse(Call <ListaEmpresas> call, retrofit2.Response<ListaEmpresas> response) {
                ListaEmpresas listaEmpresas = response.body();
                if(listaEmpresas.getlEmpresa().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaEmpresas.getlEmpresa().size() ; i++)
                    {
                        empresas.add(listaEmpresas.getlEmpresa().get(i));
                    }
                    empresasAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call <ListaEmpresas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void buscarEmpresaXNombre()
    {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaEmpresasXNombreAPIs listaEmpresasXNombreAPIs = retrofit.create(ListaEmpresasXNombreAPIs.class);
        Call<ListaEmpresas> call = listaEmpresasXNombreAPIs.listaEmpresasXNombre(nombre, "0","10", authorization);
        call.enqueue(new Callback<ListaEmpresas>() {
            @Override
            public void onResponse(Call <ListaEmpresas> call, retrofit2.Response<ListaEmpresas> response) {
                //empresas.clear();
                suggestions.clear();
                ListaEmpresas listaEmpresas = response.body();
                if(listaEmpresas.getlEmpresa().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaEmpresas.getlEmpresa().size() ; i++)
                    {
                        suggestions.add(listaEmpresas.getlEmpresa().get(i));
                        String[] columns = { BaseColumns._ID,
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                        };
                        MatrixCursor cursor = new MatrixCursor(columns);
                        for (int j = 0; j < suggestions.size(); j++) {
                            String[] tmp = {Integer.toString(j), suggestions.get(j).getEmpNombre(), suggestions.get(j).getEmpNombre()};
                            cursor.addRow(tmp);
                        }
                        suggestionAdapter.swapCursor(cursor);

                        //empresas.add(listaEmpresas.getlEmpresa().get(i));
                    }
                    empresasAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            @Override
            public void onFailure(Call <ListaEmpresas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEventoClick(Empresa empresa, int position) {
        Intent intent = new Intent(Empresas.this, EditarEmpresa.class);
        intent.putExtra("empresa", empresa);
        intent.putExtra("position", position);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_EE);
    }
}
