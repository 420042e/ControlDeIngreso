package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.stbnlycan.adapters.HorariosAdapter;
import com.stbnlycan.interfaces.HorariosAPIs;
import com.stbnlycan.interfaces.TipoVisitanteAPIs;
import com.stbnlycan.models.Horario;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Horarios extends AppCompatActivity implements HorariosAdapter.OnVisitanteClickListener, Validator.ValidationListener {

    private List<Horario> horarios;
    private HorariosAdapter horariosAdapter;

    private List<TipoVisitante> tipoVisitantes;
    private ArrayAdapter<TipoVisitante> adapterTipoVisitante;

    @Select
    private Spinner tipoVisitanteS;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private Validator validator;
    private Toolbar toolbar;
    private Recinto recintoRecibido;
    private TipoVisitante tipoVisitanteSel;
    private final static int REQUEST_CODE_NH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        setTitle("Horarios");

        validator = new Validator(this);
        validator.setValidationListener(this);

        tipoVisitanteS = findViewById(R.id.tipoVisitante);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        emptyView = (TextView) findViewById(R.id.emptyView);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        horarios = new ArrayList<>();

        iniciarSpinnerTipoVisitante();

        fetchTipoVisitantes();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_nuevo_horario:
                validator.validate();
                //Log.d("msg", "nuevo horario");
                /*Intent intent = new Intent(Horarios.this, NuevoHorarioActivity.class);
                intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
                startActivity(intent);*/
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu3, menu);
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
                horariosAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    private void fetchHorarios(String recCod, String tviCod, String horNombre, String dia) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        HorariosAPIs horariosAPIs = retrofit.create(HorariosAPIs.class);
        Call <List<Horario>> call = horariosAPIs.listaHorarios(recCod,tviCod,horNombre,dia);
        call.enqueue(new Callback <List<Horario>> () {
            @Override
            public void onResponse(Call <List<Horario>> call, Response <List<Horario>> response) {
                horarios = response.body();
                horariosAdapter = new HorariosAdapter(horarios);
                horariosAdapter.setOnVisitanteClickListener(Horarios.this);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
                recyclerView.setAdapter(horariosAdapter);

                if (response.body().size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    @Override
    public void onEventoClick(Horario horario) {
        Log.d("msg",""+horario.getHorNombre());
    }

    public void iniciarSpinnerTipoVisitante() {
        tipoVisitantes = new ArrayList<>();
        TipoVisitante tipoVisitante = new TipoVisitante();
        tipoVisitante.setTviCod("cod");
        tipoVisitante.setTviNombre("Selecciona tipo de visitante");
        tipoVisitante.setTviDescripcion("obs");
        tipoVisitante.setHorEstado("estado");
        tipoVisitantes.add(tipoVisitante);
        adapterTipoVisitante = new ArrayAdapter<TipoVisitante>(this, android.R.layout.simple_spinner_dropdown_item, tipoVisitantes);
        adapterTipoVisitante.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoVisitanteS.setAdapter(adapterTipoVisitante);
    }

    private void displayTipoVisitanteData(TipoVisitante tipoVisitante) {
        String cod = tipoVisitante.getTviCod();
        String nombre = tipoVisitante.getTviNombre();
        String descripcion = tipoVisitante.getTviDescripcion();
        String estado = tipoVisitante.getHorEstado();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + descripcion + "\nEstado: " + estado;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
        //horarios.clear();
        //horariosAdapter.notifyDataSetChanged();
    }

    private void fetchTipoVisitantes() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        TipoVisitanteAPIs tipoVisitantesAPIs = retrofit.create(TipoVisitanteAPIs.class);
        Call<List<TipoVisitante>> call = tipoVisitantesAPIs.listaTipoVisitante();
        call.enqueue(new Callback<List<TipoVisitante>>() {
            @Override
            public void onResponse(Call <List<TipoVisitante>> call, Response<List<TipoVisitante>> response) {
                //tipoVisitantes = response.body();

                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    tipoVisitantes.add(response.body().get(i));
                }

                tipoVisitanteS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        tipoVisitanteSel = (TipoVisitante) parent.getSelectedItem();
                        displayTipoVisitanteData(tipoVisitanteSel);

                        fetchHorarios(recintoRecibido.getRecCod(), tipoVisitanteSel.getTviCod(),"todos","todos");
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NH) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Horario horarioResult = (Horario) b.getSerializable("horarioResult");
                    horarios.add(horarios.size(), horarioResult);
                    horariosAdapter.notifyItemInserted(horarios.size());
                    recyclerView.scrollToPosition(horarios.size());
                }
            }
        }
    }

    @Override
    public void onValidationSucceeded() {
        Intent intent = new Intent(Horarios.this, NuevoHorarioActivity.class);
        //intent.putExtra("recCod", getIntent().getStringExtra("recCod"));
        intent.putExtra("recinto", recintoRecibido);
        intent.putExtra("tipoVisitante", tipoVisitanteSel);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_CODE_NH);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            // Display error messages
            if (view instanceof EditText) {
                //((EditText) view).setError(message);
                ((EditText) view).setError("Este campo es requerido");
            }
            else if (view instanceof Spinner) {
                //((TextView) ((Spinner) view).getSelectedView()).setError(message);
                ((TextView) ((Spinner) view).getSelectedView()).setError("Este campo es requerido");
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
