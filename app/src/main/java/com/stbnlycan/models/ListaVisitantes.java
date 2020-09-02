package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListaVisitantes {
    @SerializedName("content")
    @Expose
    private List<Visitante> lVisitante;

    @SerializedName("totalElements")
    @Expose
    private String totalElements;

    public List<Visitante> getlVisitante() {
        return lVisitante;
    }

    public void setlVisitante(List<Visitante> lVisitante) {
        this.lVisitante = lVisitante;
    }

    public String getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(String totalElements) {
        this.totalElements = totalElements;
    }
}
