package com.stbnlycan.interfaces;

import com.stbnlycan.models.ListaEmpresas;
import com.stbnlycan.models.ListaVisitantes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ListaEmpresasAPIs {
    @GET("empresa/lista")
    Call<ListaEmpresas> listaEmpresas(@Query("page") String page, @Query("size") String size);
}
