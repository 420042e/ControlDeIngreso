package com.stbnlycan.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.stbnlycan.adapters.ACAdapter;
import com.stbnlycan.controldeingreso.NetworkClient;
import com.stbnlycan.controldeingreso.NuevoVisitanteActivity;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.interfaces.BuscarXCIAPIs;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class BusquedaCiDialogFragment extends DialogFragment {

    private OnBusquedaCiListener onBusquedaCiListener;
    private AutoCompleteTextView actv;
    private ArrayList<Visitante> visitantes;
    private ACAdapter adapter;
    private Button btnNV;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_busqueda_ci, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString("email")))
            editText.setText(getArguments().getString("ci"));*/
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("Búsqueda de CI");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_busqueda_ci, null);
        builder.setView(view);

        actv =  (AutoCompleteTextView) view.findViewById(R.id.ci2);
        btnNV =  (Button) view.findViewById(R.id.btnNV);

        visitantes = new ArrayList<>();

        pref = getActivity().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);

        actv.setThreshold(1);

        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarXCI(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                onBusquedaCiListener.onBusquedaCiListener(visitantes.get(position));
                //dismiss();
            }
        });

        btnNV.setEnabled(false);
        //btnNV.setVisibility(View.INVISIBLE);
        btnNV.setAlpha(0.5f);
        btnNV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NuevoVisitanteActivity.class);
                intent.putExtra("ci", actv.getText().toString());
                //startActivityForResult(intent, REQUEST_CODE_NV);
                startActivity(intent);
                dismiss();
            }
        });
        return builder.create();
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

    private void buscarXCI(String ci) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        BuscarXCIAPIs buscarXCIAPIs = retrofit.create(BuscarXCIAPIs.class);
        Call<List<Visitante>> call = buscarXCIAPIs.buscarXQR(ci, authorization);
        call.enqueue(new Callback<List<Visitante>>() {
            @Override
            public void onResponse(Call <List<Visitante>> call, retrofit2.Response<List<Visitante>> response) {
                List<Visitante> visitantesRecibidos = response.body();
                visitantes.clear();
                /*for(int i=0;i<visitantesRecibidos.size();i++)
                {
                    visitantes.add(visitantesRecibidos.get(i));
                }*/
                if(visitantesRecibidos.size()==0)
                {
                    btnNV.setEnabled(true);
                    //btnNV.setVisibility(View.VISIBLE);
                    btnNV.setAlpha(1.0f);
                }
                else
                {
                    btnNV.setEnabled(false);
                    //btnNV.setVisibility(View.INVISIBLE);
                    btnNV.setAlpha(0.5f);
                    for(int i=0;i<visitantesRecibidos.size();i++)
                    {
                        visitantes.add(visitantesRecibidos.get(i));
                    }
                    actv.showDropDown();
                }
                adapter = new ACAdapter(getActivity(), R.layout.custom_row, visitantes);
                actv.setAdapter(adapter);
            }
            @Override
            public void onFailure(Call <List<Visitante>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface DialogListener {
        void onFinishEditDialog(String inputText);
    }

    public void setOnEventoClickListener(OnBusquedaCiListener listener){
        onBusquedaCiListener = listener;
    }

    public interface OnBusquedaCiListener
    {
        void onBusquedaCiListener(Visitante visitante);
    }

}