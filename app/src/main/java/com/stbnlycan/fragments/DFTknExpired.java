package com.stbnlycan.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.auth0.android.jwt.JWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.stbnlycan.adapters.ACAdapter;
import com.stbnlycan.controldeingreso.LoginActivity;
import com.stbnlycan.controldeingreso.NetworkClient;
import com.stbnlycan.controldeingreso.NuevoVisitanteActivity;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.interfaces.LoginAPIs;
import com.stbnlycan.interfaces.LogoutAPIs;
import com.stbnlycan.interfaces.RecintoXUsuarioAPIs;
import com.stbnlycan.models.Token;
import com.stbnlycan.models.Usuario;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class DFTknExpired extends DialogFragment implements Validator.ValidationListener {

    private ArrayList<Visitante> visitantes;
    private ACAdapter adapter;
    private Button btnNF;
    private TextView msg;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @NotEmpty
    private EditText username;
    @NotEmpty
    private EditText password;
    private ProgressBar progressBar;
    private Validator validator;

    public OnInputListener onInputListener;

    public void setOnInputListener(OnInputListener onInputListener) {
        this.onInputListener = onInputListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_busqueda_ci, container, false);
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("La sesión ha caducado");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.df_tkn_expired, null);
        builder.setView(rootView);

        validator = new Validator(this);
        validator.setValidationListener(this);

        pref = getActivity().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);


        setCancelable(false);
        btnNF =  (Button) rootView.findViewById(R.id.btnNF);
        msg =  (TextView) rootView.findViewById(R.id.msg);
        username = (EditText) rootView.findViewById(R.id.username);
        password = (EditText) rootView.findViewById(R.id.password);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        visitantes = new ArrayList<>();

        //msg.setText("No se encontró el visitante en la base de datos...\r\n¿Deseas agregar un nuevo visitante?");

        btnNF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarLogin();
                //validator.validate();
            }
        });
        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString("email")))
            editText.setText(getArguments().getString("ci"));*/
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("API123", "onCreate");
        boolean setFullScreen = false;
        if (getArguments() != null) {
            setFullScreen = getArguments().getBoolean("fullScreen");
        }
        if (setFullScreen)
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void iniciarLogin()
    {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        //dismiss();
        getActivity().finish();
    }

    @Override
    public void onValidationSucceeded() {
        anularToken();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getActivity());
            // Display error messages
            if (view instanceof EditText) {
                //((EditText) view).setError(message);
                ((EditText) view).setError("Este campo es requerido");
            }
            else if (view instanceof Spinner) {
                //((TextView) ((Spinner) view).getSelectedView()).setError(message);
                ((TextView) ((Spinner) view).getSelectedView()).setError("Este campo es requerido");
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void anularToken() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        LogoutAPIs logoutAPIs = retrofit.create(LogoutAPIs.class);
        Call<Void> call = logoutAPIs.logout(pref.getString("access_token", null));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call <Void> call, retrofit2.Response<Void> response) {
                editor.putString("access_token", "");
                editor.putString("token_type", "");
                editor.putString("rol", "");
                editor.apply();
                iniciarSesion();
            }
            @Override
            public void onFailure(Call <Void> call, Throwable t) {
                Log.d("msg4125","hola "+t.toString());
            }
        });
    }

    private void iniciarSesion() {
        progressBar.setVisibility(View.VISIBLE);
        String authString = "ingresoVisitantes:albosalpz01codex";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encodedAuthString);
        Map<String, String> fields = new HashMap<>();
        fields.put("grant_type", "password");
        fields.put("username", username.getText().toString());
        fields.put("password", password.getText().toString());
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        LoginAPIs loginAPIs = retrofit.create(LoginAPIs.class);
        Call<JsonObject> call = loginAPIs.login(headers, fields);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call <JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (response.code() == 400) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "No existe el usuario", Toast.LENGTH_LONG).show();
                }
                else
                {
                    String jsonString = response.body().toString();
                    if (jsonString.contains("access_token")) {
                        Token token = new Gson().fromJson(jsonString, Token.class);
                        Toast.makeText(getActivity(), "Acceso correcto", Toast.LENGTH_LONG).show();
                        editor.putString("access_token", token.getAccess_token());
                        editor.putString("token_type", token.getToken_type());
                        editor.apply();
                        authorization = token.getToken_type() + " " + token.getAccess_token();
                        JWT jwt = new JWT(token.getAccess_token());
                        buscaRecintosXUsuario(jwt.getClaim("user_name").asString());
                    }
                }
            }
            @Override
            public void onFailure(Call <JsonObject> call, Throwable t) {
                Log.d("msg435","hola "+t.toString());
            }
        });
    }

    private void buscaRecintosXUsuario(String user_name) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        RecintoXUsuarioAPIs recintoXUsuarioAPIs = retrofit.create(RecintoXUsuarioAPIs.class);
        Call<Usuario> call = recintoXUsuarioAPIs.recintoXUsuario(user_name, authorization);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call <Usuario> call, retrofit2.Response<Usuario> response) {
                editor.putString("rol", response.body().getRol().getNombre());
                editor.apply();
                //Actualizar activity
                onInputListener.sendInput(true);
                dismiss();
            }
            @Override
            public void onFailure(Call <Usuario> call, Throwable t) {
                Log.d("msg4335","hola "+t.toString());
            }
        });
    }

    public interface OnInputListener {
        void sendInput(boolean estado);
    }

}