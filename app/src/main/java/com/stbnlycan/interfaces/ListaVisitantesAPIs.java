package com.stbnlycan.interfaces;

import com.stbnlycan.models.ListaVisitantes;
import com.stbnlycan.models.ListaVisitas;
import com.stbnlycan.models.Visitante;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ListaVisitantesAPIs {
    @GET("visitante/lista")
    Call<ListaVisitantes> listaVisitantes(@Query("page") String page, @Query("size") String size, @Header("Authorization") String authHeader);
}
