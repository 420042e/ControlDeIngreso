package com.stbnlycan.interfaces;

import com.google.gson.JsonObject;
import com.stbnlycan.models.AreaRecinto;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginAPIs {
    @FormUrlEncoded
    @POST("oauth/token")
    Call<JsonObject> login(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> fields);
}
