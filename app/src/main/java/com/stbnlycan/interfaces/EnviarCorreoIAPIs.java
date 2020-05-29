package com.stbnlycan.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EnviarCorreoIAPIs {
    /*
    Get request to fetch city weather.Takes in two parameter-city name and API key.
    */
    @GET("visitante/enviarCorreoIngreso")
    Call<String> enviarCorreo(@Query("correo") String correo);
}