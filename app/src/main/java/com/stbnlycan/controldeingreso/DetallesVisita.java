package com.stbnlycan.controldeingreso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stbnlycan.models.Visita;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DetallesVisita extends AppCompatActivity {

    private Toolbar toolbar;
    private Visita visitaRecibida;

    private ImageView visitanteIV;
    @NotEmpty
    private EditText nombreET;
    @NotEmpty
    private EditText apellidosET;
    @NotEmpty
    private EditText telcelET;
    @NotEmpty
    private EditText empresa;
    @NotEmpty
    private EditText tipoVisitante;
    @NotEmpty
    private EditText observacion;
    @NotEmpty
    private EditText fIngreso;
    @NotEmpty
    private EditText fSalida;
    @NotEmpty
    private EditText motivo;
    @NotEmpty
    private EditText tipoDoc;
    @NotEmpty
    private EditText doiDocumento;

    private ImageView doiImagenIV;

    private TextInputLayout tilfSalida;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_visita);

        setTitle("Detalles");
        toolbar = findViewById(R.id.toolbar);
        visitanteIV = findViewById(R.id.visitanteIV);
        nombreET = findViewById(R.id.nombre);
        apellidosET = findViewById(R.id.apellidos);
        telcelET = findViewById(R.id.telcel);
        empresa = findViewById(R.id.empresa);
        tipoVisitante = findViewById(R.id.tipoVisitante);
        fIngreso = findViewById(R.id.fIngreso);
        fSalida = findViewById(R.id.fSalida);
        observacion = findViewById(R.id.observacion);
        tilfSalida = findViewById(R.id.tilfSalida);
        doiDocumento = findViewById(R.id.doiDocumento);
        doiImagenIV = findViewById(R.id.doiImagenIV);

        motivo = findViewById(R.id.motivo);
        tipoDoc = findViewById(R.id.tipoDoc);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        visitaRecibida = (Visita) getIntent().getSerializableExtra("visita");

        Gson gson = new Gson();
        String descripcion = gson.toJson(visitaRecibida);
        Log.d("msg915",""+descripcion);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);

        /*OkHttpClient client = new OkHttpClient.Builder()
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
        picasso.load("http://190.129.90.115:8083/ingresoVisitantes/visitante/mostrarFoto?foto=" + visitaRecibida.getVisitante().getVteImagen()).into(visitanteIV);*/

        String dtIngreso = visitaRecibida.getVisIngreso();
        String dtSalida = visitaRecibida.getVisSalida();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        SimpleDateFormat dd_MM_yyyy = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat hh_mm = new SimpleDateFormat("HH:mm");
        Date date = null;
        Date date2 = null;
        try {
            date = format.parse(dtIngreso);
            if(dtSalida == null)
            {
                fIngreso.setText(dd_MM_yyyy.format(date)+" "+hh_mm.format(date));
                //fSalida.setText("Con salida");
                tilfSalida.setVisibility(View.GONE);
            }
            else
            {
                date2 = format.parse(dtSalida);
                fIngreso.setText(dd_MM_yyyy.format(date)+" "+hh_mm.format(date));
                fSalida.setText(dd_MM_yyyy.format(date2)+" "+hh_mm.format(date2));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        nombreET.setText(visitaRecibida.getVisitante().getVteNombre());
        apellidosET.setText(visitaRecibida.getVisitante().getVteApellidos());
        telcelET.setText(visitaRecibida.getVisitante().getVteTelefono());
        empresa.setText(visitaRecibida.getVisitante().getEmpresa().getEmpNombre());
        tipoVisitante.setText(visitaRecibida.getVisitante().getTipoVisitante().getTviNombre());
        /*fIngreso.setText(visitaRecibida.getVisIngreso());
        fSalida.setText(visitaRecibida.getVisSalida());*/
        observacion.setText(visitaRecibida.getVisObs());

        motivo.setText(visitaRecibida.getMotivo().getMvoNombre());
        /*tipoDoc.setText(visitaRecibida.getDocumentosIngreso().get(0).getTipoDocumento().getTdoNombre());
        doiDocumento.setText(visitaRecibida.getDocumentosIngreso().get(0).getDoiDocumento());*/


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        /*OkHttpClient client = new OkHttpClient.Builder()
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
        picasso.load("http://190.129.90.115:8083/ingresoVisitantes/documentoIngreso/mostrarFoto?foto=" + visitaRecibida.getDocumentosIngreso().get(0).getDoiImagen()).resize(width, width).into(doiImagenIV);*/

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
