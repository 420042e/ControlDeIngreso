package com.stbnlycan.interfaces;

import com.stbnlycan.models.ListaUsuarios;
import com.stbnlycan.models.ListaVisitantes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ListaUsuariosXUsrNameAPIs {
    @GET("usuarios/buscarPorUsername")
    Call<ListaUsuarios> listaVisitantesXUsrName(@Query("username") String username, @Query("page") String page, @Query("size") String size, @Header("Authorization") String authHeader);
}
