package com.stbnlycan.fragments;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.stbnlycan.controldeingreso.NetworkClient;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.interfaces.ListaVCSalidaAPIs;
import com.stbnlycan.interfaces.ListaVSSalidaAPIs;
import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.ListaVisitas;
import com.stbnlycan.models.Visita;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PReporteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PReporteFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int tipoVisitaSel;
    private String tipoVisitaS;
    private String fechaIni;
    private String fechaFin;
    private String recintoSel;
    private AreaRecinto areaRecintoSel;
    private String areaRecintoS;
    private int visitasTotales;
    private String authorization;

    private ArrayList<Visita> visitasReporte;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PReporteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoadingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PReporteFragment newInstance(String param1, String param2) {
        PReporteFragment fragment = new PReporteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_busqueda_ci, container, false);
        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("Procesando...");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_loading, null);
        builder.setView(rootView);

        visitasReporte = new ArrayList<>();

        Bundle bundle = getArguments();
        if (bundle != null) {
            tipoVisitaSel = bundle.getInt("tipoVisitaSel");
            tipoVisitaS = bundle.getString("tipoVisitaS");
            fechaIni = bundle.getString("fechaIni");
            fechaFin = bundle.getString("fechaFin");
            recintoSel = bundle.getString("recintoSel");
            areaRecintoSel = (AreaRecinto) bundle.getSerializable("areaRecintoSel");
            areaRecintoS = bundle.getString("areaRecintoS");
            visitasTotales = bundle.getInt("visitasTotales");
            authorization = bundle.getString("authorization");
        }

        if(tipoVisitaSel == 1)
        {
            reporteVCS();
        }
        else if(tipoVisitaSel == 2)
        {
            reporteVSS();
        }


        setCancelable(false);
        // Inflate the layout for this fragment
        return builder.create();
    }

    private void reporteVCS() {
        Log.d("msg9811",""+fechaIni+" "+fechaFin+" "+recintoSel+" "+areaRecintoSel.getAreaCod()+" "+Integer.toString(visitasTotales)+" "+authorization);
        Retrofit retrofit = NetworkClient.getRetrofitClient(getContext());
        ListaVSSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVSSalidaAPIs.class);
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVSSalida(fechaIni, fechaFin, recintoSel, areaRecintoSel.getAreaCod(),"0", Integer.toString(visitasTotales), authorization);
        call.enqueue(new Callback<ListaVisitas>() {
            @Override
            public void onResponse(Call <ListaVisitas> call, retrofit2.Response<ListaVisitas> response) {
                //bar.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.VISIBLE);
                visitasReporte.clear();
                ListaVisitas listaVisitas = response.body();
                if(listaVisitas.getlVisita().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                    //tvTotalVisitantes.setText("Total de visitas: 0");
                }
                else {
                    //tvNoData.setVisibility(View.GONE);
                    //visitasTotales = listaVisitas.getTotalElements();
                    //tvTotalVisitantes.setText("Total de visitas: " + listaVisitas.getTotalElements());
                    for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                    {
                        visitasReporte.add(listaVisitas.getlVisita().get(i));
                    }
                    //visitasAdapter.notifyDataSetChanged();
                    generarReporte();
                }
                //swipeRefreshLayout.setRefreshing(false);
                //nPag = 0;
            }
            @Override
            public void onFailure(Call <ListaVisitas> call, Throwable t) {

            }
        });
    }

    private void reporteVSS() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getContext());
        ListaVCSalidaAPIs listaVSSalidaAPIs = retrofit.create(ListaVCSalidaAPIs.class);
        Call<ListaVisitas> call = listaVSSalidaAPIs.listaVCSalida(fechaIni, fechaFin, recintoSel, areaRecintoSel.getAreaCod(),"0",Integer.toString(visitasTotales), authorization);
        call.enqueue(new Callback<ListaVisitas>() {
            @Override
            public void onResponse(Call <ListaVisitas> call, retrofit2.Response<ListaVisitas> response) {
                //bar.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.VISIBLE);
                visitasReporte.clear();
                ListaVisitas listaVisitas = response.body();
                if(listaVisitas.getlVisita().size() == 0)
                {
                    //tvNoData.setVisibility(View.VISIBLE);
                    //tvTotalVisitantes.setText("Total de visitas: 0");
                }
                else {
                    //tvNoData.setVisibility(View.GONE);
                    //visitasTotales = listaVisitas.getTotalElements();
                    //tvTotalVisitantes.setText("Total de visitas: " + listaVisitas.getTotalElements());
                    for(int i = 0 ; i < listaVisitas.getlVisita().size() ; i++)
                    {
                        visitasReporte.add(listaVisitas.getlVisita().get(i));
                    }
                    //visitasAdapter.notifyDataSetChanged();
                    generarReporte();
                }
                //swipeRefreshLayout.setRefreshing(false);
                //nPag = 0;
            }
            @Override
            public void onFailure(Call <ListaVisitas> call, Throwable t) {

            }
        });
    }

    public void generarReporte()
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String myFilePath = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/reporte_"+tipoVisitaS.toLowerCase().replace(" ","_")+timeStamp+".pdf";

        File file = new File (myFilePath);
        Uri path = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setDataAndType(path, "application/pdf");
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext(), channelId)
                .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                .setContentTitle("Reporte de "+tipoVisitaS.toLowerCase())
                .setContentText("Click para visualizar");
        mBuilder.setTicker("Reporte de "+tipoVisitaS.toLowerCase());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(notificationId, mBuilder.build());

        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(myFilePath));
            doc.open();
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setSpacingBefore(40f);
            headerTable.setSpacingAfter(20f);
            headerTable.setWidthPercentage(100);

            PdfPTable assigneeTable = new PdfPTable(4);
            assigneeTable.setSpacingAfter(20f);
            assigneeTable.setWidthPercentage(100);
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            String [] val1 = {"Reporte de "+tipoVisitaS.toLowerCase()};
            String [] val2 = {"Area Recinto: "+areaRecintoS.toLowerCase()};
            String [] val3 = {"Mostrando resultados desde: "+fechaIni+" - "+fechaFin};
            String [] val4 = {"Total de visitas: "+visitasTotales};
            addTitulo(headerTable, 1, val1, Element.ALIGN_CENTER);
            addTitulo(headerTable, 1, val2, Element.ALIGN_LEFT);
            addTitulo(headerTable, 1, val3, Element.ALIGN_LEFT);
            addTitulo(headerTable, 1, val4, Element.ALIGN_LEFT);
            addAssigneeRow(assigneeTable);

            String [] val5 = {"Visitante","Ingreso", "Salida", "Empresa"};
            addRow(table,4, val5, new BaseColor(79, 129, 189), new BaseColor(255, 255, 255));
            for(int i = 0 ; i < visitasReporte.size() ; i++)
            {
                String [] val = {visitasReporte.get(i).getVisitante().getVteNombre()+" "+visitasReporte.get(i).getVisitante().getVteApellidos(), visitasReporte.get(i).getVisIngreso(), visitasReporte.get(i).getVisSalida(), visitasReporte.get(i).getVisitante().getEmpresa().getEmpNombre()};
                if(i % 2 == 0)
                {
                    addRow(table,4, val, new BaseColor(211, 223, 238), new BaseColor(0, 0, 0));
                }
                else
                {
                    addRow(table,4, val, new BaseColor(255, 255, 255), new BaseColor(0, 0, 0));
                }
            }
            doc.add(headerTable);
            doc.add(assigneeTable);
            doc.add(table);
            //loadingFragment.dismiss();

        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            doc.close();
        }
    }

    public static void addTitulo(PdfPTable table, int columns, String[] value, int hAlign) {
        BaseColor color = new BaseColor(240, 240, 240); // or red, green, blue, alpha
        Font boldFont = new Font(Font.FontFamily.HELVETICA , 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA , 11, Font.NORMAL);
        if(columns>0) {
            for(int i=0;i<value.length;i++)
            {
                PdfPCell cell = new PdfPCell(new Phrase(value[i],normalFont));
                cell.setColspan(1);
                cell.setPadding(5);
                /*cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);*/
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setHorizontalAlignment(hAlign);
                cell.setUseVariableBorders(true);
                cell.setBorderColorTop(BaseColor.WHITE);
                cell.setBorderColorLeft(BaseColor.WHITE);
                cell.setBorderColorBottom(BaseColor.WHITE);
                cell.setBorderColorRight(BaseColor.WHITE);

                table.addCell(cell);
            }
        }
        table.completeRow();
    }

    public static void addRow(PdfPTable table, int columns, String[] value, BaseColor baseColor, BaseColor baseColorT) {
        BaseColor color = new BaseColor(240, 240, 240); // or red, green, blue, alpha
        Font boldFont = new Font(Font.FontFamily.HELVETICA , 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA , 11, Font.NORMAL);
        normalFont.setColor(baseColorT);
        if(columns>0) {
            for(int i=0;i<value.length;i++)
            {
                String dtIngreso = value[1];
                String dtSalida = value[2];
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                SimpleDateFormat dd_MM_yyyy = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat hh_mm = new SimpleDateFormat("HH:mm");
                String hora_fecha="";
                Date date = null;
                Date date2 = null;
                try {
                    date = format.parse(dtIngreso);
                    if(dtSalida == null)
                    {
                        hora_fecha = dd_MM_yyyy.format(date)+" "+hh_mm.format(date);
                        value[1] = dd_MM_yyyy.format(date)+" "+hh_mm.format(date);
                        value[2] = "";
                    }
                    else
                    {
                        date2 = format.parse(dtSalida);
                        hora_fecha = dd_MM_yyyy.format(date)+" "+hh_mm.format(date)+" SAL: "+dd_MM_yyyy.format(date2)+" "+hh_mm.format(date2);
                        value[1] = dd_MM_yyyy.format(date)+" "+hh_mm.format(date);
                        value[2] = dd_MM_yyyy.format(date2)+" "+hh_mm.format(date2);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                PdfPCell cell = new PdfPCell(new Phrase(value[i],normalFont));
                cell.setColspan(1);
                cell.setPadding(5);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);


                cell.setBackgroundColor(baseColor);

                cell.setUseVariableBorders(true);
                cell.setBorderColorTop(baseColor);
                cell.setBorderColorLeft(baseColor);
                cell.setBorderColorBottom(baseColor);
                cell.setBorderColorRight(baseColor);

                table.addCell(cell);
            }
        }
        table.completeRow();
    }

    public static void addAssigneeRow(PdfPTable table) {
        // Creates another row that only have to columns.
        // The cell 5 and cell 6 width will span two columns
        // in width.
        BaseColor color = new BaseColor(240, 240, 240); // or red, green, blue, alpha
        Font boldFont = new Font(Font.FontFamily.HELVETICA , 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA , 11, Font.NORMAL);

    }
}
