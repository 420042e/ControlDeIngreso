package com.stbnlycan.interfaces;

import com.stbnlycan.models.Empresa;
import com.stbnlycan.models.Horario;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegistrarEmpresaAPIs {
    @POST("empresa/registrar")
    Call<Empresa> registrarEmpresa(@Body Empresa body);
}
