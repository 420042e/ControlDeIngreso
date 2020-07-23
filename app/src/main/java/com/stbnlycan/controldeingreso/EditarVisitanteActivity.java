package com.stbnlycan.controldeingreso;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.squareup.picasso.Picasso;
import com.stbnlycan.fragments.LoadingFragment;
import com.stbnlycan.interfaces.EmpresaAPIs;
import com.stbnlycan.interfaces.SubirImagenAPIs;
import com.stbnlycan.interfaces.TipoVisitanteAPIs;
import com.stbnlycan.interfaces.VisitanteAPIs;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.TipoVisitante;
import com.stbnlycan.models.Visitante;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
    @NotEmpty
    private EditText direccionET;
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
        direccionET = findViewById(R.id.direccion);
        empresaS = findViewById(R.id.empresa);
        tipoVisitanteS = findViewById(R.id.tipo_visitante);

        btnNF = findViewById(R.id.btnNF);

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

        Picasso.get().load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + visitanteRecibido.getVteImagen()).centerCrop().resize(500, 500).into(visitanteIV);
        //Picasso.get().load(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/prueba.jpg")).centerCrop().resize(150, 150).into(visitanteIV);



        ciET.setText(visitanteRecibido.getVteCi());
        nombreET.setText(visitanteRecibido.getVteNombre());
        apellidosET.setText(visitanteRecibido.getVteApellidos());
        telcelET.setText(visitanteRecibido.getVteTelefono());
        emailET.setText(visitanteRecibido.getVteCorreo());
        direccionET.setText(visitanteRecibido.getVteDireccion());
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
    }

    public void iniciarSpinnerEmpresa() {
        empresas = new ArrayList<>();
        Empresa empresa = new Empresa();
        empresa.setEmpCod("cod");
        empresa.setEmpNombre("Selecciona una empresa");
        empresa.setEmpObs("obs");
        empresas.add(empresa);
        adapterEmpresa = new ArrayAdapter<Empresa>(this, android.R.layout.simple_spinner_dropdown_item, empresas);
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
        tipoVisitante.setTviNombre("Selecciona tipo de visitante");
        tipoVisitante.setTviDescripcion("obs");
        tipoVisitante.setHorEstado("estado");
        tiposVisitante.add(tipoVisitante);
        adapterTipoVisitante = new ArrayAdapter<TipoVisitante>(this, android.R.layout.simple_spinner_dropdown_item, tiposVisitante);
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
            visitanteIV.setImageBitmap(imageBitmap);

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));

            //showLoadingwDialog();
            Gson gson = new Gson();
            String descripcion = gson.toJson(visitanteRecibido);

            imagenObtenida = finalFile.toString();
            //imagenObtenida = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/prueba.jpg";

            subirImagen(descripcion);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ev, menu);
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

    private void fetchDataEmpresa() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        EmpresaAPIs empresaAPIs = retrofit.create(EmpresaAPIs.class);
        Call<List<Empresa>> call = empresaAPIs.listaEmpresas();
        call.enqueue(new Callback<List<Empresa>>() {
            @Override
            public void onResponse(Call <List<Empresa>> call, retrofit2.Response<List<Empresa>> response) {
                //recintos = response.body();
                int pos = -1;
                for(int i = 0 ; i < response.body().size() ; i++)
                {
                    empresas.add(response.body().get(i));
                    if(response.body().get(i).getEmpCod().equals(visitanteRecibido.getEmpresa().getEmpCod()))
                    {
                        pos = i+1;
                    }
                }
                //adapter.notifyDataSetChanged();
                empresaS.setSelection(pos, true);

            }
            @Override
            public void onFailure(Call <List<Empresa>> call, Throwable t) {

            }
        });
    }

    private void fetchDataTipoVisitante() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        TipoVisitanteAPIs tipoVisitanteAPIs = retrofit.create(TipoVisitanteAPIs.class);
        Call<List<TipoVisitante>> call = tipoVisitanteAPIs.listaTipoVisitante();
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
        Call<Visitante> call = visitanteAPIs.editarVisitante(visitante);
        call.enqueue(new Callback <Visitante> () {
            @Override
            public void onResponse(Call <Visitante> call, Response<Visitante> response) {
                Visitante visitanteRecibido = response.body();
                Toast.makeText(getApplicationContext(), "El visitante fué actualizado", Toast.LENGTH_SHORT).show();
                //visitante.setVteEstado("ACT");

                Intent intent = new Intent();
                intent.putExtra("visitanteResult", visitante);
                intent.putExtra("position", position);
                setResult(RESULT_OK, intent);
                finish();
            }
            @Override
            public void onFailure(Call <Visitante> call, Throwable t) {

            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        //Toast.makeText(this, "We got it right!", Toast.LENGTH_SHORT).show();
        if(hasNullOrEmptyDrawable(visitanteIV))
        {
            Toast.makeText(this, "Debes añadir una fotografía", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Visitante visitante = new Visitante();
            visitante = new Visitante();
            visitante.setVteCi(ciET.getText().toString());
            visitante.setVteCorreo(emailET.getText().toString());

            visitante.setVteImagen(visitanteRecibido.getVteImagen());

            visitante.setVteNombre(nombreET.getText().toString());
            visitante.setVteApellidos(apellidosET.getText().toString());
            visitante.setVteTelefono(telcelET.getText().toString());
            visitante.setVteDireccion(direccionET.getText().toString());

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
        Call<Visitante> call = subirImagenAPIs.subirImagen(part, description);
        call.enqueue(new Callback<Visitante>() {
            @Override
            public void onResponse(Call <Visitante> call, retrofit2.Response <Visitante> response) {

                Visitante visitanteCB = response.body();

                Toast.makeText(getApplicationContext(), "Se guardó la nueva imágen", Toast.LENGTH_SHORT).show();

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
