package com.stbnlycan.interfaces;

import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.Motivo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface MotivosAPIs {
    @GET("motivoIngreso/lista")
    Call<List<Motivo>> listaMotivo(@Header("Authorization") String authHeader);
}
