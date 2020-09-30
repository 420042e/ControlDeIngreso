package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

    @Select
    private Spinner areaRecintoS;

    @Select
    private Spinner motivoS;

    private Spinner tipoDocS;

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

    private EditText doiDocumentoET;

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
        tipoDocS = findViewById(R.id.tipoDoc);
        observacion = findViewById(R.id.observacion);
        doiDocumentoET = findViewById(R.id.doiDocumentoET);

        fotoDoc = findViewById(R.id.fotoDoc);

        validator = new Validator(this);
        validator.setValidationListener(this);

        codigoQR = "";

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitanteRecibido = (Visitante) getIntent().getSerializableExtra("visitante");
        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

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

        doiDocumentoET.setFocusable(false);

        doiDocumentoET.setText("OBTENER DOI DEL DOCUMENTO");


        iniciarSpinnerArea();
        fetchAreaRecintos();

        iniciarSpinnerMotivo();
        fetchMotivos();

        iniciarSpinnerTipoDoc();
        fetchTipoDoc();

        fotoDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("msg123 ", "hola1");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                }
            }
        });

        doiDocumentoET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escaner();
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelaste el escaneo de ingreso", Toast.LENGTH_LONG).show();
            } else {
                //showLoadingwDialog();
                //registrarSalida(result.getContents());
                Log.d("msg123",result.getContents());
                codigoQR = result.getContents();
                doiDocumentoET.setText(result.getContents());
            }
        }


        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            redimensionarImg();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                if(data != null)
                {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    doiImagenIV.setImageBitmap(imageBitmap);
                    doiImagenIV.getLayoutParams().width = width;
                    doiImagenIV.getLayoutParams().height = width;
                    doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                else
                {
                    doiImagenIV.setImageURI(uri);
                    doiImagenIV.getLayoutParams().width = width;
                    doiImagenIV.getLayoutParams().height = width;
                    doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
            else
            {
                Log.d("msg554","hola 2");
                doiImagenIV.setImageURI(uri);
                doiImagenIV.getLayoutParams().width = width;
                doiImagenIV.getLayoutParams().height = width;
                doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    public void redimensionarImg()
    {
        try
        {
            // we'll start with the original picture already open to a file
            File imgFileOrig = new File(imagenObtenida); //change "getPic()" for whatever you need to open the image file.
            Bitmap b = BitmapFactory.decodeFile(imgFileOrig.getAbsolutePath());
            // original measurements
            int origWidth = b.getWidth();
            int origHeight = b.getHeight();

            //Toast.makeText(getApplicationContext(), "origWidth "+origWidth+" origHeight "+origHeight, Toast.LENGTH_LONG).show();

            final int destWidth = 600;//or the width you need

            if(origWidth > destWidth)
            {
                // picture is wider than we want it, we calculate its target height
                int destHeight = origHeight/( origWidth / destWidth ) ;
                // we create an scaled bitmap so it reduces the image, not just trim it
                Bitmap b2 = Bitmap.createScaledBitmap(b, destWidth, destHeight, false);

                if(origWidth > origHeight)
                {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    b2 = Bitmap.createBitmap(b2, 0, 0, b2.getWidth(), b2.getHeight(), matrix, true);
                }

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // compress to the format you want, JPEG, PNG...
                // 70 is the 0-100 quality percentage
                b2.compress(Bitmap.CompressFormat.JPEG,100 , outStream);
                // we save the file, at least until we have made use of it
                //File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "test.jpg");
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + imgFileOrig.getName());
                f.createNewFile();
                //write the bytes in file
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(outStream.toByteArray());
                // remember close de FileOutput
                fo.close();
            }
        }
        catch (Exception ex)
        {

        }
    }

    public void escaner()
    {
        IntentIntegrator intent = new IntentIntegrator( this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);

        intent.setPrompt("Registrando documento de ingreso");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);
        intent.initiateScan();
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

    public void iniciarSpinnerTipoDoc() {
        tipoDocumento = new ArrayList<>();

        /*AreaRecinto area = new AreaRecinto();
        area.setAreaCod("cod");
        area.setAreaNombre("SELECCIONE TIPO DE DOCUMENTO");
        area.setAreaDescripcion("descripcion");
        area.setAreaEstado("estado");*/

        TipoDocumento tipoDocumentod = new TipoDocumento();
        tipoDocumentod.setTdoCod(0);
        tipoDocumentod.setTdoNombre("SELECCIONE TIPO DE DOCUMENTO");
        tipoDocumentod.setTdoDescripcion("descripcion");

        tipoDocumento.add(tipoDocumentod);
        adapterTipoDoc = new ArrayAdapter<TipoDocumento>(this, R.layout.style_spinner, tipoDocumento)
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
        adapterTipoDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoDocS.setAdapter(adapterTipoDoc);
        tipoDocS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*AreaRecinto areaRecinto = (AreaRecinto) parent.getSelectedItem();
                displayAreaRData(areaRecinto);*/
                TipoDocumento tipoDocumento = (TipoDocumento) parent.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchTipoDoc() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        TipoDocAPIs tipoDocAPIs = retrofit.create(TipoDocAPIs.class);
        Call<List<TipoDocumento>> call = tipoDocAPIs.listaTipoDoc(authorization);
        call.enqueue(new Callback<List<TipoDocumento>>() {
            @Override
            public void onResponse(Call <List<TipoDocumento>> call, retrofit2.Response<List<TipoDocumento>> response) {
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    tipoDocumento.add(response.body().get(i));
                }
            }
            @Override
            public void onFailure(Call <List<TipoDocumento>> call, Throwable t) {

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
        Visita visita = new Visita();
        AreaRecinto areaRecinto = (AreaRecinto) areaRecintoS.getSelectedItem();
        visita.setVisObs(observacion.getText().toString().toUpperCase());
        visita.setVisitante(visitanteRecibido);
        visita.setAreaRecinto(areaRecinto);

        Motivo motivo = (Motivo) motivoS.getSelectedItem();
        TipoDocumento tipoDocumento = (TipoDocumento) tipoDocS.getSelectedItem();

        DocumentoIngreso documentoIngreso = new DocumentoIngreso();
        documentoIngreso.setDoiImagen("");
        documentoIngreso.setDoiDocumento(codigoQR);
        documentoIngreso.setTipoDocumento(tipoDocumento);

        visita.setMotivo(motivo);
        visita.setDocumentoIngreso(documentoIngreso);

        //registrarIngreso(visita);

        Gson gson = new Gson();
        String descripcion = gson.toJson(visita);
        Log.d("msg912",""+descripcion);
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
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RegistrarIngreso2APIs uploadAPIs = retrofit.create(RegistrarIngreso2APIs.class);
        File file = new File(filePath);
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), descripcion);
        Call <String> call = uploadAPIs.uploadImage(part, description, authorization);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call <String> call, retrofit2.Response<String> response) {
                //Log.d("msg432",""+response.body());
                Gson gson = new Gson();
                String descripcion = gson.toJson(response.body());
                Log.d("msg512",""+descripcion);

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
            public void onFailure(Call <String>call, Throwable t) {
                Log.d("msg2",""+t);
            }
        });




        /*Retrofit retrofit = NetworkClient.getRetrofitClient(this);
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
        });*/
    }

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

}
