package com.stbnlycan.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.stbnlycan.adapters.ACAdapter;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;

public class DFIngreso extends DialogFragment {

    private ArrayList<Visitante> visitantes;
    private ACAdapter adapter;
    private Button btnNF;
    private Visita visitaRecibida;
    private TextView msg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.df_ingreso, container, false);
        getDialog().setTitle("Atención");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setCancelable(false);
        btnNF =  (Button) rootView.findViewById(R.id.btnNF);
        msg =  (TextView) rootView.findViewById(R.id.msg);

        visitantes = new ArrayList<>();

        visitaRecibida = (Visita) getArguments().getSerializable("visita");

        msg.setText("Ingreso registrado:\r\n"+visitaRecibida.getVisitante().getVteNombre()+ " " + visitaRecibida.getVisitante().getVteApellidos());

        btnNF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}