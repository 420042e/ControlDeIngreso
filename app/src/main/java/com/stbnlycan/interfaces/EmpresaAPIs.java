package com.stbnlycan.interfaces;

import com.stbnlycan.models.AreaRecinto;
import com.stbnlycan.models.Empresa;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EmpresaAPIs {
    @GET("empresa/lista")
    Call<List<Empresa>> listaEmpresas();
}
