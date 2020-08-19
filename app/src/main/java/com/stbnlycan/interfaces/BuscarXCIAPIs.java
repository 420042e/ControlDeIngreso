package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visitante;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BuscarXCIAPIs {
    @GET("visitante/buscarXCi")
    Call<List<Visitante>> buscarXQR(@Query("ci") String ci);
}
