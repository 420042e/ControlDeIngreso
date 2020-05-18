package com.stbnlycan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Accion;

import java.util.List;

public class RecintoAdapter extends RecyclerView.Adapter<RecintoAdapter.ARV> {
    private List<Accion> artisList;

    private OnEventoListener onEventoListener;

    public RecintoAdapter(List<Accion> artisList) {
        this.artisList = artisList;
        this.onEventoListener = onEventoListener;
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.acciones_list, parent, false), onEventoListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ARV holder, int position) {
        Accion radios = artisList.get(position);
        holder.artNaam.setText(radios.getNombre());
        holder.artimg.setImageResource(radios.getRecurso());
    }

    @Override
    public int getItemCount() {
        return artisList!=null?artisList.size():0;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private ImageView artimg;
        private TextView artNaam;
        OnEventoListener onEventoListener;

        private static final int NOTIFICATION_ID_OPEN_ACTIVITY = 9;
        private static final int NOTIFICATION_ID_CUSTOM_BIG = 9;

        public ARV(@NonNull View itemView, OnEventoListener onEventoListener) {
            super(itemView);

            artimg = itemView.findViewById(R.id.artthumb);
            artNaam = itemView.findViewById(R.id.artname);
            this.onEventoListener = onEventoListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            onEventoListener.onEventoDetailsClick(getAdapterPosition());
        }

    }

    public void setOnEventoClickListener(OnEventoListener listener){
        onEventoListener = listener;
    }

    public interface OnEventoListener
    {
        void onEventoDetailsClick(int position);
    }

}
