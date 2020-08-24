package com.stbnlycan.interfaces;

import com.stbnlycan.models.Usuario;
import com.stbnlycan.models.Visitante;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface RecintoXUsuarioAPIs {
    @GET("usuarios")
    Call<Usuario> recintoXUsuario(@Query("username") String username, @Header("Authorization") String authHeader);
}
