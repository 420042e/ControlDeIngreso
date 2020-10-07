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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.stbnlycan.adapters.HorariosAdapter;
import com.stbnlycan.interfaces.HorariosAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
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
    private Validator validator;
    private Toolbar toolbar;
    private Recinto recintoRecibido;
    private TipoVisitante tipoVisitanteSel;
    private final static int REQUEST_CODE_NH = 1;

    private ProgressBar bar;
    private TextView tvFallo;
    private TextView tvNoData;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

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
        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvFallo = (TextView) findViewById(R.id.tvFallo);
        tvNoData = (TextView) findViewById(R.id.tvNoData);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        horarios = new ArrayList<>();

        horariosAdapter = new HorariosAdapter(horarios);
        horariosAdapter.setOnVisitanteClickListener(Horarios.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        recyclerView.setAdapter(horariosAdapter);

        iniciarSpinnerTipoVisitante();
        fetchTipoVisitantes();
        tipoVisitanteS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoVisitanteSel = (TipoVisitante) tipoVisitanteS.getSelectedItem();
                displayTipoVisitanteData(tipoVisitanteSel);
                recyclerView.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                tvFallo.setVisibility(View.GONE);
                if(!(tipoVisitanteSel.getTviCod().equals("cod")))
                {
                    actualizarHorarios(recintoRecibido.getRecCod(), tipoVisitanteSel.getTviCod(),"todos","todos");
                }
                else
                {
                    recyclerView.setVisibility(View.GONE);
                    bar.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.GONE);
                    tvFallo.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                Intent intent = new Intent(Horarios.this, LoginActivity.class);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu3, menu);

        if(rol.equals("USER") || rol.equals("")) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(0).setVisible(false);
        }
        /*MenuItem searchItem = menu.findItem(R.id.action_search);
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
        });*/
        return true;
    }

    private void actualizarHorarios(String recCod, String tviCod, String horNombre, String dia) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        HorariosAPIs horariosAPIs = retrofit.create(HorariosAPIs.class);
        Call <List<Horario>> call = horariosAPIs.listaHorarios(recCod,tviCod,horNombre,dia, authorization);
        call.enqueue(new Callback <List<Horario>> () {
            @Override
            public void onResponse(Call <List<Horario>> call, Response <List<Horario>> response) {
                bar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                horarios.clear();
                if (response.body().size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoData.setVisibility(View.GONE);
                    for(int i=0;i<response.body().size();i++)
                    {
                        horarios.add(response.body().get(i));
                    }
                    horariosAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                bar.setVisibility(View.GONE);
                tvFallo.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEventoClick(Horario horario) {
        Log.d("msg",""+horario.getHorNombre());
        iniciarHDetallesActivity(horario);
    }

    public void iniciarHDetallesActivity(Horario horario)
    {
        Intent intent = new Intent(Horarios.this, DetallesHorario.class);
        intent.putExtra("horario", horario);
        startActivity(intent);
    }

    public void iniciarSpinnerTipoVisitante() {
        tipoVisitantes = new ArrayList<>();
        TipoVisitante tipoVisitante = new TipoVisitante();
        tipoVisitante.setTviCod("cod");
        tipoVisitante.setTviNombre("SELECCIONE TIPO DE VISITANTE");
        tipoVisitante.setTviDescripcion("obs");
        tipoVisitante.setHorEstado("estado");

        tipoVisitantes.add(tipoVisitante);
        adapterTipoVisitante = new ArrayAdapter<TipoVisitante>(this, R.layout.style_spinner, tipoVisitantes){
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textview = (TextView) view;
                if (position == 0) {
                    textview.setTextColor(Color.GRAY);
                } else {
                    textview.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
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
        Call<List<TipoVisitante>> call = tipoVisitantesAPIs.listaTipoVisitante(authorization);
        call.enqueue(new Callback<List<TipoVisitante>>() {
            @Override
            public void onResponse(Call <List<TipoVisitante>> call, Response<List<TipoVisitante>> response) {
                //tipoVisitantes = response.body();

                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    tipoVisitantes.add(response.body().get(i));
                }

                /*tipoVisitanteS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        tipoVisitanteSel = (TipoVisitante) parent.getSelectedItem();
                        displayTipoVisitanteData(tipoVisitanteSel);

                        fetchHorarios(recintoRecibido.getRecCod(), tipoVisitanteSel.getTviCod(),"todos","todos");
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });*/
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
