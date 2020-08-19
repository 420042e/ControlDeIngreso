package com.stbnlycan.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.stbnlycan.adapters.ACAdapter;
import com.stbnlycan.controldeingreso.NetworkClient;
import com.stbnlycan.controldeingreso.NuevoVisitanteActivity;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.controldeingreso.Visitantes;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_busqueda_ci, container, false);
        getDialog().setTitle("Búsqueda de CI");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //setCancelable(false);
        actv =  (AutoCompleteTextView) rootView.findViewById(R.id.ci2);
        btnNV =  (Button) rootView.findViewById(R.id.btnNV);

        visitantes = new ArrayList<>();
        //adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.select_dialog_item, nombres);
        actv.setThreshold(1);
        //actv.setAdapter(adapter);

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

        btnNV.setVisibility(View.INVISIBLE);
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

        /*actv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actv.showDropDown();
            }
        });*/

        return rootView;
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

    private void buscarXCI(String ci) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        BuscarXCIAPIs buscarXCIAPIs = retrofit.create(BuscarXCIAPIs.class);
        Call<List<Visitante>> call = buscarXCIAPIs.buscarXQR(ci);
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
                    //btnNV.setEnabled(false);
                    btnNV.setVisibility(View.VISIBLE);
                }
                else
                {
                    //btnNV.setEnabled(true);
                    btnNV.setVisibility(View.INVISIBLE);
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