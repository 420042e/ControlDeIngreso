package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stbnlycan.adapters.DOI2Adapter;
import com.stbnlycan.adapters.DOIAdapter;
import com.stbnlycan.fragments.DFTknExpired;
import com.stbnlycan.interfaces.DocIngsAPIs;
import com.stbnlycan.interfaces.ListaVisitantesAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.models.DocumentoIngreso;
import com.stbnlycan.models.ListaVisitantes;
import com.stbnlycan.models.Visita;

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

public class VerDocumentosIngreso extends AppCompatActivity implements DOI2Adapter.OnDOIClickListener, DOI2Adapter.OnDOIQRClickListener {

    private Toolbar toolbar;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerView;
    private DOI2Adapter doiAdapter;
    private ArrayList<DocumentoIngreso> dois;
    private Visita visitaRecibida;
    private ProgressBar bar;
    private TextView tvFallo;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_documentos_ingreso);

        setTitle("Documentos de ingreso");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitaRecibida = (Visita) getIntent().getSerializableExtra("visita");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        tvNoData = (TextView) findViewById(R.id.tvNoData);

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

        dois = new ArrayList<>();
        doiAdapter = new DOI2Adapter(dois);
        doiAdapter.setContext(this);
        doiAdapter.setClient(client);
        doiAdapter.setAuthorization(authorization);
        doiAdapter.setOnDOIClickListener(VerDocumentosIngreso.this);
        doiAdapter.setOnDOIQRClickListener(VerDocumentosIngreso.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(doiAdapter);

        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);

        actualizarVisitantes(visitaRecibida.getVisCod());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dois, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                editor.putString("access_token", "");
                editor.putString("token_type", "");
                editor.putString("rol", "");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Sesión finalizada", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("msg4125", "hola " + t.toString());
            }
        });
    }

    private void actualizarVisitantes(String visCod) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        DocIngsAPIs docIngsAPIs = retrofit.create(DocIngsAPIs.class);
        Call<List<DocumentoIngreso>> call = docIngsAPIs.listaDocIngs(visCod, authorization);
        call.enqueue(new Callback<List<DocumentoIngreso>>() {
            @Override
            public void onResponse(Call <List<DocumentoIngreso>> call, retrofit2.Response<List<DocumentoIngreso>> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    dois.clear();
                    List<DocumentoIngreso> docIngs = response.body();
                    if(docIngs.size() == 0)
                    {
                        bar.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        bar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                        for(int i = 0 ; i < docIngs.size() ; i++)
                        {
                            dois.add(docIngs.get(i));
                        }
                        doiAdapter.notifyDataSetChanged();
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
            @Override
            public void onFailure(Call <List<DocumentoIngreso>> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void OnDOIClick(DocumentoIngreso doi) {
        Intent intent = new Intent(VerDocumentosIngreso.this, Foto.class);
        intent.putExtra("doi", doi);
        //startActivityForResult(intent, REQUEST_CODE_DOI);
        startActivity(intent);
    }

    @Override
    public void OnDOIQRClick(DocumentoIngreso doi) {
        Intent intent = new Intent(VerDocumentosIngreso.this, DoiQR.class);
        intent.putExtra("doi", doi);
        startActivity(intent);
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
