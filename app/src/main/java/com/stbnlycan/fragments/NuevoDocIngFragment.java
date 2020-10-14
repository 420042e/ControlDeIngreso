package com.stbnlycan.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Select;
import com.squareup.picasso.Picasso;
import com.stbnlycan.controldeingreso.DocumentosIngreso;
import com.stbnlycan.controldeingreso.NetworkClient;
import com.stbnlycan.controldeingreso.R;
import com.stbnlycan.interfaces.TipoDocAPIs;
import com.stbnlycan.models.DocumentoIngreso;
import com.stbnlycan.models.TipoDocumento;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NuevoDocIngFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NuevoDocIngFragment extends DialogFragment implements Validator.ValidationListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<TipoDocumento> tipoDocumento;
    private ArrayAdapter<TipoDocumento> adapterTipoDoc;

    @Select
    private Spinner tipoDocS;

    private String authorization;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Button btnAdd;
    private Button btnAddQR;
    private Button btnCancel;
    private Button btnAceptar;

    private Uri uri;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private String imagenObtenida;
    private String qrObtenido;

    private Validator validator;

    /*private ImageView doiImagenIV;
    private ImageView doiImagenIV2;*/

    public OnInputListener onInputListener;

    public OnInputListener getOnInputListener() {
        return onInputListener;
    }

    public void setOnInputListener(OnInputListener onInputListener) {
        this.onInputListener = onInputListener;
    }

    public NuevoDocIngFragment() {
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
    public static NuevoDocIngFragment newInstance(String param1, String param2) {
        NuevoDocIngFragment fragment = new NuevoDocIngFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_dialog_nuevo_doi, container, false);
        return rootView;
        /*View rootView = inflater.inflate(R.layout.fragment_loading, container, false);
        getDialog().setTitle("Procesando...");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setCancelable(false);
        // Inflate the layout for this fragment
        return rootView;*/
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("Documento de Ingreso");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_dialog_nuevo_doi, null);
        builder.setView(rootView);

        //setStyle(DialogFragment.STYLE_NO_TITLE, R.style.cust_dialog);


        validator = new Validator(this);
        validator.setValidationListener(this);

        tipoDocS = rootView.findViewById(R.id.tipoDoc);
        btnAdd = rootView.findViewById(R.id.btnAdd);
        btnAddQR = rootView.findViewById(R.id.btnAddQR);
        btnCancel = rootView.findViewById(R.id.btnCancel);
        btnAceptar = rootView.findViewById(R.id.btnAceptar);
        /*doiImagenIV = rootView.findViewById(R.id.doiImagenIV);
        doiImagenIV2 = rootView.findViewById(R.id.doiImagenIV2);*/

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("msg133","hola");
                obtenerFoto();
            }
        });

        btnAddQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("msg134","hola2");
                escaner();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });

        /*builder.setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("msg7734","holaaaa");
                        //validator.validate();
                    }
                })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });*/


        pref = getActivity().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        authorization = pref.getString("token_type", null) + " " + pref.getString("access_token", null);


        iniciarSpinnerTipoDoc();
        fetchTipoDoc();

        setCancelable(false);
        // Inflate the layout for this fragment
        return builder.create();
    }

    public void obtenerFoto()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    uri = FileProvider.getUriForFile(getActivity(), "com.stbnlycan.controldeingreso.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                    Log.d("msg4214", "" + photoFile);
                    imagenObtenida = photoFile.toString();
                } else {
                    uri = FileProvider.getUriForFile(getActivity(), "com.stbnlycan.controldeingreso.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                    Log.d("msg4215", "" + photoFile);
                    imagenObtenida = photoFile.toString();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    public void redimensionarImg() {
        try {
            // we'll start with the original picture already open to a file
            File imgFileOrig = new File(imagenObtenida); //change "getPic()" for whatever you need to open the image file.
            Bitmap b = BitmapFactory.decodeFile(imgFileOrig.getAbsolutePath());
            // original measurements
            int origWidth = b.getWidth();
            int origHeight = b.getHeight();

            //Toast.makeText(getApplicationContext(), "origWidth "+origWidth+" origHeight "+origHeight, Toast.LENGTH_LONG).show();

            final int destWidth = 768;//or the width you need

            if (origWidth > destWidth) {
                // picture is wider than we want it, we calculate its target height
                int destHeight = origHeight / (origWidth / destWidth);
                // we create an scaled bitmap so it reduces the image, not just trim it
                Bitmap b2 = Bitmap.createScaledBitmap(b, destWidth, destHeight, false);

                if (origWidth > origHeight) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    b2 = Bitmap.createBitmap(b2, 0, 0, b2.getWidth(), b2.getHeight(), matrix, true);
                }

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // compress to the format you want, JPEG, PNG...
                // 70 is the 0-100 quality percentage
                b2.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                // we save the file, at least until we have made use of it
                //File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "test.jpg");
                File f = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + imgFileOrig.getName());
                f.createNewFile();
                //write the bytes in file
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(outStream.toByteArray());
                // remember close de FileOutput
                fo.close();
            }
        } catch (Exception ex) {

        }
    }

    public void escaner() {
        IntentIntegrator intent = IntentIntegrator.forSupportFragment(NuevoDocIngFragment.this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intent.setPrompt("Registrando QR documento de ingreso");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);
        intent.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("msg6543 ", "hola "+requestCode+" "+resultCode+" "+getActivity().RESULT_OK);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelaste el escaneo de ingreso", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                qrObtenido = result.getContents();
                //doiDocumentoET.setText(qrObtenido);

                int imgResource = R.drawable.ic_check_black_24dp;
                btnAddQR.setCompoundDrawablesWithIntrinsicBounds(0, 0, imgResource, 0);
                btnAddQR.setCompoundDrawablePadding(8);




                try {
                    com.google.zxing.Writer writer = new QRCodeWriter();
                    // String finaldata = Uri.encode(data, "utf-8");
                    int width = 250;
                    int height = 250;
                    BitMatrix bm = writer
                            .encode(qrObtenido, BarcodeFormat.QR_CODE, width, height);
                    Bitmap ImageBitmap = Bitmap.createBitmap(width, height,
                            Bitmap.Config.ARGB_8888);

                    for (int i = 0; i < width; i++) {// width
                        for (int j = 0; j < height; j++) {// height
                            ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK
                                    : Color.WHITE);
                        }
                    }
                    //doiImagenIV2.setImageBitmap(ImageBitmap);
                }
                catch (Exception ex)
                {

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Log.d("msg6545 ", "" + imagenObtenida);
            File f = new File(imagenObtenida);
            //Picasso.get().load(f).resize(300, 300).into(doiImagenIV);

            /*Bitmap bmImg = BitmapFactory.decodeFile(f.getAbsolutePath());
            doiImagenIV.setImageBitmap(bmImg);*/

            redimensionarImg();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            int imgResource = R.drawable.ic_check_black_24dp;
            btnAdd.setCompoundDrawablesWithIntrinsicBounds(0, 0, imgResource, 0);
            btnAdd.setCompoundDrawablePadding(8);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //doiImagenIV.setImageBitmap(imageBitmap);
                    /*doiImagenIV.getLayoutParams().width = width;
                    doiImagenIV.getLayoutParams().height = width;
                    doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);*/
                } else {
                    //doiImagenIV.setImageURI(uri);
                    /*doiImagenIV.getLayoutParams().width = width;
                    doiImagenIV.getLayoutParams().height = width;
                    doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);*/
                }
            } else {
                Log.d("msg554", "hola 2");
                //doiImagenIV.setImageURI(uri);
                /*doiImagenIV.getLayoutParams().width = width;
                doiImagenIV.getLayoutParams().height = width;
                doiImagenIV.setScaleType(ImageView.ScaleType.CENTER_CROP);*/
            }
        }
    }

    public void iniciarSpinnerTipoDoc() {
        tipoDocumento = new ArrayList<>();

        TipoDocumento tipoDocumentod = new TipoDocumento();
        tipoDocumentod.setTdoCod(0);
        tipoDocumentod.setTdoNombre("SELECCIONE TIPO DE DOCUMENTO");
        tipoDocumentod.setTdoDescripcion("descripcion");

        tipoDocumento.add(tipoDocumentod);
        adapterTipoDoc = new ArrayAdapter<TipoDocumento>(getActivity(), R.layout.style_spinner, tipoDocumento) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textview = (TextView) view;
                if (position == 0) {
                    textview.setTextColor(Color.GRAY);
                } else {
                    textview.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapterTipoDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoDocS.setAdapter(adapterTipoDoc);
        tipoDocS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*AreaRecinto areaRecinto = (AreaRecinto) parent.getSelectedItem();
                displayAreaRData(areaRecinto);*/
                TipoDocumento tipoDocumento = (TipoDocumento) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchTipoDoc() {
        Retrofit retrofit = NetworkClient.getRetrofitClient(getActivity());
        TipoDocAPIs tipoDocAPIs = retrofit.create(TipoDocAPIs.class);
        Call<List<TipoDocumento>> call = tipoDocAPIs.listaTipoDoc(authorization);
        call.enqueue(new Callback<List<TipoDocumento>>() {
            @Override
            public void onResponse(Call<List<TipoDocumento>> call, retrofit2.Response<List<TipoDocumento>> response) {
                for (int i = 0; i < response.body().size(); i++) {
                    tipoDocumento.add(response.body().get(i));
                }
            }

            @Override
            public void onFailure(Call<List<TipoDocumento>> call, Throwable t) {

            }
        });
    }

    public interface OnInputListener {
        void sendInput(DocumentoIngreso doi);
    }

    @Override
    public void onValidationSucceeded() {

        /*File f = new File(imagenObtenida);
        DocumentoIngreso doi = new DocumentoIngreso();
        doi.setDoiImagen(imagenObtenida);
        doi.setDoiDocumento(qrObtenido);
        TipoDocumento tipoDocumento = (TipoDocumento) tipoDocS.getSelectedItem();
        doi.setTipoDocumento(tipoDocumento);


        //Reiniciar componentes
        tipoDocS.setSelection(0, true);*/


        DocumentoIngreso doi = new DocumentoIngreso();
        doi.setDoiImagen(imagenObtenida);
        doi.setDoiDocumento(qrObtenido);
        TipoDocumento tipoDocumento = (TipoDocumento) tipoDocS.getSelectedItem();
        doi.setTipoDocumento(tipoDocumento);
        onInputListener.sendInput(doi);
        dismiss();

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getActivity());
            if (view instanceof Spinner) {
                ((TextView) ((Spinner) view).getSelectedView()).setError("Este campo es requerido");
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
