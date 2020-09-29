package com.stbnlycan.interfaces;

import com.stbnlycan.models.Motivo;
import com.stbnlycan.models.TipoDocumento;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface TipoDocAPIs {
    @GET("tipoDocumento/lista")
    Call<List<TipoDocumento>> listaTipoDoc(@Header("Authorization") String authHeader);
}
