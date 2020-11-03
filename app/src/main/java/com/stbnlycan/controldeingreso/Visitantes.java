package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.fragments.DFTknExpired;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.EnviarCorreoIAPIs;
import com.stbnlycan.interfaces.ListaVisitantesXNombreAPIs;
import com.stbnlycan.interfaces.ListaVisitantesAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.models.ListaVisitantes;
import com.stbnlycan.models.Visitante;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String totalElements;
    private boolean sugerenciaPress;
    private LoadingFragment dialogFragment;
    boolean isLoading = false;

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

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", authorization)
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        visitantes = new ArrayList<>();
        //visitantesAdapter = new VisitantesAdapter(this, client, visitantes);
        visitantesAdapter = new VisitantesAdapter(this, authorization, visitantes);
        visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);
        visitantesAdapter.setOnVQRClickListener(Visitantes.this);
        visitantesAdapter.setOnEEClickListener(Visitantes.this);

        suggestions = new ArrayList<>();

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
                /*currentItems = manager.getChildCount();
                totalItems = manager.getItemCount();
                scrollOutItems = manager.findFirstVisibleItemPosition();
                if(isScrolling && (currentItems + scrollOutItems == totalItems)  && totalItems != Integer.parseInt(totalElements) && !sugerenciaPress)
                {
                    isScrolling = false;
                    nPag++;
                    mostrarMasVisitantes();
                }*/

                totalItems = manager.getItemCount();
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == visitantes.size() - 1 && totalItems != Integer.parseInt(totalElements) && !sugerenciaPress) {
                        //bottom of list!
                        nPag++;
                        mostrarMasVisitantes();
                        isLoading = true;
                    }
                }
            }
        });

        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);

        //recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

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
            case R.id.action_salir:
                cerrarSesion();
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
                Intent intent = new Intent(Visitantes.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
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
        visitantes.add(null);
        visitantesAdapter.notifyItemInserted(visitantes.size() - 1);

        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 2000);*/

        Retrofit retrofit = NetworkClient.getRetrofitClient(getApplication());
        ListaVisitantesAPIs listaVisitantesAPIs = retrofit.create(ListaVisitantesAPIs.class);
        Call<ListaVisitantes> call = listaVisitantesAPIs.listaVisitantes(Integer.toString(nPag),"10", authorization);
        call.enqueue(new Callback<ListaVisitantes>() {
            @Override
            public void onResponse(Call <ListaVisitantes> call, retrofit2.Response<ListaVisitantes> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    bar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    visitantes.remove(visitantes.size() - 1);
                    int scrollPosition = visitantes.size();
                    visitantesAdapter.notifyItemRemoved(scrollPosition);
                    ListaVisitantes listaVisitantes = response.body();
                    for(int i = 0 ; i < listaVisitantes.getlVisitante().size() ; i++)
                    {
                        visitantes.add(listaVisitantes.getlVisitante().get(i));
                    }
                    visitantesAdapter.notifyDataSetChanged();
                    isLoading = false;
                }

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
        Call<ListaVisitantes> call = listaVisitantesAPIs.listaVisitantes("0","10", authorization);
        call.enqueue(new Callback<ListaVisitantes>() {
            @Override
            public void onResponse(Call <ListaVisitantes> call, retrofit2.Response<ListaVisitantes> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
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
                        totalElements = listaVisitantes.getTotalElements();
                        visitantesAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    nPag = 0;
                    isLoading = false;
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
    protected void onResume() {
        super.onResume();
        //visitantes.clear();
    }

    private void buscarVisitanteXNombre() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitantesXNombreAPIs listaVisitantesXNombreAPIs = retrofit.create(ListaVisitantesXNombreAPIs.class);
        Call<ListaVisitantes> call = listaVisitantesXNombreAPIs.listaVisitanteXNombre(nombre,"0","10", authorization);
        call.enqueue(new Callback<ListaVisitantes>() {
            @Override
            public void onResponse(Call <ListaVisitantes> call, retrofit2.Response<ListaVisitantes> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    suggestions.clear();
                    ListaVisitantes listaVisitantes = response.body();
                    if(listaVisitantes.getlVisitante().size() == 0)
                    {
                        //tvNoData.setVisibility(View.VISIBLE);
                        String[] columns = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA};
                        MatrixCursor cursor = new MatrixCursor(columns);
                        suggestionAdapter.swapCursor(cursor);
                    }
                    else
                    {
                        //tvNoData.setVisibility(View.GONE);
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

        //suggestions = new ArrayList<>();

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
                String[] columns = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA};
                MatrixCursor cursor = new MatrixCursor(columns);
                suggestionAdapter.swapCursor(cursor);

                searchView.setQuery(suggestions.get(position).getVteNombre()+" "+suggestions.get(position).getVteApellidos(), true);
                searchView.clearFocus();

                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);

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
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                sugerenciaPress = true;
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sugerenciaPress = false;
                return true;
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
        showLoadingwDialog();
        enviarCorreoIngreso(visitante);
    }

    public void iniciarQRActivity(Visitante visitante)
    {
        Intent intent = new Intent(Visitantes.this, QRActivity.class);
        intent.putExtra("visitante", visitante);
        startActivity(intent);
    }

    public void showLoadingwDialog() {
        dialogFragment = new LoadingFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putInt("tiempo", 0);
        dialogFragment.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogLoading");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialogLoading");
    }

    private void enviarCorreoIngreso(final Visitante visitante) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        EnviarCorreoIAPIs enviarCorreoIAPIs = retrofit.create(EnviarCorreoIAPIs.class);
        Call call = enviarCorreoIAPIs.enviarCorreo(visitante.getVteCorreo(), authorization);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    if (response.body() != null) {
                        Toast.makeText(getApplicationContext(), "Se envió el correo de ingreso a "+visitante.getVteNombre() +" "+visitante.getVteApellidos(), Toast.LENGTH_LONG).show();
                    }
                    dialogFragment.dismiss();
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("msg45",""+t);
                Toast.makeText(getApplicationContext(), "Hubo un error al enviar el correo de ingreso a "+visitante.getVteNombre() +" "+visitante.getVteApellidos(), Toast.LENGTH_LONG).show();
                dialogFragment.dismiss();
            }
        });
    }

    public void showTknExpDialog() {
        DFTknExpired dfTknExpired = new DFTknExpired();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putInt("tiempo", 0);
        dfTknExpired.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogTknExpLoading");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dfTknExpired.show(ft, "dialogTknExpLoading");
    }
}
