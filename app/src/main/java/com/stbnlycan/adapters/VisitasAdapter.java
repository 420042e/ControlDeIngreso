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

import java.util.ArrayList;
import java.util.List;

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
        Visita Visita = eventosList.get(position);
        holder.artNaam.setText(Visita.getVisitante().getVteNombre()+" "+Visita.getVisitante().getVteApellidos());
        holder.lugar.setText(Visita.getVisitante().getVteCi());
        holder.empresaNombre.setText(Visita.getVisitante().getEmpresa().getEmpNombre());

        holder.Visita = eventosList.get(position);
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
                Log.d("msg1", "hola "+eventosListFull.size());
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
        Visita Visita;

        public ARV(@NonNull View itemView) {
            super(itemView);

            artNaam = itemView.findViewById(R.id.nombres);
            lugar = itemView.findViewById(R.id.nroCi);
            empresaNombre = itemView.findViewById(R.id.empresaNombre);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            Log.d("Click","clickeado");

            /*FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.setCustomAnimations(R.anim.layout_fad_in, R.anim.layout_fad_out, R.anim.layout_fad_in, R.anim.layout_fad_out);

            Fragment fragment = EventoDetailsFragment.newInstance(1, "Detalles", new Survey("","",""));

            transaction.addToBackStack(null);
            transaction.add(R.id.fragment_details, fragment, "BLANK_FRAGMENT").commit();

            onNoteListener.onNoteClick(getAdapterPosition());*/
            mListener.onEventoClick(Visita, getAdapterPosition());

        }

    }

    public void setOnVisitanteClickListener(OnVisitanteClickListener listener){
        mListener = listener;
    }

    public interface OnVisitanteClickListener
    {
        void onEventoClick(Visita Visita, int position);
    }
}
