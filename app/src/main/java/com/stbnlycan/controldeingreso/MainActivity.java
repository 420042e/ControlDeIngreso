package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.stbnlycan.adapters.RecintosAdapter;
import com.stbnlycan.models.Recinto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecintosAdapter.OnEventoClickListener{

    private RecyclerView recyclerView;
    private RecintosAdapter adapter;
    private List<Recinto> areas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        areas = new ArrayList<>();
        /*areas.add(new Recinto(0, "Recinto 1", "Descripcion 1", "1"));
        areas.add(new Recinto(1, "Recinto 2", "Descripcion 2", "1"));
        areas.add(new Recinto(2, "Recinto 3", "Descripcion 3", "1"));
        areas.add(new Recinto(3, "Recinto 4", "Descripcion 4", "1"));
        areas.add(new Recinto(4, "Recinto 5", "Descripcion 5", "0"));
        areas.add(new Recinto(5, "Recinto 6", "Descripcion 6", "0"));
        areas.add(new Recinto(6, "Recinto 7", "Descripcion 7", "1"));
        areas.add(new Recinto(7, "Recinto 8", "Descripcion 8", "1"));
        areas.add(new Recinto(8, "Recinto 9", "Descripcion 9", "1"));
        areas.add(new Recinto(9, "Recinto 10", "Descripcion 10", "0"));
        areas.add(new Recinto(10, "Recinto 11", "Descripcion 11", "0"));
        areas.add(new Recinto(11, "Recinto 12", "Descripcion 12", "0"));*/







        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new RecintosAdapter(areas);
        adapter.setOnEventoClickListener(MainActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);

        getData();
    }

    private void getData() {
        String url = "http://172.16.0.22:8080/ingresoVisitantes/recinto/lista";
        JsonArrayRequest jsonArrayRequest  = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), "hola "+response.toString(), Toast.LENGTH_LONG).show();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                Recinto recinto = new Recinto();
                                recinto.setRecCod(jsonObject.getString("recCod"));
                                recinto.setRecNombre(jsonObject.getString("recNombre"));
                                recinto.setRecNombrea(jsonObject.getString("recNombrea"));
                                recinto.setRecEstado(jsonObject.getString("recEstado"));
                                recinto.setRecTipo(jsonObject.getString("recTipo"));
                                recinto.setAduana(null);
                                areas.add(recinto);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
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
    public void onEventoClick(Recinto areaRecinto) {
        Log.d("Recinto",""+areaRecinto.getRecNombrea());

        Intent intent = new Intent(MainActivity.this, RecintoActivity.class);
        intent.putExtra("recCod",areaRecinto.getRecCod());
        intent.putExtra("titulo",areaRecinto.getRecNombre());
        startActivity(intent);
        //finish();
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
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}
