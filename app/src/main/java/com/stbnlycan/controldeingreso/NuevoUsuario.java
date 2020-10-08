package com.stbnlycan.controldeingreso;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    @Email
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
    private String imagenObtenida;
    private final static int REQUEST_CODE_NE = 1;

    private String rol;
    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Recinto recintoRecibido;
    private Usuario usuario;

    static final int REQUEST_TAKE_PHOTO = 1;
    private Uri uri;
    private String currentPhotoPath;
    private FloatingActionButton fabNF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_usuario);

        recintoRecibido = (Recinto) getIntent().getSerializableExtra("recinto");

        validator = new Validator(this);
        validator.setValidationListener(this);

        setTitle("Nuevo usuario");
        visitanteIV = findViewById(R.id.visitanteIV);
        usernameET = findViewById(R.id.username);
        passwordET = findViewById(R.id.password);
        emailET = findViewById(R.id.email);
        fullnameET = findViewById(R.id.fullname);
        occupationET = findViewById(R.id.occupation);
        phoneET = findViewById(R.id.phone);
        addressET = findViewById(R.id.address);
        rolS = findViewById(R.id.rol);
        fabNF = findViewById(R.id.fabNF);

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

        fabNF.setOnClickListener(new View.OnClickListener() {
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),"com.stbnlycan.controldeingreso.fileprovider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            uri = photoURI;
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

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("msg42545",""+requestCode+" "+resultCode+" "+REQUEST_IMAGE_CAPTURE+" "+RESULT_OK);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            redimensionarImg();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                //if(data.getExtras() != null)
                if(data != null)
                {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    visitanteIV.setImageBitmap(imageBitmap);
                    visitanteIV.getLayoutParams().width = width;
                    visitanteIV.getLayoutParams().height = width;
                    visitanteIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                else
                {
                    visitanteIV.setImageURI(uri);
                    visitanteIV.getLayoutParams().width = width;
                    visitanteIV.getLayoutParams().height = width;
                    visitanteIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
            else
            {
                Log.d("msg554","hola 2");
                visitanteIV.setImageURI(uri);
                visitanteIV.getLayoutParams().width = width;
                visitanteIV.getLayoutParams().height = width;
                visitanteIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    public void redimensionarImg()
    {
        try
        {
            /*String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName,".jpg",storageDir);
            currentPhotoPath = image.getAbsolutePath();*/


            // we'll start with the original picture already open to a file
            File imgFileOrig = new File(imagenObtenida); //change "getPic()" for whatever you need to open the image file.
            Bitmap b = BitmapFactory.decodeFile(imgFileOrig.getAbsolutePath());
            // original measurements
            int origWidth = b.getWidth();
            int origHeight = b.getHeight();

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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        Bitmap OutImage = Bitmap.createScaledBitmap(inImage, 1000, 1000,true);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), OutImage, "Title", "From Camera");
        return Uri.parse(path);

        /*ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"From Camera");
        Uri path=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        return path;*/
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
            fabNF.setEnabled(false);
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
                Intent intent = new Intent(NuevoUsuario.this, LoginActivity.class);
                startActivity(intent);
                finish();
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
                Log.d("msg1",""+response);
                try
                {
                    //esto funciona cuando es responsebody
                    //Log.d("msg2",""+response.body().string());
                }
                catch (Exception ex)
                {
                    Log.d("msg4",""+ex);
                }

                //usuario.setPic(response.body().getPic());
                //usuario.setPic(imagenObtenida);

                Toast.makeText(getApplicationContext(), "Se guardó el nuevo usuario", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("usuarioResult", response.body());
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
