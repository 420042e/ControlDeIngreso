package com.stbnlycan.interfaces;

import com.stbnlycan.models.DocumentoIngreso;
import com.stbnlycan.models.ListaEmpresas;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface DocIngsAPIs {
    @GET("documentoIngreso/buscarXVisita")
    Call<List<DocumentoIngreso>> listaDocIngs(@Query("visita") String visita, @Header("Authorization") String authHeader);
}
