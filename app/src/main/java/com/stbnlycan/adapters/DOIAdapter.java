package com.stbnlycan.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.models.DocumentoIngreso;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class DOIAdapter extends RecyclerView.Adapter<DOIAdapter.ARV>{

    private List<DocumentoIngreso> doiList;
    private OnDOIClickListener doiListener;
    private OnDOIEClickListener doiEListener;

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

        if(doi.getDoiImagen() != null && doi.getDoiDocumento() != null)
        {
            holder.seccionQR.setVisibility(View.VISIBLE);
            holder.seccionFoto.setVisibility(View.VISIBLE);
            holder.divider.setVisibility(View.VISIBLE);

            File f = new File(doi.getDoiImagen());
            Picasso.get().load(f).resize(300, 300).into(holder.doiImagen);

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

            File f = new File(doi.getDoiImagen());
            Picasso.get().load(f).resize(300, 300).into(holder.doiImagen);
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
        private TextView textoQR;
        private Button btnMore;
        private LinearLayout seccionFoto;
        private LinearLayout seccionQR;
        private View divider;

        public ARV(@NonNull View itemView) {
            super(itemView);
            doiImagen = itemView.findViewById(R.id.doiImagen);
            doiImagenIV2 = itemView.findViewById(R.id.doiImagenIV2);
            tdoNombre = itemView.findViewById(R.id.tdoNombre);
            btnMore = itemView.findViewById(R.id.btnMore);
            textoQR = itemView.findViewById(R.id.textoQR);
            seccionFoto = itemView.findViewById(R.id.seccionFoto);
            seccionQR = itemView.findViewById(R.id.seccionQR);
            divider = itemView.findViewById(R.id.divider);

            itemView.setOnClickListener(this);

            final View itemView2 = itemView;

            btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(btnMore.getContext(), itemView2);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_eliminar:
                                    //moveFile(recordName.getText().toString(), getAdapterPosition());
                                    Log.d("Click","clickeado"+getAdapterPosition());
                                    doiList.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    notifyItemRangeChanged(getAdapterPosition(),doiList.size());
                                    doiEListener.OnDOIClick(doiList.size());
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    // here you can inflate your menu
                    popup.inflate(R.menu.menu_foto);
                    popup.setGravity(Gravity.RIGHT);

                    // if you want icon with menu items then write this try-catch block.
                    try {
                        Field mFieldPopup=popup.getClass().getDeclaredField("mPopup");
                        mFieldPopup.setAccessible(true);
                        MenuPopupHelper mPopup = (MenuPopupHelper) mFieldPopup.get(popup);
                        mPopup.setForceShowIcon(true);
                    } catch (Exception e) {

                    }
                    popup.show();
                }
            });


        }

        @Override
        public void onClick(View view)
        {
            /*Log.d("Click","clickeado"+getAdapterPosition());
            doiList.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
            notifyItemRangeChanged(getAdapterPosition(),doiList.size());*/
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

    public interface OnDOIEClickListener
    {
        void OnDOIClick(int total);
    }

    public void setOnDOIEClickListener(OnDOIEClickListener listener){
        doiEListener = listener;
    }

}
