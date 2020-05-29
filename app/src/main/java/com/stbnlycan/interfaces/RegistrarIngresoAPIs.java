package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visita;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegistrarIngresoAPIs {
    @POST("visita/registrarIngreso")
    Call<Visita> registrarIngreso(@Body Visita body);
}
