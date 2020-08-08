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
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Empresa;

import java.util.ArrayList;
import java.util.List;

public class EmpresasAdapter extends RecyclerView.Adapter<EmpresasAdapter.ARV> {
    private List<Empresa> eventosList;
    private List<Empresa> eventosListFull;

    private OnVisitanteClickListener mListener;

    public EmpresasAdapter(List<Empresa> eventosList) {
        this.eventosList = eventosList;
        this.eventosListFull = new ArrayList<>(eventosList);
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.empresas_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ARV holder, int position) {
        Empresa empresa = eventosList.get(position);
        holder.artNaam.setText(empresa.getEmpNombre());
        holder.lugar.setText(empresa.getEmpObs());

        //holder.artimg.setImageResource(R.drawable.evento_agetic2);
        holder.empresa = eventosList.get(position);
    }

    @Override
    public int getItemCount() {
        return eventosList!=null?eventosList.size():0;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private ImageView imgVisitante;
        private TextView artNaam;
        private TextView lugar;
        Empresa empresa;

        public ARV(@NonNull View itemView) {
            super(itemView);

            imgVisitante = itemView.findViewById(R.id.imgVisitante);
            artNaam = itemView.findViewById(R.id.nombres);
            lugar = itemView.findViewById(R.id.nroCi);
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
            mListener.onEventoClick(empresa, getAdapterPosition());

        }

    }

    public void setOnVisitanteClickListener(OnVisitanteClickListener listener){
        mListener = listener;
    }

    public interface OnVisitanteClickListener
    {
        void onEventoClick(Empresa empresa, int position);
    }
}
