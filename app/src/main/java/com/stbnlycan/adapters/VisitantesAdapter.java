package com.stbnlycan.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

public class VisitantesAdapter extends RecyclerView.Adapter<VisitantesAdapter.ARV> implements Filterable {
    private List<Visitante> eventosList;
    private List<Visitante> eventosListFull;

    private OnVisitanteClickListener mListener;

    public VisitantesAdapter(List<Visitante> eventosList) {
        this.eventosList = eventosList;
        eventosListFull = new ArrayList<>(eventosList);
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.visitantes_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ARV holder, int position) {
        Visitante visitante = eventosList.get(position);
        holder.artNaam.setText(visitante.getVteNombre()+" "+visitante.getVteApellidos());
        holder.lugar.setText(visitante.getVteCi());
        if(visitante.getVteEstado().equals("1"))
        {
            holder.estado.setText("Activo");
        }
        else
        {
            holder.estado.setText("Inactivo");
        }

        //holder.artimg.setImageResource(R.drawable.evento_agetic2);
        holder.areaRecinto = eventosList.get(position);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Visitante> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0)
            {
                filteredList.addAll(eventosListFull);
            }
            else
            {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Visitante item : eventosListFull)
                {
                    if(item.getVteNombre().toLowerCase().contains(filterPattern))
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

        //private ImageView artimg;
        private TextView artNaam;
        private TextView lugar;
        private TextView estado;
        Visitante areaRecinto;

        public ARV(@NonNull View itemView) {
            super(itemView);

            //artimg = itemView.findViewById(R.id.artthumb);
            artNaam = itemView.findViewById(R.id.artname);
            lugar = itemView.findViewById(R.id.lugar);
            estado = itemView.findViewById(R.id.fecha);
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
            mListener.onEventoClick(areaRecinto);

        }

    }

    public void setOnVisitanteClickListener(OnVisitanteClickListener listener){
        mListener = listener;
    }

    public interface OnVisitanteClickListener
    {
        void onEventoClick(Visitante areaRecinto);
    }
}
