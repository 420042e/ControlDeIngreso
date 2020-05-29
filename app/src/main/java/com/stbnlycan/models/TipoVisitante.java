package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TipoVisitante implements Serializable {
    @SerializedName("tviCod")
    @Expose
    private String tviCod;

    @SerializedName("tviNombre")
    @Expose
    private String tviNombre;

    @SerializedName("tviDescripcion")
    @Expose
    private String tviDescripcion;

    @SerializedName("horEstado")
    @Expose
    private String horEstado;

    public String getTviCod() {
        return tviCod;
    }

    public void setTviCod(String tviCod) {
        this.tviCod = tviCod;
    }

    public String getTviNombre() {
        return tviNombre;
    }

    public void setTviNombre(String tviNombre) {
        this.tviNombre = tviNombre;
    }

    public String getTviDescripcion() {
        return tviDescripcion;
    }

    public void setTviDescripcion(String tviDescripcion) {
        this.tviDescripcion = tviDescripcion;
    }

    public String getHorEstado() {
        return horEstado;
    }

    public void setHorEstado(String horEstado) {
        this.horEstado = horEstado;
    }

    public TipoVisitante() {
    }

    @Override
    public String toString() {
        return tviNombre;
    }
}
