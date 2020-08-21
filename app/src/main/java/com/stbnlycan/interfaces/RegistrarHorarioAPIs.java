package com.stbnlycan.interfaces;

import com.stbnlycan.models.Horario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RegistrarHorarioAPIs {
    @POST("horario/registrar")
    Call<Horario> registrarHorario(@Body Horario body, @Header("Authorization") String authHeader);
}
