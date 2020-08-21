package com.stbnlycan.interfaces;

import com.google.gson.JsonObject;
import com.stbnlycan.models.Visita;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface RegistrarIngresoAPIs {
    /*@POST("visita/registrarIngreso")
    Call<Visita> registrarIngreso(@Body Visita body);*/
    @POST("visita/registrarIngreso")
    Call<JsonObject> registrarIngreso(@Body Visita body, @Header("Authorization") String authHeader);

    /*@POST("visita/registrarIngreso")
    Call<Object> registrarIngreso(@Body Visita body);*/
}
