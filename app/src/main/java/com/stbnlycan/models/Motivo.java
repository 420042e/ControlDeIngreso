package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Motivo implements Serializable {
    @SerializedName("mvoCod")
    @Expose
    private Integer mvoCod;

    @SerializedName("mvoNombre")
    @Expose
    private String mvoNombre;

    @SerializedName("mvoDescripcion")
    @Expose
    private String mvoDescripcion;

    @SerializedName("visitas")
    @Expose
    private List<Visita> visitas = new ArrayList<>();

    public Integer getMvoCod() {
        return mvoCod;
    }

    public void setMvoCod(Integer mvoCod) {
        this.mvoCod = mvoCod;
    }

    public String getMvoNombre() {
        return mvoNombre;
    }

    public void setMvoNombre(String mvoNombre) {
        this.mvoNombre = mvoNombre;
    }

    public String getMvoDescripcion() {
        return mvoDescripcion;
    }

    public void setMvoDescripcion(String mvoDescripcion) {
        this.mvoDescripcion = mvoDescripcion;
    }

    public List<Visita> getVisitas() {
        return visitas;
    }

    public void setVisitas(List<Visita> visitas) {
        this.visitas = visitas;
    }

    @Override
    public String toString() {
        return mvoNombre;
    }
}
