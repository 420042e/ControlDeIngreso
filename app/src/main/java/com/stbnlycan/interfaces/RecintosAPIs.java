package com.stbnlycan.interfaces;

import com.stbnlycan.models.Recinto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface RecintosAPIs {
    @GET("recinto/lista")
    Call<List<Recinto>> listaRecintos(@Header("Authorization") String authHeader);
}
