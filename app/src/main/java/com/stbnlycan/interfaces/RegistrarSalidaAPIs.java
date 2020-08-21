package com.stbnlycan.interfaces;

import com.google.gson.JsonObject;
import com.stbnlycan.models.Horario;
import com.stbnlycan.models.Visita;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RegistrarSalidaAPIs
{
    /*@POST("visita/registrarSalida")
    Call<Visita> registrarSalida(@Query("recCod") String recCod, @Query("llave") String llave);*/
    @POST("visita/registrarSalida")
    Call<JsonObject> registrarSalida(@Query("recCod") String recCod, @Query("llave") String llave, @Header("Authorization") String authHeader);
}
