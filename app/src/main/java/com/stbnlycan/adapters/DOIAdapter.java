package com.stbnlycan.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.DocumentoIngreso;
import com.stbnlycan.models.Visitante;

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
        holder.artNaam.setText(doi.getTipoDocumento().getTdoNombre());
    }

    @Override
    public int getItemCount() {
        return doiList!=null?doiList.size():0;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private TextView artNaam;

        public ARV(@NonNull View itemView) {
            super(itemView);
            artNaam = itemView.findViewById(R.id.artname);
        }

        @Override
        public void onClick(View view)
        {
            Log.d("Click","clickeado");

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
