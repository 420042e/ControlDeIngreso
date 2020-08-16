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
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
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

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.Gson;
import com.stbnlycan.adapters.VisitasAdapter;
import com.stbnlycan.interfaces.AreaRecintoAPIs;
import com.stbnlycan.interfaces.ListaVCSalidaAPIs;
import com.stbnlycan.interfaces.ListaVSSalidaAPIs;
import com.stbnlycan.interfaces.ListaVisitaXCiAPIs;
import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.Empresa;
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

public class Visitas extends AppCompatActivity implements VisitasAdapter.OnVisitanteClickListener{

    private Toolbar toolbar;
    private Recinto recintoRecibido;

    private ProgressBar bar;
    private TextView tvFallo;
    private TextView tvNoData;
    private RecyclerView recyclerView;

    private SearchView searchView;
    private List<Visita> suggestions;
    private CursorAdapter suggestionAdapter;

    private ArrayList<Visita> visitas;
    private VisitasAdapter visitasAdapter;
    private String ci;
    private TextView tvTotalVisitantes;

    private String fechaIni;
    private String fechaFin;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager manager;
    private int nPag;

    private MaterialDatePicker<Pair<Long, Long>> picker;
    private EditText fechaRango;
    private AreaRecinto areaRecintoSel;
    private Spinner areaRecintoS;
    private Spinner tipoVisitaS;
    private ArrayList<AreaRecinto> areaRecinto;
    private ArrayAdapter<AreaRecinto> adapterAreaR;
    private String recintoSel;
    private int tipoVisitaSel;
    private int currentItems, totalItems, scrollOutItems;
    private boolean isScrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitas);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        setTitle("Visitas");

        toolbar = findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        tvNoData = (TextView) findViewById(R.id.tvNoData);
        tvTotalVisitantes = (TextView) findViewById(R.id.tvTotalVisitantes);
        areaRecintoS = (Spinner) findViewById(R.id.area_recinto);
        tipoVisitaS = (Spinner) findViewById(R.id.tipoVisita);
        fechaRango = (EditText) findViewById(R.id.fechaRango);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        fechaIni = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        fechaFin = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        /*fechaIni = "14-07-2020";
        fechaFin = "14-08-2020";*/
        fechaRango.setText(fechaIni.replace("-","/") + " - " + fechaFin.replace("-","/"));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        manager = new LinearLayoutManager(this);

        visitas = new ArrayList<>();
        visitasAdapter = new VisitasAdapter(visitas);
        recyclerView.setAdapter(visitasAdapter);
        //recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setLayoutManager(manager);
        recyclerView.setVisibility(View.GONE);

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
                    Log.d("msg314",""+nPag);
                    if(tipoVisitaSel == 1)
                    {
                        mostrarMasVCS();
                    }
                    else if(tipoVisitaSel == 2)
                    {
                        mostrarMasVSS();
                    }
                }
            }
        });

        bar.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        tvFallo.setVisibility(View.GONE);

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
                    if(tipoVisitaSel == 1)
                    {
                        actualizarVCS();
                    }
                    else if(tipoVisitaSel == 2)
                    {
                        actualizarVSS();
                    }
                }
            }
        });
        fechaRango.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });

        recintoSel = recintoRecibido.getRecCod();

        iniciarSpinnerArea();
        fetchAreaRecintos();

        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("Selecciona tipo de visita");
        spinnerArray.add("Visitantes con salida");
        spinnerArray.add("Visitantes sin salida");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoVisitaS.setAdapter(adapter);
        tipoVisitaS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AreaRecinto areaRecinto = (AreaRecinto) areaRecintoS.getSelectedItem();
                recyclerView.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                tvFallo.setVisibility(View.GONE);
                if(!(areaRecinto.getAreaCod().equals("cod")))
                {
                    if(position == 1)
                    {
                        tipoVisitaSel = 1;
                        actualizarVCS();
                    }
                    else if(position == 2)
                    {
                        tipoVisitaSel = 2;
                        actualizarVSS();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tipoVisitaS.setSelection(1);
    }

    public void iniciarSpinnerArea() {
        areaRecinto = new ArrayList<>();

        AreaRecinto area = new AreaRecinto();
        area.setAreaCod("cod");
        area.setAreaNombre("Selecciona area del recinto");
        area.setAreaDescripcion("descripcion");
        area.setAreaEstado("estado");

        AreaRecinto area2 = new AreaRecinto();
        area2.setAreaCod("0");
        area2.setAreaNombre("TODOS");
        area2.setAreaDescripcion("descripcion");
        area2.setAreaEstado("estado");

        areaRecinto.add(area);
        areaRecinto.add(area2);
        adapterAreaR = new ArrayAdapter<AreaRecinto>(this, android.R.layout.simple_spinner_dropdown_item, areaRecinto);
        adapterAreaR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaRecintoS.setAdapter(adapterAreaR);
        areaRecintoS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                areaRecintoSel = (AreaRecinto) areaRecintoS.getSelectedItem();
                recyclerView.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                tvFallo.setVisibility(View.GONE);
                if(!(areaRecintoSel.getAreaCod().equals("cod")))
                {
                    if(tipoVisitaSel == 1)
                    {
                        actualizarVCS();
                    }
                    else if(tipoVisitaSel == 2)
                    {
                        actualizarVSS();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        areaRecintoS.setSelection(1);
        areaRecintoSel = (AreaRecinto) areaRecintoS.getSelectedItem();
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
                    //areaRecintoS.setSelection(1);
                    tipoVisitaSel = 1;
                }
            }
            @Override
            public void onFailure(Call <List<AreaRecinto>> call, Throwable t) {

            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();


        // Solution
        int autoCompleteTextViewID = getResources().getIdentifier("search_src_text", "id", getPackageName());
        AutoCompleteTextView searchAutoCompleteTextView = (AutoCompleteTextView) searchView.findViewById(autoCompleteTextViewID);
        searchAutoCompleteTextView.setThreshold(1);

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

                Gson gson = new Gson();
                String descripcion = gson.toJson(suggestions.get(position));
                Log.d("msg476",""+descripcion);


                /*for(int i = 0 ; i < areaRecinto.size() ; i++)
                {
                    if(suggestions.get(position).getAreaRecinto().getAreaCod().equals(areaRecinto.get(i).getAreaCod()))
                    {
                        areaRecintoS.setSelection(i);
                    }
                }*/

                visitas.clear();
                visitas.add(suggestions.get(position));
                visitasAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                return true;
            }
        });



        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Ingresar CI");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //ci = query;
                //buscarVisitaXCi2();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /*if(newText.equals("")){
                    //this.onQueryTextSubmit("");
                    visitas.clear();
                    nPag = 0;
                    fetchVisitas();
                }*/
                if(newText.length() > 4)
                {
                    ci = newText;
                    buscarVisitaXCi2();
                }
                /*ci = newText;
                buscarVisitaXCi2();*/
                return false;
            }
        });
        return true;
    }

    private void buscarVisitaXCi2() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVisitaXCiAPIs listaVisitaXCiAPIs = retrofit.create(ListaVisitaXCiAPIs.class);
        Call<ListaVisitas> call = listaVisitaXCiAPIs.listaVisitaXCi(ci, fechaIni, fechaFin,"0","10");
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
                                SearchManager.SUGGEST_COLUMN_TEXT_1
                        };
                        MatrixCursor cursor = new MatrixCursor(columns);
                        for (int j = 0; j < suggestions.size(); j++) {
                            String[] tmp = {Integer.toString(j), suggestions.get(j).getVisitante().getVteNombre() + " " + suggestions.get(j).getVisitante().getVteApellidos()};
                            cursor.addRow(tmp);
                        }
                        suggestionAdapter.swapCursor(cursor);
                        //visitas.add(listaVisitas.getlVisita().get(i));
                    }
                    //visitasAdapter.notifyDataSetChanged();
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
    public void onEventoClick(Visita Visita, int position) {

    }

    private void actualizarVCS() {
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

    private void mostrarMasVCS() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVSSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVSSalidaAPIs.class);
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVSSalida(fechaIni, fechaFin, recintoRecibido.getRecCod(), areaRecintoSel.getAreaCod(), Integer.toString(nPag),"10");
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

    private void actualizarVSS() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVCSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVCSalidaAPIs.class);
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVCSalida(fechaIni, fechaFin, recintoSel, areaRecintoSel.getAreaCod(),"0","10");
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

    private void mostrarMasVSS() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaVCSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVCSalidaAPIs.class);
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVCSalida(fechaIni, fechaFin, recintoRecibido.getRecCod(), areaRecintoSel.getAreaCod(), Integer.toString(nPag),"10");
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
}
