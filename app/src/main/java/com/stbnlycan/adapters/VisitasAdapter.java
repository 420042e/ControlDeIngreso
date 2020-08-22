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

        String dtIngreso = visita.getVisIngreso();
        String dtSalida = visita.getVisSalida();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        SimpleDateFormat dd_MM_yyyy = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat hh_mm = new SimpleDateFormat("HH:mm");
        String hora_fecha="";
        Date date = null;
        Date date2 = null;
        try {
            date = format.parse(dtIngreso);
            if(dtSalida == null)
            {
                hora_fecha = "ING: "+dd_MM_yyyy.format(date)+" "+hh_mm.format(date);
                holder.fIngreso.setText("ING: "+dd_MM_yyyy.format(date)+" "+hh_mm.format(date));
            }
            else
            {
                date2 = format.parse(dtSalida);
                hora_fecha = "ING: "+dd_MM_yyyy.format(date)+" "+hh_mm.format(date)+" SAL: "+dd_MM_yyyy.format(date2)+" "+hh_mm.format(date2);
                holder.fIngreso.setText("ING: "+dd_MM_yyyy.format(date)+" "+hh_mm.format(date));
                holder.fSalida.setText("SAL: "+dd_MM_yyyy.format(date2)+" "+hh_mm.format(date2));
            }
            //hora_fecha = hh_mm.format(date)+" "+dd_MM_yyyy.format(date)+"-"+hh_mm.format(date2)+" "+dd_MM_yyyy.format(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        holder.nombres.setText("NOMBRE: "+visita.getVisitante().getVteNombre()+" "+visita.getVisitante().getVteApellidos());
        holder.empresaNombre.setText("EMPRESA: "+visita.getVisitante().getEmpresa().getEmpNombre());
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
        private TextView fIngreso;
        private TextView fSalida;
        private TextView nombres;
        private TextView empresaNombre;
        Visita visita;

        public ARV(@NonNull View itemView) {
            super(itemView);
            fIngreso = itemView.findViewById(R.id.fIngreso);
            fSalida = itemView.findViewById(R.id.fSalida);
            nombres = itemView.findViewById(R.id.nombres);
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
