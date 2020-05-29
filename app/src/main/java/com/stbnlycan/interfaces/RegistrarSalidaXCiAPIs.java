package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visita;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RegistrarSalidaXCiAPIs {
    @POST("visita/registrarSalidaXCi")
    Call<Visita> registrarSalidaXCi(@Query("recCod") String recCod, @Query("ci") String ci);
}
