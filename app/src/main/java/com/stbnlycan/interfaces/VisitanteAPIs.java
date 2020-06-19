package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visitante;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

public interface VisitanteAPIs {
    @PUT("visitante/editar")
    Call<Visitante> editarVisitante(@Body Visitante body);
}
