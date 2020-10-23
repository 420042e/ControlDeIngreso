package com.stbnlycan.interfaces;

import com.stbnlycan.models.Usuario;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface EditarUsuarioAPIs {
    @PUT("usuarios")
    Call<Usuario> editarUsuario(@Body Usuario body, @Header("Authorization") String authHeader);
}