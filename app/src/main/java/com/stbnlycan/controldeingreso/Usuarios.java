package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stbnlycan.adapters.UsuariosAdapter;
import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.interfaces.LogoutAPIs;
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
    private List<Visitante> suggestions;
    private CursorAdapter suggestionAdapter;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Recinto recintoRecibido;

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
        usuariosAdapter = new UsuariosAdapter(this, client, usuarios);
        usuariosAdapter.setOnUsuarioClickListener(Usuarios.this);

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
                if(isScrolling && (currentItems + scrollOutItems == totalItems))
                {
                    isScrolling = false;
                    nPag++;
                    //mostrarMasUsuarios();
                }
            }
        });

        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);

        //actualizarUsuarios();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                //tvNoData.setVisibility(View.GONE);
                //actualizarUsuarios();
            }
        });
    }

    @Override
    public void onEventoClick(Usuario visitante, int position) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuarios, menu);
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
                startActivity(intent);
                //startActivityForResult(intent, REQUEST_CODE_NV);
                return false;
            case R.id.action_salir:
                cerrarSesion();
                Intent intentS = new Intent(Usuarios.this, LoginActivity.class);
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
}
