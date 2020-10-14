package com.stbnlycan.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
    private OnDOIQRClickListener doiQRListener;
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
        return new DOI2Adapter.ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.doi2_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final DOI2Adapter.ARV holder, int position) {
        DocumentoIngreso doi = doiList.get(position);

        Log.d("msg901",""+doi.getDoiImagen());

        if(doi.getDoiImagen() != null && doi.getDoiDocumento() != null)
        {
            holder.seccionQR.setVisibility(View.VISIBLE);
            holder.seccionFoto.setVisibility(View.VISIBLE);
            holder.divider.setVisibility(View.VISIBLE);

            Picasso picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(client))
                    .build();
            picasso.load("http://190.129.90.115:8083/ingresoVisitantes/documentoIngreso/mostrarFoto?foto=" + doi.getDoiImagen()).resize(300, 300).into(holder.doiImagen, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    Log.d("msg512","cargado");
                    holder.doiImagen.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onError(Exception e) {
                    Log.d("msg516","error "+e);
                }
            });

            holder.textoQR.setText(doi.getDoiDocumento());
            try {
                com.google.zxing.Writer writer = new QRCodeWriter();
                int width = 300;
                int height = 300;
                BitMatrix bm = writer
                        .encode(doi.getDoiDocumento(), BarcodeFormat.QR_CODE, width, height);
                Bitmap ImageBitmap = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_8888);

                for (int i = 0; i < width; i++) {// width
                    for (int j = 0; j < height; j++) {// height
                        ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK
                                : Color.WHITE);
                    }
                }
                holder.doiImagenIV2.setImageBitmap(ImageBitmap);
            }
            catch (Exception ex)
            {

            }
        }
        else if(doi.getDoiImagen() != null && doi.getDoiDocumento() == null)
        {
            holder.seccionQR.setVisibility(View.GONE);
            holder.seccionFoto.setVisibility(View.VISIBLE);
            holder.divider.setVisibility(View.GONE);

            Picasso picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(client))
                    .build();
            picasso.load("http://190.129.90.115:8083/ingresoVisitantes/documentoIngreso/mostrarFoto?foto=" + doi.getDoiImagen()).resize(300, 300).into(holder.doiImagen, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    holder.doiImagen.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
        else if(doi.getDoiImagen() == null && doi.getDoiDocumento() != null)
        {
            holder.seccionQR.setVisibility(View.VISIBLE);
            holder.seccionFoto.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);

            holder.textoQR.setText(doi.getDoiDocumento());
            try {
                com.google.zxing.Writer writer = new QRCodeWriter();
                int width = 300;
                int height = 300;
                BitMatrix bm = writer
                        .encode(doi.getDoiDocumento(), BarcodeFormat.QR_CODE, width, height);
                Bitmap ImageBitmap = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_8888);

                for (int i = 0; i < width; i++) {// width
                    for (int j = 0; j < height; j++) {// height
                        ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK
                                : Color.WHITE);
                    }
                }
                holder.doiImagenIV2.setImageBitmap(ImageBitmap);
            }
            catch (Exception ex)
            {

            }
        }


        /*if(doi.getDoiImagen() != null)
        {
            Picasso picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(client))
                    .build();
            picasso.load("http://190.129.90.115:8083/ingresoVisitantes/documentoIngreso/mostrarFoto?foto=" + doi.getDoiImagen()).resize(300, 300).into(holder.doiImagen, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    holder.doiImagen.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
        else
        {
            Picasso.get().load(R.drawable.ic_image_black_48dp).resize(120, 120).into(holder.doiImagen);
            holder.doiImagen.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }

        if(doi.getDoiDocumento() != null)
        {
            holder.textoQR.setText(doi.getDoiDocumento());
            try {
                com.google.zxing.Writer writer = new QRCodeWriter();
                int width = 300;
                int height = 300;
                BitMatrix bm = writer
                        .encode(doi.getDoiDocumento(), BarcodeFormat.QR_CODE, width, height);
                Bitmap ImageBitmap = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_8888);

                for (int i = 0; i < width; i++) {// width
                    for (int j = 0; j < height; j++) {// height
                        ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK
                                : Color.WHITE);
                    }
                }
                holder.doiImagenIV2.setImageBitmap(ImageBitmap);
            }
            catch (Exception ex)
            {

            }
        }
        else
        {
            Picasso.get().load(R.drawable.ic_image_black_48dp).resize(120, 120).into(holder.doiImagenIV2);
            holder.textoQR.setText("");
        }*/

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
        private ImageView doiImagenIV2;
        private TextView tdoNombre;
        private DocumentoIngreso doi;
        private ProgressBar progressBar;
        private TextView textoQR;
        private LinearLayout seccionFoto;
        private LinearLayout seccionQR;
        private View divider;

        public ARV(@NonNull View itemView) {
            super(itemView);
            doiImagen = itemView.findViewById(R.id.doiImagen);
            doiImagenIV2 = itemView.findViewById(R.id.doiImagenIV2);
            tdoNombre = itemView.findViewById(R.id.tdoNombre);
            progressBar = itemView.findViewById(R.id.progressBar);
            textoQR = itemView.findViewById(R.id.textoQR);

            seccionFoto = itemView.findViewById(R.id.seccionFoto);
            seccionQR = itemView.findViewById(R.id.seccionQR);
            divider = itemView.findViewById(R.id.divider);

            doiImagen.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(this);

            doiImagen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doiListener.OnDOIClick(doi);
                }
            });

            doiImagenIV2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doiQRListener.OnDOIQRClick(doi);
                }
            });
        }

        @Override
        public void onClick(View view)
        {
            Log.d("Click","clickeado");
            //doiListener.OnDOIClick(doi);
        }
    }

    public interface OnDOIClickListener
    {
        void OnDOIClick(DocumentoIngreso doi);
    }

    public void setOnDOIClickListener(OnDOIClickListener listener){
        doiListener = listener;
    }

    public interface OnDOIQRClickListener
    {
        void OnDOIQRClick(DocumentoIngreso doi);
    }

    public void setOnDOIQRClickListener(OnDOIQRClickListener listener){
        doiQRListener = listener;
    }

}
