package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.stbnlycan.fragments.DFTknExpired;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.EditarUsuarioAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.models.Rol;
import com.stbnlycan.models.Usuario;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditarUsuario extends AppCompatActivity implements Validator.ValidationListener {

    private ArrayList<Rol> roles;
    private ArrayAdapter<Rol> adapterRol;
    private ImageView visitanteIV;
    @NotEmpty
    private EditText usernameET;
    @NotEmpty
    private EditText emailET;
    @NotEmpty
    private EditText fullnameET;
    @NotEmpty
    private EditText occupationET;
    @NotEmpty
    private EditText phoneET;
    @NotEmpty
    private EditText addressET;

    @Select
    private Spinner rolS;

    private Toolbar toolbar;

    private int position;

    private Usuario usuarioRecibido;
    private boolean cambioFoto;
    private Usuario usuario;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ProgressBar progressBar;
    private Validator validator;

    private FloatingActionButton fabNF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        validator = new Validator(this);
        validator.setValidationListener(this);

        usuarioRecibido = (Usuario) getIntent().getSerializableExtra("usuario");
        position = getIntent().getIntExtra("position", -1);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

        setTitle("Editar usuario");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        visitanteIV = findViewById(R.id.visitanteIV);
        usernameET = findViewById(R.id.username);
        emailET = findViewById(R.id.email);
        fullnameET = findViewById(R.id.fullname);
        occupationET = findViewById(R.id.occupation);
        phoneET = findViewById(R.id.phone);
        addressET = findViewById(R.id.address);
        rolS = findViewById(R.id.rol);
        progressBar = findViewById(R.id.progressBar);
        fabNF = findViewById(R.id.fabNF);

        cambioFoto = false;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        iniciarSpinnerRol();

        progressBar.setVisibility(View.VISIBLE);

        String url = getResources().getString(R.string.url_foto_usuarios) + usuarioRecibido.getPic();
        GlideUrl glideUrl = new GlideUrl(url,
                new LazyHeaders.Builder()
                        .addHeader("Authorization", authorization)
                        .build());
        Glide.with(this)
                .load(glideUrl)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(visitanteIV);




        usernameET.setText(usuarioRecibido.getUsername());
        emailET.setText(usuarioRecibido.getEmail());
        fullnameET.setText(usuarioRecibido.getFullname());
        occupationET.setText(usuarioRecibido.getOccupation());
        phoneET.setText(usuarioRecibido.getPhone());
        addressET.setText(usuarioRecibido.getAddress());
    }

    public void iniciarSpinnerRol() {
        Log.d("msg993",""+usuarioRecibido.getRol().getNombre());
        roles = new ArrayList<>();
        Rol rol = new Rol();
        rol.setNombre("SELECCIONE UN ROL");
        rol.setDescripcion("SELECCIONE UN ROL");
        Rol rol1 = new Rol();
        rol1.setNombre("ADMIN");
        rol1.setDescripcion("ADMINISTRADOR");
        Rol rol2 = new Rol();
        rol2.setNombre("USER");
        rol2.setDescripcion("USUARIO");
        roles.add(rol);
        roles.add(rol1);
        roles.add(rol2);
        adapterRol = new ArrayAdapter<Rol>(this, R.layout.style_spinner, roles){
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
        adapterRol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rolS.setAdapter(adapterRol);
        rolS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Rol rol = (Rol) parent.getSelectedItem();
                displayRolData(rol);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int pos = -1;
        for(int i=0;i<roles.size();i++)
        {
            if(roles.get(i).getNombre().equals(usuarioRecibido.getRol().getNombre()))
            {
                pos = i;
            }
        }
        rolS.setSelection(pos, true);
    }

    private void displayRolData(Rol rol) {
        String nombre = rol.getNombre();
        String desc = rol.getDescripcion();
        String userData = "Nombre: " + nombre + "\nDesc: " + desc;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_eu, menu);

        if(rol.equals("USER") || rol.equals(""))
        {
            menu.getItem(0).setEnabled(false);
            menu.getItem(0).setVisible(false);
            //btnNF.setEnabled(false);
            fabNF.setEnabled(false);
            usernameET.setFocusable(false);
            usernameET.setFocusableInTouchMode(false);
            fullnameET.setFocusable(false);
            fullnameET.setFocusableInTouchMode(false);
            occupationET.setFocusable(false);
            occupationET.setFocusableInTouchMode(false);
            phoneET.setFocusable(false);
            phoneET.setFocusableInTouchMode(false);
            emailET.setFocusable(false);
            emailET.setFocusableInTouchMode(false);
            rolS.setEnabled(false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish();
                if(cambioFoto)
                {
                    cambioFoto = false;
                    Intent intent = new Intent();
                    //intent.putExtra("visitanteResult", visitanteResult);
                    //intent.putExtra("position", position);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else
                {
                    finish();
                }
                return false;
            case R.id.action_editar_usuario:
                validator.validate();
                return false;
            case R.id.action_salir:
                cerrarSesion();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValidationSucceeded() {
        /*if(hasNullOrEmptyDrawable(visitanteIV))
        {
            Toast.makeText(this, "Debes añadir una fotografía", Toast.LENGTH_LONG).show();
        }
        else
        {
            usuario = new Usuario();
            usuario.setAddress(addressET.getText().toString());
            usuario.setEmail(emailET.getText().toString());
            usuario.setFullname(fullnameET.getText().toString());
            usuario.setOccupation(occupationET.getText().toString());
            usuario.setPhone(phoneET.getText().toString());
            //usuario.setPic();
            usuario.setRecinto(usuarioRecibido.getRecinto());
            usuario.setUsername(usernameET.getText().toString());
            usuario.setRol((Rol) rolS.getSelectedItem());
            editarUsuario();
        }*/
        usuario = new Usuario();
        usuario.setAddress(addressET.getText().toString().toUpperCase());
        usuario.setEmail(emailET.getText().toString().toUpperCase());
        usuario.setFullname(fullnameET.getText().toString().toUpperCase());
        usuario.setOccupation(occupationET.getText().toString().toUpperCase());
        usuario.setPassword(usuarioRecibido.getPassword());
        usuario.setPhone(phoneET.getText().toString());
        usuario.setPic(usuarioRecibido.getPic());
        usuario.setRecinto(usuarioRecibido.getRecinto());
        usuario.setRol((Rol) rolS.getSelectedItem());
        usuario.setState(usuarioRecibido.getState());
        usuario.setUsername(usernameET.getText().toString());
        editarUsuario();
    }

    private void editarUsuario() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        EditarUsuarioAPIs editarUsuarioAPIs = retrofit.create(EditarUsuarioAPIs.class);
        Call<Usuario> call = editarUsuarioAPIs.editarUsuario(usuario, authorization);
        call.enqueue(new Callback <Usuario> () {
            @Override
            public void onResponse(Call <Usuario> call, Response<Usuario> response) {
                if (response.code() == 401) {
                    showTknExpDialog();
                }
                else
                {
                    Usuario usuarioRecibido = response.body();
                    Toast.makeText(getApplicationContext(), "El usuario fué actualizado", Toast.LENGTH_LONG).show();
                    //visitante.setVteEstado("ACT");

                    Intent intent = new Intent();
                    intent.putExtra("usuarioResult", usuarioRecibido);
                    intent.putExtra("position", position);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call <Usuario> call, Throwable t) {
                Log.d("msg132",""+t);
            }
        });
    }

    public static boolean hasNullOrEmptyDrawable(ImageView iv)
    {
        Drawable drawable = iv.getDrawable();
        BitmapDrawable bitmapDrawable = drawable instanceof BitmapDrawable ? (BitmapDrawable)drawable : null;

        return bitmapDrawable == null || bitmapDrawable.getBitmap() == null;
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
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
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
    public void onBackPressed() {
        //super.onBackPressed();
        if(cambioFoto)
        {
            cambioFoto = false;
            Intent intent = new Intent();
            //intent.putExtra("visitanteResult", visitanteResult);
            //intent.putExtra("position", position);
            setResult(RESULT_OK, intent);
            finish();
        }
        else
        {
            super.onBackPressed();
        }
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
