package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visita;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ListaVCSalidaAPIs {
    @GET("visita/visitantesConSalida")
    Call<List<Visita>> listaVCSalida(@Query("fechaInicio") String fechaInicio, @Query("fechaFin") String fechaFin, @Query("recinto") String recinto, @Query("areaRecinto") String areaRecinto);
}
