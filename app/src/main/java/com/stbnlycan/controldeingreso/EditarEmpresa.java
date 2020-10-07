package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.EditarEmpresaAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.RegistrarEmpresaAPIs;
import com.stbnlycan.models.Empresa;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditarEmpresa extends AppCompatActivity implements Validator.ValidationListener{

    @NotEmpty
    private EditText nombreET;
    @NotEmpty
    private EditText observacionET;
    private Toolbar toolbar;
    private Validator validator;
    private Empresa empresa;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Empresa empresaRecibida;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_empresa);

        validator = new Validator(this);
        validator.setValidationListener(this);

        setTitle("Editar empresa");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nombreET = findViewById(R.id.nombre);
        observacionET = findViewById(R.id.observacion);

        empresaRecibida = (Empresa) getIntent().getSerializableExtra("empresa");
        position = getIntent().getIntExtra("position", -1);

        nombreET.setText(empresaRecibida.getEmpNombre());
        observacionET.setText(empresaRecibida.getEmpObs());

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

    }

    @Override
    public void onValidationSucceeded() {
        empresa = new Empresa();
        empresa.setEmpCod(empresaRecibida.getEmpCod());
        empresa.setEmpNombre(nombreET.getText().toString().toUpperCase());
        empresa.setEmpObs(observacionET.getText().toString().toUpperCase());
        showLoadingwDialog();
        actualizarEmpresa();

        /*Intent intent = new Intent();
        intent.putExtra("empresaResult", empresa);
        setResult(RESULT_OK, intent);
        finish();*/
    }

    private void actualizarEmpresa() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        EditarEmpresaAPIs editarEmpresaAPIs = retrofit.create(EditarEmpresaAPIs.class);
        Call<Empresa> call = editarEmpresaAPIs.editarEmpresa(empresa, authorization);
        call.enqueue(new Callback<Empresa>() {
            @Override
            public void onResponse(Call <Empresa> call, Response<Empresa> response) {
                /*Gson gson = new Gson();
                String descripcion = gson.toJson(empresa);
                Log.d("msg741",""+descripcion);*/
                Empresa empresaRecibida = response.body();
                Toast.makeText(getApplicationContext(), "La empresa fué actualizada", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("empresaResult", empresaRecibida);
                intent.putExtra("position", position);
                setResult(RESULT_OK, intent);
                finish();
            }
            @Override
            public void onFailure(Call <Empresa> call, Throwable t) {

            }
        });
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
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ge, menu);

        if(rol.equals("USER") || rol.equals(""))
        {
            menu.getItem(0).setEnabled(false);
            menu.getItem(0).setVisible(false);
            nombreET.setFocusable(false);
            nombreET.setFocusableInTouchMode(false);
            observacionET.setFocusable(false);
            observacionET.setFocusableInTouchMode(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_guardar_empresa:
                validator.validate();
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
                Intent intent = new Intent(EditarEmpresa.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

    void showLoadingwDialog() {

        final LoadingFragment dialogFragment = new LoadingFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putInt("tiempo", 0);
        dialogFragment.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }
}
