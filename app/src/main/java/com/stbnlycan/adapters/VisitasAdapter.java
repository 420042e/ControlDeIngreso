package com.stbnlycan.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Visita;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitasAdapter extends RecyclerView.Adapter<VisitasAdapter.ARV> implements Filterable {
    private List<Visita> eventosList;
    private List<Visita> eventosListFull;

    private OnVisitanteClickListener mListener;

    public VisitasAdapter(List<Visita> eventosList) {
        this.eventosList = eventosList;
        this.eventosListFull = new ArrayList<>(eventosList);
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.visitas_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ARV holder, int position) {
        Visita visita = eventosList.get(position);

        String dtStart = visita.getVisIngreso();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        SimpleDateFormat hh_mm_ss = new SimpleDateFormat("HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(dtStart);
            Log.d("msg23",""+date.getTime()+" "+hh_mm_ss.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.artNaam.setText(visita.getVisitante().getVteNombre()+" "+visita.getVisitante().getVteApellidos());
        holder.lugar.setText(visita.getVisIngreso()+ " "+ visita.getVisSalida());
        holder.empresaNombre.setText(visita.getVisitante().getEmpresa().getEmpNombre());

        holder.visita = eventosList.get(position);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Visita> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0)
            {
                filteredList.addAll(eventosListFull);
            }
            else
            {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Visita item : eventosListFull)
                {
                    //Log.d("msg2", ""+item.getVteNombre());
                    if(item.getVisitante().getVteNombre().toLowerCase().contains(filterPattern))
                    {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            eventosList.clear();
            eventosList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public int getItemCount() {
        return eventosList!=null?eventosList.size():0;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private TextView artNaam;
        private TextView lugar;
        private TextView empresaNombre;
        Visita visita;

        public ARV(@NonNull View itemView) {
            super(itemView);

            artNaam = itemView.findViewById(R.id.nombres);
            lugar = itemView.findViewById(R.id.nroCi);
            empresaNombre = itemView.findViewById(R.id.empresaNombre);
            itemView.setOnClickListener(this);
            visita = new Visita();
        }

        @Override
        public void onClick(View view)
        {
            mListener.onEventoClick(visita, getAdapterPosition());

        }

    }

    public void setOnVisitanteClickListener(OnVisitanteClickListener listener){
        mListener = listener;
    }

    public interface OnVisitanteClickListener
    {
        void onEventoClick(Visita visita, int position);
    }
}
