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

import com.stbnlycan.adapters.UsuariosAdapter;
import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.interfaces.ListaUsuariosAPIs;
import com.stbnlycan.interfaces.ListaUsuariosXUsrNameAPIs;
import com.stbnlycan.interfaces.ListaVisitantesAPIs;
import com.stbnlycan.interfaces.ListaVisitantesXNombreAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.models.ListaUsuarios;
import com.stbnlycan.models.ListaVisitantes;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.Usuario;
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

public class Usuarios extends AppCompatActivity implements UsuariosAdapter.OnUsuarioClickListener {

    private ArrayList<Usuario> usuarios;
    private UsuariosAdapter usuariosAdapter;

    private int currentItems, totalItems, scrollOutItems;
    private boolean isScrolling = false;
    private LinearLayoutManager manager;
    private int nPag;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar bar;
    private TextView tvFallo;
    private TextView tvNoData;

    private SearchView searchView;
    private List<Usuario> suggestions;
    private CursorAdapter suggestionAdapter;

    private String nombre;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Recinto recintoRecibido;

    private String totalElements;

    private final static int REQUEST_CODE_NU = 1;
    private final static int REQUEST_CODE_EU = 2;

    private boolean sugerenciaPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        nPag = 0;

        setTitle("Usuarios");

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

        usuarios = new ArrayList<>();
        usuariosAdapter = new UsuariosAdapter(this, authorization, usuarios);
        usuariosAdapter.setOnUsuarioClickListener(Usuarios.this);

        suggestions = new ArrayList<>();

        recyclerView.setAdapter(usuariosAdapter);

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
                if(isScrolling && (currentItems + scrollOutItems == totalItems) && totalItems != Integer.parseInt(totalElements)  && !sugerenciaPress)
                {
                    isScrolling = false;
                    nPag++;
                    mostrarMasUsuarios();
                }
            }
        });

        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);

        actualizarUsuarios();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                //tvNoData.setVisibility(View.GONE);
                actualizarUsuarios();
            }
        });
    }

    @Override
    public void onEventoClick(Usuario usuario, int position) {
        Intent intent = new Intent(Usuarios.this, EditarUsuario.class);
        intent.putExtra("usuario", usuario);
        intent.putExtra("position", position);
        startActivityForResult(intent, REQUEST_CODE_EU);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuarios, menu);
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

                searchView.setQuery(suggestions.get(position).getUsername(), true);
                searchView.clearFocus();

                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);

                usuarios.clear();
                usuarios.add(suggestions.get(position));
                usuariosAdapter.notifyDataSetChanged();
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
                nombre = newText;
                if(newText.equals("")){
                    nPag = 0;
                    actualizarUsuarios();
                }
                else
                {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buscarUsuarioXUsrName();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_nuevo_usuario:
                Intent intent = new Intent(Usuarios.this, NuevoUsuario.class);
                intent.putExtra("recinto", recintoRecibido);
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_NU);
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
                Intent intentS = new Intent(Usuarios.this, LoginActivity.class);
                startActivity(intentS);
                finish();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

    private void actualizarUsuarios() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaUsuariosAPIs listaUsuariosAPIs = retrofit.create(ListaUsuariosAPIs.class);
        Call<ListaUsuarios> call = listaUsuariosAPIs.listaUsuarios("0","10", authorization);
        call.enqueue(new Callback<ListaUsuarios>() {
            @Override
            public void onResponse(Call <ListaUsuarios> call, retrofit2.Response<ListaUsuarios> response) {
                usuarios.clear();
                ListaUsuarios listaUsuarios = response.body();
                if(listaUsuarios.getlUsuario().size() == 0)
                {
                    tvNoData.setVisibility(View.VISIBLE);
                }
                else
                {
                    bar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaUsuarios.getlUsuario().size() ; i++)
                    {
                        usuarios.add(listaUsuarios.getlUsuario().get(i));
                    }
                    totalElements = listaUsuarios.getTotalElements();
                    usuariosAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
                nPag = 0;
            }
            @Override
            public void onFailure(Call <ListaUsuarios> call, Throwable t) {
                tvFallo.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void mostrarMasUsuarios() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaUsuariosAPIs listaUsuariosAPIs = retrofit.create(ListaUsuariosAPIs.class);
        Call<ListaUsuarios> call = listaUsuariosAPIs.listaUsuarios(Integer.toString(nPag),"10", authorization);
        call.enqueue(new Callback<ListaUsuarios>() {
            @Override
            public void onResponse(Call <ListaUsuarios> call, retrofit2.Response<ListaUsuarios> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ListaUsuarios listaUsuarios = response.body();
                for(int i = 0 ; i < listaUsuarios.getlUsuario().size() ; i++)
                {
                    usuarios.add(listaUsuarios.getlUsuario().get(i));
                }
                usuariosAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call <ListaUsuarios> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });

    }

    private void buscarUsuarioXUsrName() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaUsuariosXUsrNameAPIs listaUsuariosXUsrNameAPIs = retrofit.create(ListaUsuariosXUsrNameAPIs.class);
        Call<ListaUsuarios> call = listaUsuariosXUsrNameAPIs.listaVisitantesXUsrName(nombre,"0","10", authorization);
        call.enqueue(new Callback<ListaUsuarios>() {
            @Override
            public void onResponse(Call <ListaUsuarios> call, retrofit2.Response<ListaUsuarios> response) {
                suggestions.clear();
                ListaUsuarios listaUsuarios = response.body();
                if(listaUsuarios.getlUsuario().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                    String[] columns = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA};
                    MatrixCursor cursor = new MatrixCursor(columns);
                    suggestionAdapter.swapCursor(cursor);
                }
                else
                {
                    //tvNoData.setVisibility(View.GONE);
                    for(int i = 0 ; i < listaUsuarios.getlUsuario().size() ; i++)
                    {
                        suggestions.add(listaUsuarios.getlUsuario().get(i));
                        String[] columns = { BaseColumns._ID,
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                        };
                        MatrixCursor cursor = new MatrixCursor(columns);
                        for (int j = 0; j < suggestions.size(); j++) {
                            String[] tmp = {Integer.toString(j), suggestions.get(j).getUsername(), suggestions.get(j).getUsername()};
                            cursor.addRow(tmp);
                        }
                        suggestionAdapter.swapCursor(cursor);
                    }
                }
            }
            @Override
            public void onFailure(Call <ListaUsuarios> call, Throwable t) {
                tvFallo.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NU) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Usuario usuarioResult = (Usuario) b.getSerializable("usuarioResult");
                    usuarios.add(0, usuarioResult);
                    usuariosAdapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                }
            }
            else if (requestCode == REQUEST_CODE_EU) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Usuario usuarioResult = (Usuario) b.getSerializable("usuarioResult");
                    int position = b.getInt("position", -1);
                    usuarios.set(position, usuarioResult);
                    usuariosAdapter.notifyItemChanged(position);
                    recyclerView.scrollToPosition(position);
                }
            }
        }
    }
}
