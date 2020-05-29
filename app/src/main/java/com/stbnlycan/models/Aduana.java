package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Aduana implements Serializable {
    @SerializedName("aduCod")
    @Expose
    private String aduCod;

    @SerializedName("aduNombre")
    @Expose
    private String aduNombre;

    @SerializedName("aduPais")
    @Expose
    private String aduPais;

    @SerializedName("aduEstado")
    @Expose
    private String aduEstado;

    public String getAduCod() {
        return aduCod;
    }

    public String getAduNombre() {
        return aduNombre;
    }

    public String getAduPais() {
        return aduPais;
    }

    public String getAduEstado() {
        return aduEstado;
    }

    public void setAduCod(String aduCod) {
        this.aduCod = aduCod;
    }

    public void setAduNombre(String aduNombre) {
        this.aduNombre = aduNombre;
    }

    public void setAduPais(String aduPais) {
        this.aduPais = aduPais;
    }

    public void setAduEstado(String aduEstado) {
        this.aduEstado = aduEstado;
    }

    public Aduana() {

    }

    public Aduana(String aduCod, String aduNombre, String aduPais, String aduEstado) {
        this.aduCod = aduCod;
        this.aduNombre = aduNombre;
        this.aduPais = aduPais;
        this.aduEstado = aduEstado;
    }
}
