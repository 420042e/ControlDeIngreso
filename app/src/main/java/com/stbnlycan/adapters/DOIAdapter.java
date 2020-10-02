package com.stbnlycan.adapters;

import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.DocumentoIngreso;
import com.stbnlycan.models.Visitante;

import java.io.File;
import java.util.List;

public class DOIAdapter extends RecyclerView.Adapter<DOIAdapter.ARV>{

    private List<DocumentoIngreso> doiList;
    private OnDOIClickListener doiListener;

    public DOIAdapter(List<DocumentoIngreso> doiList) {
        this.doiList = doiList;
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DOIAdapter.ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.doi_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DOIAdapter.ARV holder, int position) {
        DocumentoIngreso doi = doiList.get(position);

        File f = new File(doi.getDoiImagen());
        Picasso.get().load(f).resize(300, 300).into(holder.doiImagen);
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
