package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visitante;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VisitanteAPIs {
    @POST("visitante/editar")
    Call<Visitante> editarVisitante(@Body Visitante body);
}
