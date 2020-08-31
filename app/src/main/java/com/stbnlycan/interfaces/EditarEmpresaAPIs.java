package com.stbnlycan.interfaces;

import com.stbnlycan.models.Empresa;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface EditarEmpresaAPIs {
    @PUT("empresa/actualizar")
    Call<Empresa> editarEmpresa(@Body Empresa body, @Header("Authorization") String authHeader);
}
