package com.stbnlycan.interfaces;

import com.stbnlycan.models.Horario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HorariosAPIs {
    /*
    Get request to fetch city weather.Takes in two parameter-city name and API key.
    */
    @GET("horario/listaPorRecinto")
    Call <List<Horario>> listaHorarios(@Query("recCod") String recCod, @Query("tviCod") String tviCod, @Query("horNombre") String horNombre, @Query("dia") String dia);
}
