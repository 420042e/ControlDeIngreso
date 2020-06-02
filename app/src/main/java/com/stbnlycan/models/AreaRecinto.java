package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AreaRecinto implements Serializable {
    @SerializedName("areaCod")
    @Expose
    private String areaCod;

    @SerializedName("areaNombre")
    @Expose
    private String areaNombre;

    @SerializedName("areaDescripcion")
    @Expose
    private String areaDescripcion;

    @SerializedName("areaEstado")
    @Expose
    private String areaEstado;

    @SerializedName("recinto")
    @Expose
    private Recinto recinto;

    public String getAreaCod() {
        return areaCod;
    }

    public void setAreaCod(String areaCod) {
        this.areaCod = areaCod;
    }

    public String getAreaNombre() {
        return areaNombre;
    }

    public void setAreaNombre(String areaNombre) {
        this.areaNombre = areaNombre;
    }

    public String getAreaDescripcion() {
        return areaDescripcion;
    }

    public void setAreaDescripcion(String areaDescripcion) {
        this.areaDescripcion = areaDescripcion;
    }

    public String getAreaEstado() {
        return areaEstado;
    }

    public void setAreaEstado(String areaEstado) {
        this.areaEstado = areaEstado;
    }

    public Recinto getRecinto() {
        return recinto;
    }

    public void setRecinto(Recinto recinto) {
        this.recinto = recinto;
    }

    public AreaRecinto() {
    }

    @Override
    public String toString() {
        return areaNombre;
    }
}
