package com.stbnlycan.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.DocumentoIngreso;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DOI2Adapter extends RecyclerView.Adapter<DOI2Adapter.ARV>{

    private List<DocumentoIngreso> doiList;
    private OnDOIClickListener doiListener;
    private Context context;
    private OkHttpClient client;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public DOI2Adapter(List<DocumentoIngreso> doiList) {
        this.doiList = doiList;
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DOI2Adapter.ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.doi_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DOI2Adapter.ARV holder, int position) {
        DocumentoIngreso doi = doiList.get(position);

        /*File f = new File(doi.getDoiImagen());
        Picasso.get().load(f).resize(300, 300).into(holder.doiImagen);*/
        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .build();
        picasso.load("http://190.129.90.115:8083/ingresoVisitantes/documentoIngreso/mostrarFoto?foto=" + doi.getDoiImagen()).fit().into(holder.doiImagen);

        //holder.visitante = eventosList.get(position);

        holder.tdoNombre.setText(doi.getTipoDocumento().getTdoNombre());
        holder.doi =  doiList.get(position);
    }

    @Override
    public int getItemCount() {
        return doiList!=null?doiList.size():0;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private ImageView doiImagen;
        private TextView tdoNombre;
        private DocumentoIngreso doi;

        public ARV(@NonNull View itemView) {
            super(itemView);
            doiImagen = itemView.findViewById(R.id.doiImagen);
            tdoNombre = itemView.findViewById(R.id.tdoNombre);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            Log.d("Click","clickeado");
            doiListener.OnDOIClick(doi);
        }
    }

    public interface OnDOIClickListener
    {
        void OnDOIClick(DocumentoIngreso doi);
    }

    public void setOnDOIClickListener(OnDOIClickListener listener){
        doiListener = listener;
    }

}
