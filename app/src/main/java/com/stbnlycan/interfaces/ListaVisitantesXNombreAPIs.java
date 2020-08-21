package com.stbnlycan.interfaces;

import com.stbnlycan.models.ListaVisitantes;
import com.stbnlycan.models.ListaVisitas;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ListaVisitantesXNombreAPIs {
    @GET("visitante/buscarPorNombre")
    Call<ListaVisitantes> listaVisitanteXNombre(@Query("nombre") String nombre, @Query("page") String page, @Query("size") String size, @Header("Authorization") String authHeader);
}
