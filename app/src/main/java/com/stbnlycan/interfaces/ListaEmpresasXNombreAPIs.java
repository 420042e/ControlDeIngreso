package com.stbnlycan.interfaces;

import com.stbnlycan.models.ListaEmpresas;
import com.stbnlycan.models.ListaVisitantes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ListaEmpresasXNombreAPIs {
    @GET("empresa/buscarPorNombre")
    Call<ListaEmpresas> listaEmpresasXNombre(@Query("nombre") String nombre, @Query("page") String page, @Query("size") String size, @Header("Authorization") String authHeader);
}
