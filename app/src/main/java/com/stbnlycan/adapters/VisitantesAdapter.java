package com.stbnlycan.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.Visitante;

import java.util.ArrayList;
import java.util.List;

public class VisitantesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private List<Visitante> eventosList;
    private List<Visitante> eventosListFull;

    private OnVisitanteClickListener mListener;
    private OnVQRClickListener vqrListener;
    private OnEEClickListener eeListener;
    private Context context;
    private String authorization;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public VisitantesAdapter(Context context, String authorization, List<Visitante> eventosList) {
        this.eventosList = eventosList;
        this.eventosListFull = new ArrayList<>(eventosList);
        this.context = context;
        //this.client = client;
        this.authorization = authorization;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.visitantes_list, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_recycler, parent, false);
            return new LoadingViewHolder(view);
        }
        //return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.visitantes_list, parent, false));
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
                    //Log.d("msg2", ""+item.getVteNombre());
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

    @Override
    public int getItemViewType(int position) {
        return eventosList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private ImageView imgVisitante;
        private TextView artNaam;
        private TextView lugar;
        private TextView tipoVisitante;
        private TextView empresaNombre;
        private Button btnVQR;
        private Button btnEE;
        private Visitante visitante;
        private ProgressBar progressBar;

        public ARV(@NonNull View itemView) {
            super(itemView);

            imgVisitante = itemView.findViewById(R.id.imgVisitante);
            artNaam = itemView.findViewById(R.id.nombres);
            lugar = itemView.findViewById(R.id.nroCi);
            tipoVisitante = itemView.findViewById(R.id.tipoVisitante);
            empresaNombre = itemView.findViewById(R.id.empresaNombre);
            btnVQR = itemView.findViewById(R.id.btnVQR);
            btnEE = itemView.findViewById(R.id.btnEE);
            progressBar = itemView.findViewById(R.id.progressBar);

            imgVisitante.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(this);

            btnVQR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vqrListener.OnVQRClick(visitante);
                }
            });

            btnEE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eeListener.OnEEClick(visitante);
                }
            });
        }

        @Override
        public void onClick(View view)
        {
            Log.d("Click","clickeado");

            mListener.onEventoClick(visitante, getAdapterPosition());

        }

    }

    public void setOnVisitanteClickListener(OnVisitanteClickListener listener){
        mListener = listener;
    }

    public void setOnVQRClickListener(OnVQRClickListener listener){
        vqrListener = listener;
    }

    public void setOnEEClickListener(OnEEClickListener listener){
        eeListener = listener;
    }

    public interface OnVisitanteClickListener
    {
        void onEventoClick(Visitante visitante, int position);
    }

    public interface OnVQRClickListener
    {
        void OnVQRClick(Visitante visitante);
    }

    public interface OnEEClickListener
    {
        void OnEEClick(Visitante visitante);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imgVisitante;
        private TextView artNaam;
        private TextView lugar;
        private TextView tipoVisitante;
        private TextView empresaNombre;
        private Button btnVQR;
        private Button btnEE;
        private Visitante visitante;
        private ProgressBar progressBar;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVisitante = itemView.findViewById(R.id.imgVisitante);
            artNaam = itemView.findViewById(R.id.nombres);
            lugar = itemView.findViewById(R.id.nroCi);
            tipoVisitante = itemView.findViewById(R.id.tipoVisitante);
            empresaNombre = itemView.findViewById(R.id.empresaNombre);
            btnVQR = itemView.findViewById(R.id.btnVQR);
            btnEE = itemView.findViewById(R.id.btnEE);
            progressBar = itemView.findViewById(R.id.progressBar);

            imgVisitante.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(this);

            btnVQR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vqrListener.OnVQRClick(visitante);
                }
            });

            btnEE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eeListener.OnEEClick(visitante);
                }
            });
        }

        @Override
        public void onClick(View view)
        {
            Log.d("Click","clickeado");

            mListener.onEventoClick(visitante, getAdapterPosition());

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

        Visitante visitante = eventosList.get(position);
        viewHolder.artNaam.setText(visitante.getVteNombre() + " " + visitante.getVteApellidos());
        viewHolder.lugar.setText(visitante.getVteCi());
        viewHolder.tipoVisitante.setText(visitante.getTipoVisitante().getTviNombre());
        viewHolder.empresaNombre.setText(visitante.getEmpresa().getEmpNombre());

        String url = context.getResources().getString(R.string.url_foto_visitantes) + visitante.getVteImagen();
        GlideUrl glideUrl = new GlideUrl(url,
                new LazyHeaders.Builder()
                        .addHeader("Authorization", authorization)
                        .build());
        Glide.with(context)
                .load(glideUrl)
                .centerCrop()
                .apply(new RequestOptions().override(96, 96))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewHolder.imgVisitante.setVisibility(View.VISIBLE);
                        viewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        viewHolder.imgVisitante.setVisibility(View.VISIBLE);
                        viewHolder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(viewHolder.imgVisitante);

        viewHolder.visitante = eventosList.get(position);

    }

}
