package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visitante;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BuscarXCIAPIs {
    @GET("visitante/buscarXCi")
    Call<Visitante> buscarXQR(@Query("ci") String ci);
}
