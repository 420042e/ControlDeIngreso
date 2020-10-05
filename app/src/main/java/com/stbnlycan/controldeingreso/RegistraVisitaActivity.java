package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stbnlycan.fragments.DFError;
import com.stbnlycan.fragments.DFIngreso;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.AreaRecintoAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.MotivosAPIs;
import com.stbnlycan.interfaces.RegistrarIngreso2APIs;
import com.stbnlycan.interfaces.RegistrarIngresoAPIs;
import com.stbnlycan.interfaces.RegistrarSalidaAPIs;
import com.stbnlycan.interfaces.TipoDocAPIs;
import com.stbnlycan.interfaces.UploadAPIs;
import com.stbnlycan.models.Aduana;
import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.DocumentoIngreso;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Error;
import com.stbnlycan.models.Motivo;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoDocumento;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class RegistraVisitaActivity extends AppCompatActivity implements Validator.ValidationListener {

    private ArrayList<AreaRecinto> areaRecinto;
    private ArrayList<Motivo> motivo;
    private ArrayList<TipoDocumento> tipoDocumento;

    private ArrayAdapter<AreaRecinto> adapterAreaR;
    private ArrayAdapter<Motivo> adapterMotivo;
    private ArrayAdapter<TipoDocumento> adapterTipoDoc;
    private Visitante visitanteRecibido;
    private Recinto recintoRecibido;
    private LoadingFragment loadingFragment;

    @Select
    private Spinner areaRecintoS;

    @Select
    private Spinner motivoS;

    private EditText ciET;
    private EditText nombreET;
    private EditText apellidosET;

    private EditText observacion;
    private Validator validator;

    private Toolbar toolbar;

    private ImageView visitanteIV;
    private ImageView doiImagenIV;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Button fotoDoc;

    private Uri uri;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private String imagenObtenida;
    private String codigoQR;

    private final static int REQUEST_CODE_DOI = 1;
    private ArrayList<DocumentoIngreso> doisResult;
    private ArrayList<String> srcs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_visita);

        setTitle("Registrar Ingreso");
        visitanteIV = findViewById(R.id.visitanteIV);
        doiImagenIV = findViewById(R.id.doiImagenIV);
        ciET = findViewById(R.id.ci);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        areaRecintoS = findViewById(R.id.area_recinto);
        motivoS = findViewById(R.id.motivo);
        observacion = findViewById(R.id.observacion);

        fotoDoc = findViewById(R.id.fotoDoc);

        validator = new Validator(this);
        validator.setValidationListener(this);

        codigoQR = "";
        doisResult = new ArrayList<>();

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitanteRecibido = (Visitante) getIntent().getSerializableExtra("visitante");
        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        srcs = new ArrayList<>();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
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

        ciET.setEnabled(false);
        ciET.setFocusable(false);
        nombreET.setEnabled(false);
        nombreET.setFocusable(false);
        apellidosET.setEnabled(false);
        apellidosET.setFocusable(false);




        iniciarSpinnerArea();
        fetchAreaRecintos();

        iniciarSpinnerMotivo();
        fetchMotivos();

        fotoDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("msg123 ", "hola1");
                /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            uri = FileProvider.getUriForFile(getApplicationContext(),"com.stbnlycan.controldeingreso.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            Log.d("msg4214",""+photoFile);
                            imagenObtenida = photoFile.toString();
                        }
                        else
                        {
                            uri = FileProvider.getUriForFile(getApplicationContext(),"com.stbnlycan.controldeingreso.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            Log.d("msg4215",""+photoFile);
                            imagenObtenida = photoFile.toString();
                        }
                    }
                }*/
                iniciarDOIActivity();
            }
        });

    }

    public void iniciarDOIActivity() {
        Intent intent = new Intent(RegistraVisitaActivity.this, DocumentosIngreso.class);
        intent.putExtra("dois", doisResult);
        startActivityForResult(intent, REQUEST_CODE_DOI);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        return image;
    }

    public void iniciarSpinnerArea() {
        areaRecinto = new ArrayList<>();

        AreaRecinto area = new AreaRecinto();
        area.setAreaCod("cod");
        area.setAreaNombre("SELECCIONE ÁREA DEL RECINTO");
        area.setAreaDescripcion("descripcion");
        area.setAreaEstado("estado");

        areaRecinto.add(area);
        adapterAreaR = new ArrayAdapter<AreaRecinto>(this, R.layout.style_spinner, areaRecinto)
        {
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
        adapterAreaR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaRecintoS.setAdapter(adapterAreaR);
        areaRecintoS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AreaRecinto areaRecinto = (AreaRecinto) parent.getSelectedItem();
                displayAreaRData(areaRecinto);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchAreaRecintos() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        AreaRecintoAPIs areaRecintoAPIs = retrofit.create(AreaRecintoAPIs.class);
        Call<List<AreaRecinto>> call = areaRecintoAPIs.listaPorRecinto(recintoRecibido.getRecCod(), authorization);
        call.enqueue(new Callback<List<AreaRecinto>>() {
            @Override
            public void onResponse(Call <List<AreaRecinto>> call, retrofit2.Response<List<AreaRecinto>> response) {
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    areaRecinto.add(response.body().get(i));
                }
            }
            @Override
            public void onFailure(Call <List<AreaRecinto>> call, Throwable t) {

            }
        });
    }

    public void iniciarSpinnerMotivo() {
        motivo = new ArrayList<>();

        Motivo motivod = new Motivo();
        motivod.setMvoCod(0);
        motivod.setMvoNombre("SELECCIONE MOTIVO");
        motivod.setMvoDescripcion("descripcion");

        motivo.add(motivod);
        adapterMotivo = new ArrayAdapter<Motivo>(this, R.layout.style_spinner, motivo)
        {
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
        adapterMotivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        motivoS.setAdapter(adapterMotivo);
        motivoS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*AreaRecinto areaRecinto = (AreaRecinto) parent.getSelectedItem();
                displayAreaRData(areaRecinto);*/
                Motivo motivo = (Motivo) parent.getSelectedItem();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchMotivos() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        MotivosAPIs motivosAPIs = retrofit.create(MotivosAPIs.class);
        Call<List<Motivo>> call = motivosAPIs.listaMotivo(authorization);
        call.enqueue(new Callback<List<Motivo>>() {
            @Override
            public void onResponse(Call <List<Motivo>> call, retrofit2.Response<List<Motivo>> response) {
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    motivo.add(response.body().get(i));
                }
            }
            @Override
            public void onFailure(Call <List<Motivo>> call, Throwable t) {

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
    public void onValidationSucceeded() {
        for(int i = 0 ; i < doisResult.size() ;i++)
        {
            srcs.add(doisResult.get(i).getDoiImagen());
            File f = new File(doisResult.get(i).getDoiImagen());
            doisResult.get(i).setDoiImagen(f.getName());
        }
        Visita visita = new Visita();
        AreaRecinto areaRecinto = (AreaRecinto) areaRecintoS.getSelectedItem();
        visita.setVisObs(observacion.getText().toString().toUpperCase());
        visita.setVisitante(visitanteRecibido);
        visita.setAreaRecinto(areaRecinto);

        Motivo motivo = (Motivo) motivoS.getSelectedItem();

        visita.setDocumentosIngreso(doisResult);

        visita.setMotivo(motivo);

        //registrarIngreso(visita);

        Gson gson = new Gson();
        String descripcion = gson.toJson(visita);
        //Log.d("msg912",""+descripcion);
        showLoadingwDialog();
        registrarIngreso2(imagenObtenida, descripcion);
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
            }else if (view instanceof Spinner) {
                //((TextView) ((Spinner) view).getSelectedView()).setError(message);
                ((TextView) ((Spinner) view).getSelectedView()).setError("Este campo es requerido");
            }else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registrarIngreso(final Visita visita) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarIngresoAPIs registrarIngresoAPIs = retrofit.create(RegistrarIngresoAPIs.class);
        Call<JsonObject> call = registrarIngresoAPIs.registrarIngreso(visita, authorization);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                String jsonString = response.body().toString();
                if (jsonString.contains("visCod")) {
                    Visita visitaRecibida = new Gson().fromJson(jsonString, Visita.class);
                    //Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha ingresado a " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.putExtra("success", "true");
                    intent.putExtra("visitaResult", visitaRecibida);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Error error = new Gson().fromJson(jsonString, Error.class);
                    //Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_LONG).show();

                    //showDFError(error.getMessage());
                    Intent intent = new Intent();
                    intent.putExtra("success", "false");
                    intent.putExtra("errorResult", error);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg",""+t.toString());
            }
        });
    }

    private void registrarIngreso2(String filePath, String descripcion) {
        List<MultipartBody.Part> files  = new ArrayList<>();
        for(int i = 0 ; i < srcs.size() ; i++)
        {
            File file = new File(srcs.get(i));
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imageRequest = MultipartBody.Part.createFormData("files", file.getName(), fileReqBody);
            files.add(imageRequest);
        }

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarIngreso2APIs uploadAPIs = retrofit.create(RegistrarIngreso2APIs.class);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), descripcion);
        Call <String> call = uploadAPIs.uploadImage(files , description, authorization);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call <String> call, retrofit2.Response<String> response) {
                Gson gson = new Gson();
                String descripcion = gson.toJson(response.body());
                Log.d("msg919",""+descripcion);
                showLoadingwDialog();

                String jsonString = response.body();
                if (jsonString.contains("visCod")) {
                    Visita visitaRecibida = new Gson().fromJson(jsonString, Visita.class);
                    //Toast.makeText(getApplicationContext(), visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos() + " ha ingresado a " + visitaRecibida.getAreaRecinto().getAreaNombre(), Toast.LENGTH_LONG).show();
                    loadingFragment.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra("success", "true");
                    intent.putExtra("visitaResult", visitaRecibida);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Error error = new Gson().fromJson(jsonString, Error.class);
                    //Toast.makeText(getApplicationContext(), ""+error.getMessage(), Toast.LENGTH_LONG).show();

                    loadingFragment.dismiss();
                    //showDFError(error.getMessage());
                    Intent intent = new Intent();
                    intent.putExtra("success", "false");
                    intent.putExtra("errorResult", error);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call <String>call, Throwable t) {
                Log.d("msg2",""+t);
            }
        });
    }

    /*@NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = FileUtils.getFile(this, fileUri);
        //compress the image using Compressor lib
        Timber.d("size of image before compression --> " + file.getTotalSpace());
        compressedImageFile = new Compressor(this).compressToFile(file);
        Timber.d("size of image after compression --> " + compressedImageFile.getTotalSpace());
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        compressedImageFile);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_registrar_visitante:
                // Registrar visitante
                validator.validate();
                return false;
            case R.id.action_salir:
                cerrarSesion();
                Intent intent = new Intent(RegistraVisitaActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
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
        inflater.inflate(R.menu.menu_rv, menu);
        return true;
    }

    public void showDFError(String mensaje) {
        DFError dfError = new DFError();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putBoolean("notAlertDialog", true);
        bundle.putSerializable("mensaje", mensaje);
        dfError.setArguments(bundle);
        //dialogFragment.setOnEventoClickListener(RecintoActivity.this);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogError");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dfError.show(ft, "dialogError");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == REQUEST_CODE_DOI)
            {
                Bundle b = data.getExtras();
                if (data != null)
                {
                    //doisResult = null;
                    doisResult = (ArrayList<DocumentoIngreso>) b.getSerializable("doisResult");

                    /*for(int i = 0 ; i < doisResult.size() ;i++)
                    {
                        srcs.add(doisResult.get(i).getDoiImagen());
                        File f = new File(doisResult.get(i).getDoiImagen());
                        doisResult.get(i).setDoiImagen(f.getName());
                    }*/
                }
            }
        }
    }

    public void showLoadingwDialog() {
        loadingFragment = new LoadingFragment();
        FragmentTransaction ft;
        Bundle bundle = new Bundle();
        bundle.putInt("tiempo", 0);
        loadingFragment.setArguments(bundle);
        //dialogFragment.setTargetFragment(this, 1);
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogLoading");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        loadingFragment.show(ft, "dialogLoading");
    }

}
