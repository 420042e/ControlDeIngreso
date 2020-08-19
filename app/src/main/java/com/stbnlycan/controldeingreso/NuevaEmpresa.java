package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.RegistrarEmpresaAPIs;
import com.stbnlycan.models.Empresa;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NuevaEmpresa extends AppCompatActivity implements Validator.ValidationListener{

    @NotEmpty
    private EditText nombreET;
    @NotEmpty
    private EditText observacionET;
    private Toolbar toolbar;
    private Validator validator;
    private Empresa empresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_empresa);

        validator = new Validator(this);
        validator.setValidationListener(this);

        setTitle("Nueva empresa");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nombreET = findViewById(R.id.nombre);
        observacionET = findViewById(R.id.observacion);
    }

    @Override
    public void onValidationSucceeded() {
        empresa = new Empresa();
        empresa.setEmpNombre(nombreET.getText().toString().toUpperCase());
        empresa.setEmpObs(observacionET.getText().toString().toUpperCase());
        showLoadingwDialog();
        registrarEmpresa();

        /*Intent intent = new Intent();
        intent.putExtra("empresaResult", empresa);
        setResult(RESULT_OK, intent);
        finish();*/
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

    private void registrarEmpresa() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarEmpresaAPIs registrarEmpresaAPIs = retrofit.create(RegistrarEmpresaAPIs.class);
        Call<Empresa> call = registrarEmpresaAPIs.registrarEmpresa(empresa);
        call.enqueue(new Callback<Empresa>() {
            @Override
            public void onResponse(Call <Empresa> call, Response<Empresa> response) {
                Empresa empresaRecibida = response.body();
                Toast.makeText(getApplicationContext(), "La nueva empresa fué registrada", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("empresaResult", empresaRecibida);
                setResult(RESULT_OK, intent);
                finish();
            }
            @Override
            public void onFailure(Call <Empresa> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ge, menu);
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
        }
        return super.onOptionsItemSelected(item);
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
