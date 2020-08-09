package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListaVisitas {
    @SerializedName("content")
    @Expose
    private List<Visita> lVisita;

    @SerializedName("totalElements")
    @Expose
    private int totalElements;

    public int getTotalElements() {
        return totalElements;
    }

    public List<Visita> getlVisita() {
        return lVisita;
    }

    public ListaVisitas() {
    }

}
