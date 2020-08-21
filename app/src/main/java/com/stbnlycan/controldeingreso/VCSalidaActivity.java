package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.stbnlycan.adapters.VisitasAdapter;
import com.stbnlycan.interfaces.AreaRecintoAPIs;
import com.stbnlycan.interfaces.ListaVSSalidaAPIs;
import com.stbnlycan.interfaces.ListaVisitaXCiAPIs;
import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.ListaVisitas;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class VCSalidaActivity extends AppCompatActivity implements VisitasAdapter.OnVisitanteClickListener {

    private Recinto recintoRecibido;
    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private ArrayList<Visita> visitas;
    private VisitasAdapter visitasAdapter;

    private ProgressBar bar;
    private TextView tvFallo;
    private TextView tvNoData;

    private ArrayList<AreaRecinto> areaRecinto;
    private ArrayAdapter<AreaRecinto> adapterAreaR;
    private Spinner areaRecintoS;
    private MaterialDatePicker<Pair<Long, Long>> picker;

    private AreaRecinto areaRecintoSel;
    private String fechaIni;
    private String fechaFin;
    private String recintoSel;
    private String areaRecintoSelect;

    private EditText fechaRango;
    private int currentItems, totalItems, scrollOutItems;
    private boolean isScrolling = false;
    private LinearLayoutManager manager;
    private int nPag;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String ci;
    private TextView tvTotalVisitantes;

    private SearchView searchView;
    private List<Visita> suggestions;
    private CursorAdapter suggestionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_c_salida);

        fechaRango = (EditText) findViewById(R.id.fechaRango);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        fechaIni = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        fechaFin = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        recintoSel = recintoRecibido.getRecCod();
        areaRecintoSelect = "1";

        nPag = 0;

        fechaRango.setText(fechaIni.replace("-","/") + " - " + fechaFin.replace("-","/"));

        setTitle("Visitas con salidas");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        areaRecintoS = findViewById(R.id.area_recinto);

        visitas = new ArrayList<>();

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
                    mostrarMasVisitas();
                }
            }
        });

        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        tvNoData = (TextView) findViewById(R.id.tvNoData);
        tvTotalVisitantes = (TextView) findViewById(R.id.tvTotalVisitantes);

        bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);

        iniciarSpinnerArea();
        fetchAreaRecintos();

        fetchVisitas();

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        builder.setTitleText("Selecciona un rango de fechas");
        builder.setCalendarConstraints(constraintsBuilder.build());
        picker = builder.build();

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                Long startDate = selection.first;
                Long endDate = selection.second;

                TimeZone timeZoneUTC = TimeZone.getDefault();
                int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;

                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                Date date1 = new Date(startDate + offsetFromUTC);
                Date date2 = new Date(endDate + offsetFromUTC);

                AreaRecinto areaRecinto = (AreaRecinto) areaRecintoS.getSelectedItem();

                fechaIni = simpleFormat.format(date1);
                fechaFin = simpleFormat.format(date2);

                fechaRango.setText(fechaIni.replace("-","/") + " - " + fechaFin.replace("-","/"));
                recyclerView.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);

                if(!(areaRecinto.getAreaCod().equals("cod")))
                {
                    actualizarVisitas();
                }
            }
        });

        fechaRango.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tvFallo.setVisibility(View.GONE);
                actualizarVisitas();
            }
        });
    }

    public void iniciarSpinnerArea() {
        areaRecinto = new ArrayList<>();

        AreaRecinto area = new AreaRecinto();
        area.setAreaCod("cod");
        area.setAreaNombre("Selecciona area del recinto");
        area.setAreaDescripcion("descripcion");
        area.setAreaEstado("estado");

        areaRecinto.add(area);
        adapterAreaR = new ArrayAdapter<AreaRecinto>(this, android.R.layout.simple_spinner_dropdown_item, areaRecinto);
        adapterAreaR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaRecintoS.setAdapter(adapterAreaR);
        areaRecintoS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AreaRecinto areaRecinto = (AreaRecinto) parent.getSelectedItem();
                areaRecintoSel = (AreaRecinto) parent.getSelectedItem();
                displayAreaRData(areaRecinto);
                //Toast.makeText(getApplicationContext(), ""+areaRecinto.getAreaCod(), Toast.LENGTH_LONG).show();
                recyclerView.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);
                if(!(areaRecinto.getAreaCod().equals("cod")))
                {
                    actualizarVisitas();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchAreaRecintos() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        AreaRecintoAPIs areaRecintoAPIs = retrofit.create(AreaRecintoAPIs.class);
        Call<List<AreaRecinto>> call = areaRecintoAPIs.listaPorRecinto(recintoRecibido.getRecCod());
        call.enqueue(new Callback<List<AreaRecinto>>() {
            @Override
            public void onResponse(Call <List<AreaRecinto>> call, retrofit2.Response<List<AreaRecinto>> response) {
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    areaRecinto.add(response.body().get(i));
                }
                if(response.body().size() > 0)
                {
                    areaRecintoS.setSelection(1);
                }
            }
            @Override
            public void onFailure(Call <List<AreaRecinto>> call, Throwable t) {

            }
        });
    }

    private void displayAreaRData(AreaRecinto areaRecinto) {
        String cod = areaRecinto.getAreaCod();
        String nombre = areaRecinto.getAreaNombre();
        String descripcion = areaRecinto.getAreaDescripcion();
        String estado = areaRecinto.getAreaEstado();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + descripcion + "\nEstado: " + estado;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
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
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVSSalida(fechaIni, fechaFin, recintoRecibido.getRecCod(), "1",Integer.toString(nPag),"10");
        call.enqueue(new Callback<ListaVisitas>() {
            @Override
            public void onResponse(Call <ListaVisitas> call, retrofit2.Response<ListaVisitas> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ListaVisitas listaVisitas = response.body();
                if(listaVisitas.getlVisita().size() == 0)
                {
                    tvNoData.setVisibility(View.VISIBLE);
                    tvTotalVisitantes.setText("Total de visitas: 0");
                    visitasAdapter = new VisitasAdapter(visitas);
                }
                else
                {
                    tvNoData.setVisibility(View.GONE);
                    tvTotalVisitantes.setText("Total de visitas: " + listaVisitas.getTotalElements());
                    for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                    {
                        visitas.add(listaVisitas.getlVisita().get(i));
                    }
                    visitasAdapter = new VisitasAdapter(visitas);
                    visitasAdapter.setOnVisitanteClickListener(VCSalidaActivity.this);

                    recyclerView.setAdapter(visitasAdapter);
                }
            }
            @Override
            public void onFailure(Call <ListaVisitas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarMasVisitas() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVSSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVSSalidaAPIs.class);
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVSSalida(fechaIni, fechaFin, recintoRecibido.getRecCod(), areaRecintoSel.getAreaCod(),Integer.toString(nPag),"10");
        call.enqueue(new Callback<ListaVisitas>() {
            @Override
            public void onResponse(Call <ListaVisitas> call, retrofit2.Response<ListaVisitas> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ListaVisitas listaVisitas = response.body();
                for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                {
                    visitas.add(listaVisitas.getlVisita().get(i));
                }
                visitasAdapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call <ListaVisitas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEventoClick(Visita Visita, int position) {

    }

    private void actualizarVisitas() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVSSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVSSalidaAPIs.class);
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVSSalida(fechaIni, fechaFin, recintoSel, areaRecintoSel.getAreaCod(),"0","10");
        call.enqueue(new Callback<ListaVisitas>() {
            @Override
            public void onResponse(Call <ListaVisitas> call, retrofit2.Response<ListaVisitas> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                visitas.clear();
                ListaVisitas listaVisitas = response.body();
                if(listaVisitas.getlVisita().size() == 0)
                {
                    tvNoData.setVisibility(View.VISIBLE);
                    tvTotalVisitantes.setText("Total de visitas: 0");
                }
                else {
                    tvNoData.setVisibility(View.GONE);
                    tvTotalVisitantes.setText("Total de visitas: " + listaVisitas.getTotalElements());
                    for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                    {
                        visitas.add(listaVisitas.getlVisita().get(i));
                    }
                    visitasAdapter = new VisitasAdapter(visitas);
                    visitasAdapter.setOnVisitanteClickListener(VCSalidaActivity.this);

                    recyclerView.setAdapter(visitasAdapter);

                    visitasAdapter.notifyDataSetChanged();
                }

                swipeRefreshLayout.setRefreshing(false);
                nPag = 0;
            }
            @Override
            public void onFailure(Call <ListaVisitas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void buscarVisitaXCi() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitaXCiAPIs listaVisitaXCiAPIs = retrofit.create(ListaVisitaXCiAPIs.class);
        Call<ListaVisitas> call = listaVisitaXCiAPIs.listaVisitaXCi(ci, fechaIni, fechaFin, recintoRecibido.getRecCod(), areaRecintoSel.getAreaCod(), "true","0","10");
        call.enqueue(new Callback<ListaVisitas>() {
            @Override
            public void onResponse(Call <ListaVisitas> call, retrofit2.Response<ListaVisitas> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                visitas.clear();
                ListaVisitas listaVisitas = response.body();
                if(listaVisitas.getlVisita().size() == 0)
                {
                    tvNoData.setVisibility(View.VISIBLE);
                    tvTotalVisitantes.setText("Total de visitas: 0");
                }
                else {
                    tvNoData.setVisibility(View.GONE);
                    tvTotalVisitantes.setText("Total de visitas: " + listaVisitas.getTotalElements());
                    for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                    {
                        visitas.add(listaVisitas.getlVisita().get(i));
                    }
                    visitasAdapter = new VisitasAdapter(visitas);
                    visitasAdapter.setOnVisitanteClickListener(VCSalidaActivity.this);

                    recyclerView.setAdapter(visitasAdapter);

                    visitasAdapter.notifyDataSetChanged();
                }

                swipeRefreshLayout.setRefreshing(false);
                nPag = 0;
            }
            @Override
            public void onFailure(Call <ListaVisitas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void buscarVisitaXCi2() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitaXCiAPIs listaVisitaXCiAPIs = retrofit.create(ListaVisitaXCiAPIs.class);
        Call<ListaVisitas> call = listaVisitaXCiAPIs.listaVisitaXCi(ci, fechaIni, fechaFin, recintoRecibido.getRecCod(), areaRecintoSel.getAreaCod(), "true","0","10");
        call.enqueue(new Callback<ListaVisitas>() {
            @Override
            public void onResponse(Call <ListaVisitas> call, retrofit2.Response<ListaVisitas> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                //visitas.clear();
                suggestions.clear();
                ListaVisitas listaVisitas = response.body();
                if(listaVisitas.getlVisita().size() == 0)
                {
                    tvNoData.setVisibility(View.VISIBLE);
                    tvTotalVisitantes.setText("Total de visitas: 0");
                }
                else {
                    tvNoData.setVisibility(View.GONE);
                    tvTotalVisitantes.setText("Total de visitas: " + listaVisitas.getTotalElements());

                    for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                    {
                        suggestions.add(listaVisitas.getlVisita().get(i));
                        String[] columns = { BaseColumns._ID,
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                        };
                        MatrixCursor cursor = new MatrixCursor(columns);
                        for (int j = 0; j < suggestions.size(); j++) {
                            String[] tmp = {Integer.toString(j), suggestions.get(j).getVisitante().getVteNombre() + " " + suggestions.get(j).getVisitante().getVteApellidos(), suggestions.get(j).getVisitante().getVteNombre()};
                            cursor.addRow(tmp);
                        }
                        suggestionAdapter.swapCursor(cursor);
                    }


                    /*for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                    {
                        visitas.add(listaVisitas.getlVisita().get(i));
                    }
                    visitasAdapter = new VisitasAdapter(visitas);
                    visitasAdapter.setOnVisitanteClickListener(VCSalidaActivity.this);

                    recyclerView.setAdapter(visitasAdapter);

                    visitasAdapter.notifyDataSetChanged();*/
                }

                swipeRefreshLayout.setRefreshing(false);
                nPag = 0;
            }
            @Override
            public void onFailure(Call <ListaVisitas> call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
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
                searchView.setQuery(suggestions.get(position).getVisitante().getVteCi(), true);
                searchView.clearFocus();

                visitas.clear();
                visitas.add(suggestions.get(position));
                visitasAdapter.notifyDataSetChanged();

                for(int i = 0; i < areaRecinto.size() ; i++)
                {
                    if(areaRecinto.get(i).getAreaCod().equals(suggestions.get(position).getAreaRecinto().getAreaCod()))
                    {
                        areaRecintoS.setSelection(i);
                    }
                }
                return true;
            }
        });


        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Ingresar CI");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ci = query;
                //buscarVisitaXCi();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    //this.onQueryTextSubmit("");
                    visitas.clear();
                    nPag = 0;
                    fetchVisitas();
                }
                ci = newText;
                buscarVisitaXCi2();
                return false;
            }
        });
        return true;
    }

}
