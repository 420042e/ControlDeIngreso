package com.stbnlycan.interfaces;

import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.Visitante;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface AreaRecintoAPIs {
    @GET("areaRecinto/listaPorRecinto")
    Call<List<AreaRecinto>> listaPorRecinto(@Query("recCod") String recCod, @Header("Authorization") String authHeader);
}
