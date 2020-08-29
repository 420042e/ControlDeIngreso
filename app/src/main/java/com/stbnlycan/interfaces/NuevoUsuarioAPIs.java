package com.stbnlycan.interfaces;

import com.stbnlycan.models.Usuario;
import com.stbnlycan.models.Visitante;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface NuevoUsuarioAPIs {
    @Multipart
    @POST("visitante/registrar")
    Call<Usuario> nuevoUsuario(@Part MultipartBody.Part file, @Part("user") RequestBody requestBody, @Header("Authorization") String authHeader);
}