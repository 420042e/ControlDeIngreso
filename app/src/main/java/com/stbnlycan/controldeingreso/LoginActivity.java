package com.stbnlycan.controldeingreso;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.stbnlycan.adapters.RecintosAdapter;
import com.stbnlycan.interfaces.LoginAPIs;
import com.stbnlycan.interfaces.RecintoXUsuarioAPIs;
import com.stbnlycan.interfaces.RecintosAPIs;
import com.stbnlycan.models.Error;
import com.stbnlycan.models.ErrorToken;
import com.stbnlycan.models.Recinto;
import com.stbnlycan.models.Token;
import com.stbnlycan.models.Usuario;
import com.stbnlycan.models.Visita;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private FrameLayout progressBarHolder;
    private String authorization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

        Button boton_ingreso = (Button) findViewById(R.id.btn_ingreso);
        boton_ingreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();*/
                iniciarSesion();
            }
        });
    }

    private void iniciarSesion() {
        progressBarHolder.setVisibility(View.VISIBLE);
        String authString = "ingresoVisitantes:albosalpz01codex";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encodedAuthString);
        Map<String, String> fields = new HashMap<>();
        fields.put("grant_type", "password");
        fields.put("username", username.getText().toString());
        fields.put("password", password.getText().toString());

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        LoginAPIs loginAPIs = retrofit.create(LoginAPIs.class);
        Call<JsonObject> call = loginAPIs.login(headers, fields);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                //progressBarHolder.setVisibility(View.GONE);
                if (response.code() == 400) {
                    progressBarHolder.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "No existe el usuario", Toast.LENGTH_LONG).show();
                }
                else
                {
                    String jsonString = response.body().toString();
                    if (jsonString.contains("access_token")) {
                        Token token = new Gson().fromJson(jsonString, Token.class);
                        Toast.makeText(getApplicationContext(), "Acceso correcto", Toast.LENGTH_LONG).show();
                        editor.putString("access_token", token.getAccess_token());
                        editor.putString("token_type", token.getToken_type());
                        editor.commit();

                        authorization = token.getToken_type() + " " + token.getAccess_token();

                        JWT jwt = new JWT(token.getAccess_token());
                        Map<String, Claim> allClaims = jwt.getClaims();
                        for (Map.Entry<String, Claim> entry : allClaims.entrySet()) {
                            Log.d("msg8645",""+entry.getKey() + "/" + entry.getValue().asString());
                        }

                        //iniciarMainActivity(jwt.getClaim("user_name").asString());
                        buscaRecintosXUsuario(jwt.getClaim("user_name").asString());
                    }
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

    private void buscaRecintosXUsuario(String user_name) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        RecintoXUsuarioAPIs recintoXUsuarioAPIs = retrofit.create(RecintoXUsuarioAPIs.class);
        Call<Usuario> call = recintoXUsuarioAPIs.recintoXUsuario(user_name, authorization);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call <Usuario> call, retrofit2.Response<Usuario> response) {
                iniciarRecintoActivity(response.body().getRecinto());
            }
            @Override
            public void onFailure(Call <Usuario> call, Throwable t) {

            }
        });
    }

    public void iniciarRecintoActivity(Recinto recinto)
    {
        Intent intent = new Intent(LoginActivity.this, RecintoActivity.class);
        intent.putExtra("recinto", recinto);
        startActivity(intent);
        finish();
    }

    public void iniciarMainActivity(String user_name)
    {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user_name", user_name);
        startActivity(intent);
        finish();
    }

}
