package com.stbnlycan.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.stbnlycan.controldeingreso.EditarVisitanteActivity;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.controldeingreso.RecintoActivity;
import com.stbnlycan.controldeingreso.Visitantes;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;

public class VisitanteListAdapter extends ArrayAdapter<Visitante> {
    private Context mContext;
    int mResource;

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public interface OnItemClickListener
    {
        void OnItemClick(Visitante visitante);
    }

    public VisitanteListAdapter(Context context, int resource, ArrayList<Visitante> objetos) {
        super(context, resource, objetos);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        String nombre = getItem(position).getVteNombre();
        String apellidos = getItem(position).getVteApellidos();
        Visitante visitante = new Visitante();
        visitante.setVteNombre(nombre);
        visitante.setVteApellidos(apellidos);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvNombre = convertView.findViewById(R.id.nombre);
        tvNombre.setText(nombre);

        TextView tvApellidos = convertView.findViewById(R.id.apellidos);
        tvApellidos.setText(apellidos);

        Button btnEditar = convertView.findViewById(R.id.editar);
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.OnItemClick(getItem(position));
            }
        });

        return convertView;
    }


}
