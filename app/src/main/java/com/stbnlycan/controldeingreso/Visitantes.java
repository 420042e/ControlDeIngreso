package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.stbnlycan.adapters.RecintosAdapter;
import com.stbnlycan.adapters.VisitanteListAdapter;
import com.stbnlycan.adapters.VisitantesAdapter;
import com.stbnlycan.models.AreaR;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Visitantes extends AppCompatActivity implements VisitantesAdapter.OnVisitanteClickListener {

    ArrayList<Visitante> visitantes;
    ListView listaVisitantes;
    //VisitanteListAdapter adapter;
    VisitantesAdapter visitantesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitantes);

        setTitle("Visitantes");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //listaVisitantes = findViewById(R.id.visitantes);

        visitantes = new ArrayList<>();

        /*adapter = new VisitanteListAdapter(this, R.layout.adapter_view_layout, visitantes);
        adapter.setOnItemClickListener(Visitantes.this);
        listaVisitantes.setAdapter(adapter);*/


        /*RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        visitantesAdapter = new VisitantesAdapter(visitantes);
        visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(visitantesAdapter);*/


        getDataVisitante();
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

    private void getDataVisitante() {
        String url = "http://172.16.0.22:8080/ingresoVisitantes/visitante/lista";
        JsonArrayRequest jsonArrayRequest  = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), "hola "+response.toString(), Toast.LENGTH_LONG).show();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                Visitante visitante = new Visitante();
                                visitante.setVteCi(jsonObject.getString("vteCi"));
                                visitante.setVteCorreo(jsonObject.getString("vteCorreo"));
                                visitante.setVteNombre(jsonObject.getString("vteNombre"));
                                visitante.setVteApellidos(jsonObject.getString("vteApellidos"));
                                visitante.setVteTelefono(jsonObject.getString("vteTelefono"));
                                visitante.setVteDireccion(jsonObject.getString("vteDireccion"));
                                visitante.setVteEstado(jsonObject.getString("vteEstado"));
                                visitante.setVteLlave(jsonObject.getString("vteLlave"));
                                visitante.setVteFecha(jsonObject.getString("vteFecha"));
                                visitante.setVteFecha(jsonObject.getString("vteFecha"));

                                JSONObject jsonObjectTV = new JSONObject(jsonObject.getString("tipoVisitante"));
                                TipoVisitante tipoVisitante = new TipoVisitante();
                                tipoVisitante.setTviCod(jsonObjectTV.getString("tviCod"));
                                tipoVisitante.setTviNombre(jsonObjectTV.getString("tviNombre"));
                                tipoVisitante.setTviDescripcion(jsonObjectTV.getString("tviDescripcion"));
                                tipoVisitante.setHorEstado(jsonObjectTV.getString("horEstado"));

                                JSONObject jsonObjectE = new JSONObject(jsonObject.getString("empresa"));
                                Empresa empresa = new Empresa();
                                empresa.setEmpCod(jsonObjectE.getString("empCod"));
                                empresa.setEmpNombre(jsonObjectE.getString("empNombre"));
                                empresa.setEmpObs(jsonObjectE.getString("empObs"));

                                visitante.setTipoVisitante(tipoVisitante);
                                visitante.setEmpresa(empresa);

                                visitantes.add(visitante);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                        visitantesAdapter = new VisitantesAdapter(visitantes);
                        visitantesAdapter.setOnVisitanteClickListener(Visitantes.this);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
                        recyclerView.setAdapter(visitantesAdapter);

                        //visitantesAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getApplicationContext(), "error "+error, Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //visitantes.clear();


        //getDataVisitante();
        //Toast.makeText(this, "Hola", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                visitantesAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onEventoClick(Visitante visitante) {
        Intent intent = new Intent(Visitantes.this, EditarVisitanteActivity.class);
        intent.putExtra("Visitante", visitante);
        startActivity(intent);
    }
}
