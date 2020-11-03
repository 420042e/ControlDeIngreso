package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListaEmpresas {
    @SerializedName("content")
    @Expose
    private List<Empresa> lEmpresa;

    @SerializedName("totalElements")
    @Expose
    private String totalElements;

    public List<Empresa> getlEmpresa() {
        return lEmpresa;
    }

    public ListaEmpresas() {
    }

    public String getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(String totalElements) {
        this.totalElements = totalElements;
    }
}
