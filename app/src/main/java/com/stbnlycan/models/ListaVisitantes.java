package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListaVisitantes {
    @SerializedName("content")
    @Expose
    private List<Visitante> lVisitante;

    public List<Visitante> getlVisitante() {
        return lVisitante;
    }

    public ListaVisitantes() {
    }
}
