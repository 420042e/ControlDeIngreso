package com.stbnlycan.interfaces;

import com.stbnlycan.models.Horario;
import com.stbnlycan.models.Visita;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RegistrarSalidaAPIs
{
    @POST("visita/registrarSalida")
    Call<Visita> registrarSalida(@Query("recCod") String recCod, @Query("llave") String llave);
}
