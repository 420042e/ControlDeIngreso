package com.stbnlycan.interfaces;

import com.stbnlycan.models.ListaVisitas;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ListaVisitaXCiAPIs {
    @GET("visita/buscarXCi")
    Call<ListaVisitas> listaVisitaXCi(@Query("ci") String ci, @Query("fechaInicio") String fechaInicio, @Query("fechaFin") String fechaFin, @Query("recinto") String recinto, @Query("areaRecinto") String areaRecinto, @Query("salida") String salida, @Query("page") String page, @Query("size") String size, @Header("Authorization") String authHeader);
}
