package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.squareup.picasso.Picasso;
import com.stbnlycan.adapters.DOIAdapter;
import com.stbnlycan.adapters.RecintoAdapter;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.TipoDocAPIs;
import com.stbnlycan.models.DocumentoIngreso;
import com.stbnlycan.models.TipoDocumento;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class DocumentosIngreso extends AppCompatActivity implements DOIAdapter.OnDOIClickListener, Validator.ValidationListener{

    private Toolbar toolbar;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private RecyclerView recyclerView;
    private DOIAdapter doiAdapter;
    private ArrayList<DocumentoIngreso> dois;

    private ArrayList<TipoDocumento> tipoDocumento;
    private ArrayAdapter<TipoDocumento> adapterTipoDoc;

    @Select
    private Spinner tipoDocS;

    private EditText doiDocumentoET;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private String imagenObtenida;

    private Uri uri;

    private ImageView doiImagenIV;
    private Button btnAdd;
    private String qrObtenido;

    private Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentos_ingreso);

        validator = new Validator(this);
        validator.setValidationListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        tipoDocS = findViewById(R.id.tipoDoc);
        doiDocumentoET = findViewById(R.id.doiDocumentoET);
        doiImagenIV = findViewById(R.id.doiImagenIV);
        btnAdd = findViewById(R.id.btnAdd);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);

        setTitle("Adjuntar documentos de ingreso");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        doiDocumentoET.setFocusable(false);

        dois = new ArrayList<>();

        doiAdapter = new DOIAdapter(dois);
        doiAdapter.setOnDOIClickListener(DocumentosIngreso.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(doiAdapter);

        iniciarSpinnerTipoDoc();
        fetchTipoDoc();

        doiDocumentoET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escaner();
            }
        });

        doiImagenIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            uri = FileProvider.getUriForFile(getApplicationContext(), "com.stbnlycan.controldeingreso.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            Log.d("msg4214", "" + photoFile);
                            imagenObtenida = photoFile.toString();
                        } else {
                            uri = FileProvider.getUriForFile(getApplicationContext(), "com.stbnlycan.controldeingreso.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            Log.d("msg4215", "" + photoFile);
                            imagenObtenida = photoFile.toString();
                        }
                    }
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
                /*File f = new File(imagenObtenida);
                DocumentoIngreso doi = new DocumentoIngreso();
                doi.setDoiImagen(imagenObtenida);
                doi.setDoiDocumento(qrObtenido);
                TipoDocumento tipoDocumento = (TipoDocumento) tipoDocS.getSelectedItem();
                doi.setTipoDocumento(tipoDocumento);
                dois.add(0, doi);
                recyclerView.smoothScrollToPosition(0);
                doiAdapter.notifyItemInserted(0);*/
            }
        });
    }

    public void escaner() {
        IntentIntegrator intent = new IntentIntegrator(DocumentosIngreso.this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);

        intent.setPrompt("Registrando QR documento de ingreso");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);
        intent.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*Log.d("msg6545 ",""+imagenObtenida);
        File f = new File(imagenObtenida);
        Picasso.get().load(f).resize(300, 300).into(doiImagenIV);*/

        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelaste el escaneo de ingreso", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, ""+result.getContents(), Toast.LENGTH_LONG).show();
                //showLoadingwDialog();
                //registrarSalida(result.getContents());
                //Log.d("msg1234", "result.getContents()"+result.getContents());
                qrObtenido = result.getContents();
                doiDocumentoET.setText(qrObtenido);
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("msg6545 ", "" + imagenObtenida);
            File f = new File(imagenObtenida);
            Picasso.get().load(f).resize(300, 300).into(doiImagenIV);

            /*Bitmap bmImg = BitmapFactory.decodeFile(f.getAbsolutePath());
            doiImagenIV.setImageBitmap(bmImg);*/

            redimensionarImg();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //doiImagenIV.setImageBitmap(imageBitmap);
                    /*doiImagenIV.getLayoutParams().width = width;
                    doiImagenIV.getLayoutParams().height = width;
                    doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);*/
                } else {
                    //doiImagenIV.setImageURI(uri);
                    /*doiImagenIV.getLayoutParams().width = width;
                    doiImagenIV.getLayoutParams().height = width;
                    doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);*/
                }
            } else {
                Log.d("msg554", "hola 2");
                //doiImagenIV.setImageURI(uri);
                /*doiImagenIV.getLayoutParams().width = width;
                doiImagenIV.getLayoutParams().height = width;
                doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);*/
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    public void redimensionarImg() {
        try {
            // we'll start with the original picture already open to a file
            File imgFileOrig = new File(imagenObtenida); //change "getPic()" for whatever you need to open the image file.
            Bitmap b = BitmapFactory.decodeFile(imgFileOrig.getAbsolutePath());
            // original measurements
            int origWidth = b.getWidth();
            int origHeight = b.getHeight();

            //Toast.makeText(getApplicationContext(), "origWidth "+origWidth+" origHeight "+origHeight, Toast.LENGTH_LONG).show();

            final int destWidth = 600;//or the width you need

            if (origWidth > destWidth) {
                // picture is wider than we want it, we calculate its target height
                int destHeight = origHeight / (origWidth / destWidth);
                // we create an scaled bitmap so it reduces the image, not just trim it
                Bitmap b2 = Bitmap.createScaledBitmap(b, destWidth, destHeight, false);

                if (origWidth > origHeight) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    b2 = Bitmap.createBitmap(b2, 0, 0, b2.getWidth(), b2.getHeight(), matrix, true);
                }

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // compress to the format you want, JPEG, PNG...
                // 70 is the 0-100 quality percentage
                b2.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
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
        } catch (Exception ex) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            case R.id.action_confirmar:
                Intent intentR = new Intent();
                intentR.putExtra("doisResult", dois);
                setResult(RESULT_OK, intentR);
                finish();
                return false;
            case R.id.action_salir:
                cerrarSesion();
                Intent intent = new Intent(DocumentosIngreso.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    public void iniciarSpinnerTipoDoc() {
        tipoDocumento = new ArrayList<>();

        TipoDocumento tipoDocumentod = new TipoDocumento();
        tipoDocumentod.setTdoCod(0);
        tipoDocumentod.setTdoNombre("SELECCIONE TIPO DE DOCUMENTO");
        tipoDocumentod.setTdoDescripcion("descripcion");

        tipoDocumento.add(tipoDocumentod);
        adapterTipoDoc = new ArrayAdapter<TipoDocumento>(this, R.layout.style_spinner, tipoDocumento) {
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
            public void onResponse(Call<List<TipoDocumento>> call, retrofit2.Response<List<TipoDocumento>> response) {
                for (int i = 0; i < response.body().size(); i++) {
                    tipoDocumento.add(response.body().get(i));
                }
            }

            @Override
            public void onFailure(Call<List<TipoDocumento>> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_doi, menu);
        return true;
    }

    private void cerrarSesion() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        LogoutAPIs logoutAPIs = retrofit.create(LogoutAPIs.class);
        Call<Void> call = logoutAPIs.logout(pref.getString("access_token", null));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                editor.putString("access_token", "");
                editor.putString("token_type", "");
                editor.putString("rol", "");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Sesión finalizada", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("msg4125", "hola " + t.toString());
            }
        });
    }

    @Override
    public void OnDOIClick(DocumentoIngreso doi) {
        Log.d("msg431", doi.getTipoDocumento().getTdoNombre());
    }

    @Override
    public void onValidationSucceeded() {
        File f = new File(imagenObtenida);
        DocumentoIngreso doi = new DocumentoIngreso();
        doi.setDoiImagen(imagenObtenida);
        doi.setDoiDocumento(qrObtenido);
        TipoDocumento tipoDocumento = (TipoDocumento) tipoDocS.getSelectedItem();
        doi.setTipoDocumento(tipoDocumento);
        dois.add(0, doi);
        recyclerView.smoothScrollToPosition(0);
        doiAdapter.notifyItemInserted(0);

        //Reiniciar componentes
        tipoDocS.setSelection(0, true);
        doiDocumentoET.setText("");
        doiImagenIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_a_photo_black_48dp));

        /*Bitmap bmImg = BitmapFactory.decodeFile(f.getAbsolutePath());
        doiImagenIV.setImageBitmap(bmImg);*/
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if (view instanceof Spinner) {
                ((TextView) ((Spinner) view).getSelectedView()).setError("Este campo es requerido");
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
