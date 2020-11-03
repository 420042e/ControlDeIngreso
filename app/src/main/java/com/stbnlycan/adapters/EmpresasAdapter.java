package com.stbnlycan.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Empresa;

import java.util.ArrayList;
import java.util.List;

public class EmpresasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Empresa> eventosList;
    private List<Empresa> eventosListFull;

    private OnVisitanteClickListener mListener;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public EmpresasAdapter(List<Empresa> eventosList) {
        this.eventosList = eventosList;
        this.eventosListFull = new ArrayList<>(eventosList);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empresas_list, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_recycler, parent, false);
            return new LoadingViewHolder(view);
        }
        //return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.empresas_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemViewHolder) {
            populateItemRows((ItemViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }

    }

    @Override
    public int getItemCount() {
        return eventosList!=null?eventosList.size():0;
    }

    @Override
    public int getItemViewType(int position) {
        return eventosList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
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

    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imgVisitante;
        private TextView artNaam;
        private TextView lugar;
        Empresa empresa;

        public ItemViewHolder(@NonNull View itemView) {
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
            mListener.onEventoClick(empresa, getAdapterPosition());

        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }

    private void populateItemRows(final ItemViewHolder viewHolder, int position) {
        Empresa empresa = eventosList.get(position);
        viewHolder.artNaam.setText(empresa.getEmpNombre());
        viewHolder.lugar.setText(empresa.getEmpObs());
        viewHolder.empresa = eventosList.get(position);
    }
}
