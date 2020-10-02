package com.stbnlycan.interfaces;

import com.stbnlycan.models.Visita;
import com.stbnlycan.models.Visitante;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RegistrarIngreso2APIs {
    @Multipart
    @POST("visita/registrarIngreso")
    Call<String> uploadImage(@Part List<MultipartBody.Part> file, @Part("vis") RequestBody requestBody, @Header("Authorization") String authHeader);
}