package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visitante;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ListaVisitantesAPIs {
    @GET("visitante/lista")
    Call<List<Visitante>> listaVisitantes();
}
