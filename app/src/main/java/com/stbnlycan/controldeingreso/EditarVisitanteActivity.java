package com.stbnlycan.controldeingreso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.EmpresaAPIs;
import com.stbnlycan.interfaces.ListaEmpresasAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.SubirImagenAPIs;
import com.stbnlycan.interfaces.TipoVisitanteAPIs;
import com.stbnlycan.interfaces.VisitanteAPIs;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.ListaEmpresas;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditarVisitanteActivity extends AppCompatActivity implements Validator.ValidationListener{

    private ImageView visitanteIV;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private List<Recinto> areas;
    private ArrayList<Empresa> empresas;
    private ArrayList<TipoVisitante> tiposVisitante;

    private ArrayAdapter<Empresa> adapterEmpresa;
    private ArrayAdapter<TipoVisitante> adapterTipoVisitante;
    private Visitante visitante;

    @NotEmpty
    private EditText ciET;
    @NotEmpty
    private EditText nombreET;
    @NotEmpty
    private EditText apellidosET;
    @NotEmpty
    private EditText telcelET;
    @NotEmpty
    private EditText emailET;
    @Select
    private Spinner empresaS;
    @Select
    private Spinner tipoVisitanteS;

    private Validator validator;
    private Visitante visitanteRecibido;
    private int position;
    private Toolbar toolbar;
    private Button btnNF;
    private String imagenObtenida;
    private Button btnNE;
    private final static int REQUEST_CODE_NE = 1;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_visitante);

        validator = new Validator(this);
        validator.setValidationListener(this);

        setTitle("Editar visitante");
        visitanteIV = findViewById(R.id.visitanteIV);
        ciET = findViewById(R.id.ci);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        telcelET = findViewById(R.id.telcel);
        emailET = findViewById(R.id.email);
        empresaS = findViewById(R.id.empresa);
        tipoVisitanteS = findViewById(R.id.tipo_visitante);
        btnNE = findViewById(R.id.btnNE);
        btnNF = findViewById(R.id.btnNF);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);
        rol = pref.getString("rol", null);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        iniciarSpinnerEmpresa();
        iniciarSpinnerTipoVisitante();

        visitanteRecibido = (Visitante) getIntent().getSerializableExtra("visitante");
        position = getIntent().getIntExtra("position", -1);
        //Log.d("msg2", ""+position);


        fetchDataEmpresa();
        fetchDataTipoVisitante();
        //getDataEmpresa();
        //getDataTipoVisitante();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", authorization)
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(client))
                .build();
        picasso.load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + visitanteRecibido.getVteImagen()).resize(width, width).into(visitanteIV);

        ciET.setText(visitanteRecibido.getVteCi());
        nombreET.setText(visitanteRecibido.getVteNombre());
        apellidosET.setText(visitanteRecibido.getVteApellidos());
        telcelET.setText(visitanteRecibido.getVteTelefono());
        emailET.setText(visitanteRecibido.getVteCorreo());

        /*empresaS.setSelection(0);
        tipoVisitanteS.setSelection(0);*/

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

        btnNE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditarVisitanteActivity.this, NuevaEmpresa.class);
                startActivityForResult(intent, REQUEST_CODE_NE);
            }
        });
    }

    public void iniciarSpinnerEmpresa() {
        empresas = new ArrayList<>();
        Empresa empresa = new Empresa();
        empresa.setEmpCod("cod");
        empresa.setEmpNombre("SELECCIONE UNA EMPRESA");
        empresa.setEmpObs("obs");
        empresas.add(empresa);
        adapterEmpresa = new ArrayAdapter<Empresa>(this, R.layout.style_spinner, empresas){
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
        adapterEmpresa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        empresaS.setAdapter(adapterEmpresa);
        empresaS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Empresa empresa = (Empresa) parent.getSelectedItem();
                displayEmpresaData(empresa);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void iniciarSpinnerTipoVisitante() {
        tiposVisitante = new ArrayList<>();
        TipoVisitante tipoVisitante = new TipoVisitante();
        tipoVisitante.setTviCod("cod");
        tipoVisitante.setTviNombre("SELECCIONE TIPO DE VISITANTE");
        tipoVisitante.setTviDescripcion("obs");
        tipoVisitante.setHorEstado("estado");
        tiposVisitante.add(tipoVisitante);
        adapterTipoVisitante = new ArrayAdapter<TipoVisitante>(this, R.layout.style_spinner, tiposVisitante){
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
        tipoVisitanteS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TipoVisitante tipoVisitante = (TipoVisitante) parent.getSelectedItem();
                displayTipoVisitanteData(tipoVisitante);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void displayEmpresaData(Empresa empresa) {
        String cod = empresa.getEmpCod();
        String nombre = empresa.getEmpNombre();
        String obs = empresa.getEmpObs();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + obs;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    private void displayTipoVisitanteData(TipoVisitante tipoVisitante) {
        String cod = tipoVisitante.getTviCod();
        String nombre = tipoVisitante.getTviNombre();
        String descripcion = tipoVisitante.getTviDescripcion();
        String estado = tipoVisitante.getHorEstado();
        String userData = "Cod: " + cod + "\nNombre: " + nombre + "\nObs: " + descripcion + "\nEstado: " + estado;
        //Toast.makeText(this, userData, Toast.LENGTH_LONG).show();
    }

    /*public void takePicture(View view)
    {
        Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(imageTakeIntent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(imageTakeIntent, REQUEST_IMAGE_CAPTURE);
        }
        //Picasso.get().load("http://dineroclub.net/wp-content/uploads/2019/11/DEVELOPER3-696x465.jpg").centerCrop().resize(150, 150).into(visitanteIV);
    }*/

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

            //showLoadingwDialog();
            Gson gson = new Gson();
            String descripcion = gson.toJson(visitanteRecibido);

            imagenObtenida = finalFile.toString();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            Picasso.get().load(finalFile).resize(width, width).into(visitanteIV);
            //imagenObtenida = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/prueba.jpg";

            subirImagen(descripcion);
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NE) {
                Bundle b = data.getExtras();
                if (data != null) {
                    Empresa empresaResult = (Empresa) b.getSerializable("empresaResult");
                    empresas.add(1, empresaResult);
                    empresaS.setSelection(1, true);
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
            case R.id.action_editar_visitante:
                validator.validate();
                return false;
            case R.id.action_salir:
                cerrarSesion();
                Intent intent = new Intent(EditarVisitanteActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ev, menu);

        if(rol.equals("USER") || rol.equals(""))
        {
            menu.getItem(0).setEnabled(false);
            menu.getItem(0).setVisible(false);
            btnNF.setEnabled(false);
            ciET.setFocusable(false);
            ciET.setFocusableInTouchMode(false);
            nombreET.setFocusable(false);
            nombreET.setFocusableInTouchMode(false);
            apellidosET.setFocusable(false);
            apellidosET.setFocusableInTouchMode(false);
            telcelET.setFocusable(false);
            telcelET.setFocusableInTouchMode(false);
            emailET.setFocusable(false);
            emailET.setFocusableInTouchMode(false);
            empresaS.setEnabled(false);
            btnNE.setEnabled(false);
            tipoVisitanteS.setEnabled(false);
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

    private void fetchDataEmpresa() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ListaEmpresasAPIs listaEmpresasAPIs = retrofit.create(ListaEmpresasAPIs.class);
        Call<ListaEmpresas> call = listaEmpresasAPIs.listaEmpresas("0","10", authorization);
        call.enqueue(new Callback<ListaEmpresas>() {
            @Override
            public void onResponse(Call <ListaEmpresas> call, retrofit2.Response<ListaEmpresas> response) {
                //recintos = response.body();
                int pos = -1;
                for(int i = 0 ; i < response.body().getlEmpresa().size() ; i++)
                {
                    empresas.add(response.body().getlEmpresa().get(i));
                    if(response.body().getlEmpresa().get(i).getEmpCod().equals(visitanteRecibido.getEmpresa().getEmpCod()))
                    {
                        pos = i+1;
                    }
                }
                //adapter.notifyDataSetChanged();
                empresaS.setSelection(pos, true);
            }
            @Override
            public void onFailure(Call <ListaEmpresas> call, Throwable t) {

            }
        });
    }

    private void fetchDataTipoVisitante() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        TipoVisitanteAPIs tipoVisitanteAPIs = retrofit.create(TipoVisitanteAPIs.class);
        Call<List<TipoVisitante>> call = tipoVisitanteAPIs.listaTipoVisitante(authorization);
        call.enqueue(new Callback<List<TipoVisitante>>() {
            @Override
            public void onResponse(Call <List<TipoVisitante>> call, retrofit2.Response<List<TipoVisitante>> response) {
                int pos = -1;
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    tiposVisitante.add(response.body().get(i));
                    if(response.body().get(i).getTviCod().equals(visitanteRecibido.getTipoVisitante().getTviCod()))
                    {
                        pos = i+1;
                    }
                }
                tipoVisitanteS.setSelection(pos, true);
            }
            @Override
            public void onFailure(Call <List<TipoVisitante>> call, Throwable t) {

            }
        });
    }

    /*public void guardarVisitante(View view) {
        validator.validate();
    }*/

    private void editarVisitante() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        VisitanteAPIs visitanteAPIs = retrofit.create(VisitanteAPIs.class);
        Call<Visitante> call = visitanteAPIs.editarVisitante(visitante, authorization);
        call.enqueue(new Callback <Visitante> () {
            @Override
            public void onResponse(Call <Visitante> call, Response<Visitante> response) {
                Visitante visitanteRecibido = response.body();
                Toast.makeText(getApplicationContext(), "El visitante fué actualizado", Toast.LENGTH_LONG).show();
                //visitante.setVteEstado("ACT");

                Intent intent = new Intent();
                intent.putExtra("visitanteResult", visitante);
                intent.putExtra("position", position);
                setResult(RESULT_OK, intent);
                finish();
            }
            @Override
            public void onFailure(Call <Visitante> call, Throwable t) {
                Log.d("msg132",""+t);
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
            //Visitante visitante = new Visitante();
            visitante = new Visitante();
            visitante.setVteCi(ciET.getText().toString());
            visitante.setVteCorreo(emailET.getText().toString());

            visitante.setVteImagen(visitanteRecibido.getVteImagen());

            visitante.setVteNombre(nombreET.getText().toString().toUpperCase());
            visitante.setVteApellidos(apellidosET.getText().toString().toUpperCase());
            visitante.setVteTelefono(telcelET.getText().toString());
            visitante.setVteDireccion("");

            visitante.setVteEstado(visitanteRecibido.getVteEstado());
            visitante.setVteLlave(visitanteRecibido.getVteLlave());
            visitante.setVteFecha(visitanteRecibido.getVteFecha());

            TipoVisitante tipoVisitante = (TipoVisitante) tipoVisitanteS.getSelectedItem();
            Empresa empresa = (Empresa) empresaS.getSelectedItem();
            visitante.setTipoVisitante(tipoVisitante);
            visitante.setEmpresa(empresa);

            showLoadingwDialog();
            Gson gson = new Gson();
            String descripcion = gson.toJson(visitante);

            editarVisitante();

            /*visitante.setVteEstado("0");
            Intent intent = new Intent();
            intent.putExtra("visitanteResult", visitante);
            intent.putExtra("position", position);
            setResult(RESULT_OK, intent);
            finish();*/
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

    private void subirImagen(String descripcion) {
        Log.d("msgCB1", descripcion);

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        SubirImagenAPIs subirImagenAPIs = retrofit.create(SubirImagenAPIs.class);
        File file = new File(imagenObtenida);
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), descripcion);
        Call<Visitante> call = subirImagenAPIs.subirImagen(part, description, authorization);
        call.enqueue(new Callback<Visitante>() {
            @Override
            public void onResponse(Call <Visitante> call, retrofit2.Response <Visitante> response) {

                Visitante visitanteCB = response.body();

                Toast.makeText(getApplicationContext(), "Se guardó la nueva imágen", Toast.LENGTH_LONG).show();

                visitanteRecibido.setVteImagen(visitanteCB.getVteImagen());



                /*Gson gson2 = new Gson();
                String descripcion2 = gson2.toJson(visitanteCB);
                Log.d("msgCB2", descripcion2);*/

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("msg2",""+t);
            }
        });
    }

}
