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
import com.stbnlycan.models.Usuario;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.ARV> implements Filterable {
    private List<Usuario> eventosList;
    private List<Usuario> eventosListFull;

    private OnUsuarioClickListener mListener;
    private OnVQRClickListener vqrListener;
    private OnEEClickListener eeListener;
    private Context context;
    //private OkHttpClient client;
    private String authorization;

    public UsuariosAdapter(Context context, String authorization, List<Usuario> eventosList) {
        this.eventosList = eventosList;
        this.eventosListFull = new ArrayList<>(eventosList);
        this.context = context;
        //this.client = client;
        this.authorization = authorization;
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.usuarios_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ARV holder, int position) {
        Usuario visitante = eventosList.get(position);
        holder.artNaam.setText(visitante.getUsername());
        holder.lugar.setText(visitante.getFullname());
        holder.tipoVisitante.setText(visitante.getRol().getNombre());
        holder.empresaNombre.setText(visitante.getAddress());

        /*Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .build();
        picasso.load("http://190.129.90.115:8083/ingresoVisitantes/usuarios/mostrarFoto?foto=" + visitante.getPic()).fit().into(holder.imgVisitante);*/
        String url = "http://190.129.90.115:8083/ingresoVisitantes/usuarios/mostrarFoto?foto=" + visitante.getPic();
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
                        holder.imgVisitante.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.imgVisitante.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imgVisitante);

        holder.visitante = eventosList.get(position);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Usuario> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0)
            {
                filteredList.addAll(eventosListFull);
            }
            else
            {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Usuario item : eventosListFull)
                {
                    //Log.d("msg2", ""+item.getVteNombre());
                    if(item.getFullname().toLowerCase().contains(filterPattern))
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

        private ImageView imgVisitante;
        private TextView artNaam;
        private TextView lugar;
        private TextView tipoVisitante;
        private TextView empresaNombre;
        private Usuario visitante;
        private ProgressBar progressBar;

        public ARV(@NonNull View itemView) {
            super(itemView);

            imgVisitante = itemView.findViewById(R.id.imgVisitante);
            artNaam = itemView.findViewById(R.id.nombres);
            lugar = itemView.findViewById(R.id.nroCi);
            tipoVisitante = itemView.findViewById(R.id.tipoVisitante);
            empresaNombre = itemView.findViewById(R.id.empresaNombre);
            progressBar = itemView.findViewById(R.id.progressBar);

            imgVisitante.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            Log.d("Click","clickeado");

            mListener.onEventoClick(visitante, getAdapterPosition());

        }

    }

    public void setOnUsuarioClickListener(OnUsuarioClickListener listener){
        mListener = listener;
    }

    public void setOnVQRClickListener(OnVQRClickListener listener){
        vqrListener = listener;
    }

    public void setOnEEClickListener(OnEEClickListener listener){
        eeListener = listener;
    }

    public interface OnUsuarioClickListener
    {
        void onEventoClick(Usuario visitante, int position);
    }

    public interface OnVQRClickListener
    {
        void OnVQRClick(Usuario visitante);
    }

    public interface OnEEClickListener
    {
        void OnEEClick(Usuario visitante);
    }
}
