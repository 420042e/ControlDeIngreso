package com.stbnlycan.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Visita;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DFError#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DFError extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button btnNF;
    private String mensajeRecibido;
    private TextView msg;

    public DFError() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DFError.
     */
    // TODO: Rename and change types and number of parameters
    public static DFError newInstance(String param1, String param2) {
        DFError fragment = new DFError();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_d_f_error, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        SpannableString title = new SpannableString("Atención");
        title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_rojo)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme).setTitle("Atención");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_d_f_error, null);
        builder.setView(rootView);

        setCancelable(false);
        btnNF =  (Button) rootView.findViewById(R.id.btnNF);
        msg =  (TextView) rootView.findViewById(R.id.msg);

        mensajeRecibido = (String) getArguments().getSerializable("mensaje");

        msg.setText(mensajeRecibido);

        btnNF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder.create();
    }
}
