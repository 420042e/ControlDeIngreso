package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visitante;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BuscarXQRAPIs {
    @GET("visitante/buscarXQR")
    Call<Visitante> buscarXQR(@Query("llave") String llave);
}
