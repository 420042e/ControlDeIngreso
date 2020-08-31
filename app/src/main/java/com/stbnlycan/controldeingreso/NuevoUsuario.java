package com.stbnlycan.controldeingreso;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.squareup.picasso.Picasso;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.EmpresaAPIs;
import com.stbnlycan.interfaces.EnviarCorreoIAPIs;
import com.stbnlycan.interfaces.ListaEmpresasAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.NuevoUsuarioAPIs;
import com.stbnlycan.interfaces.TipoVisitanteAPIs;
import com.stbnlycan.interfaces.UploadAPIs;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.ListaEmpresas;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.Rol;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Usuario;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NuevoUsuario extends AppCompatActivity implements Validator.ValidationListener{

    private ImageView visitanteIV;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private List<Recinto> areas;
    private ArrayList<Rol> empresas;
    private ArrayList<TipoVisitante> tiposVisitante;

    private ArrayAdapter<Rol> adapterRol;
    private Visitante visitante;

    @NotEmpty
    private EditText usernameET;
    @NotEmpty
    private EditText passwordET;
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

    private Validator validator;
    private Toolbar toolbar;
    private Button btnNF;
    private String imagenObtenida;
    private final static int REQUEST_CODE_NE = 1;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Recinto recintoRecibido;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_usuario);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        validator = new Validator(this);
        validator.setValidationListener(this);

        setTitle("Nuevo usuario");
        visitanteIV = findViewById(R.id.visitanteIV);
        btnNF = findViewById(R.id.btnNF);
        usernameET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);
        emailET = findViewById(R.id.email);
        fullnameET = findViewById(R.id.fullname);
        occupationET = findViewById(R.id.occupation);
        phoneET = findViewById(R.id.phone);
        addressET = findViewById(R.id.address);
        rolS = findViewById(R.id.rol);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        iniciarSpinnerEmpresa();




        //getDataEmpresa();
        //getDataTipoVisitante();

        btnNF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(imageTakeIntent.resolveActivity(getPackageManager())!=null)
                {
                    startActivityForResult(imageTakeIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

    }

    public void iniciarSpinnerEmpresa() {
        empresas = new ArrayList<>();
        Rol rol = new Rol();
        rol.setNombre("SELECCIONE UN ROL");
        rol.setDescripcion("SELECCIONE UN ROL");
        Rol rol1 = new Rol();
        rol1.setNombre("ADMIN");
        rol1.setDescripcion("ADMINISTRADOR");
        Rol rol2 = new Rol();
        rol2.setNombre("USER");
        rol2.setDescripcion("USUARIO");
        empresas.add(rol);
        empresas.add(rol1);
        empresas.add(rol2);
        adapterRol = new ArrayAdapter<Rol>(this, R.layout.style_spinner, empresas){
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
    }

    private void displayRolData(Rol rol) {
        String nombre = rol.getNombre();
        String desc = rol.getDescripcion();
        String userData = "Nombre: " + nombre + "\nDesc: " + desc;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            //visitanteIV.setImageBitmap(imageBitmap);

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));

            imagenObtenida = finalFile.toString();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            Picasso.get().load(finalFile).resize(width, width).into(visitanteIV);

        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NE) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Rol rolResult = (Rol) b.getSerializable("rolResult");
                    empresas.add(1, rolResult);
                    rolS.setSelection(1, true);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        Bitmap OutImage = Bitmap.createScaledBitmap(inImage, 1000, 1000,true);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), OutImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_nuevo_visitante:
                validator.validate();
                return false;
            case R.id.action_salir:
                cerrarSesion();
                Intent intent = new Intent(NuevoUsuario.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_nv, menu);

        if(rol.equals("USER") || rol.equals(""))
        {
            btnNF.setEnabled(false);
            usernameET.setFocusable(false);
            usernameET.setFocusableInTouchMode(false);
            passwordET.setFocusable(false);
            passwordET.setFocusableInTouchMode(false);
            emailET.setFocusable(false);
            emailET.setFocusableInTouchMode(false);
            fullnameET.setFocusable(false);
            fullnameET.setFocusableInTouchMode(false);
            occupationET.setFocusable(false);
            occupationET.setFocusableInTouchMode(false);
            phoneET.setFocusable(false);
            phoneET.setFocusableInTouchMode(false);
            addressET.setFocusable(false);
            addressET.setFocusableInTouchMode(false);
            rolS.setFocusable(false);
            rolS.setFocusableInTouchMode(false);
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
                visitantesAdapter.getFilter().filter(newText);
                return false;
            }
        });*/
        return true;
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
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

    private void uploadToServer(String filePath, String descripcion) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        NuevoUsuarioAPIs nuevoUsuarioAPIs = retrofit.create(NuevoUsuarioAPIs.class);
        File file = new File(filePath);
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), descripcion);
        Call <Usuario> call = nuevoUsuarioAPIs.nuevoUsuario(part, description, authorization);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call <Usuario> call, Response<Usuario> response) {
                Gson gson = new Gson();
                String descripcion = gson.toJson(usuario);
                //Log.d("msg1255",""+response.body());

                usuario.setPic(response.body().getPic());
                Toast.makeText(getApplicationContext(), "Se guardó el nuevo usuario", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("usuarioResult", usuario);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(Call <Usuario>call, Throwable t) {
                Log.d("msg2",""+t);
            }
        });
    }

    private void enviarCorreoIngreso() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        EnviarCorreoIAPIs enviarCorreoIAPIs = retrofit.create(EnviarCorreoIAPIs.class);
        Call call = enviarCorreoIAPIs.enviarCorreo(emailET.getText().toString(), authorization);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                if (response.body() != null) {
                    //Aqui se debería cerrar esta actividad al recibir respuesta del server
                    Toast.makeText(getApplicationContext(), "Se envió el correo de ingreso", Toast.LENGTH_LONG).show();
                    visitante.setVteEstado("0");
                    Intent intent = new Intent();
                    intent.putExtra("visitanteResult", visitante);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("msg4",""+t);
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        //Toast.makeText(this, "We got it right!", Toast.LENGTH_LONG).show();
        if(hasNullOrEmptyDrawable(visitanteIV))
        {
            Toast.makeText(this, "Debes añadir una fotografía", Toast.LENGTH_LONG).show();
        }
        else
        {
            usuario = new Usuario();
            usuario.setUsername(usernameET.getText().toString());
            usuario.setPassword(passwordET.getText().toString());
            usuario.setEmail(emailET.getText().toString().toUpperCase());
            usuario.setPic("");
            usuario.setFullname(fullnameET.getText().toString().toUpperCase());
            usuario.setOccupation(occupationET.getText().toString().toUpperCase());
            usuario.setPhone(phoneET.getText().toString());
            usuario.setAddress(addressET.getText().toString().toUpperCase());
            usuario.setState("");
            usuario.setRecinto(recintoRecibido);
            usuario.setRol((Rol) rolS.getSelectedItem());

            showLoadingwDialog();
            Gson gson = new Gson();
            String descripcion = gson.toJson(usuario);
            //Log.d("msg9128",""+descripcion);

            uploadToServer(imagenObtenida, descripcion);
        }
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

}
