package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visitante;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SubirImagenAPIs {
    @Multipart
    @POST("visitante/subirImagen")
    //Call<ResponseBody> subirImagen(@Part MultipartBody.Part file, @Part("vte") RequestBody requestBody);
    Call<Visitante> subirImagen(@Part MultipartBody.Part file, @Part("vte") RequestBody requestBody);
}