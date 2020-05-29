package com.stbnlycan.interfaces;

import com.stbnlycan.models.TipoVisitante;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TipoVisitanteAPIs {
    @GET("tipoVisitante/lista")
    Call<List<TipoVisitante>> listaTipoVisitante();
}
