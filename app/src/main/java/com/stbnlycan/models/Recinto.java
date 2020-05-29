package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Recinto implements Serializable {
    @SerializedName("recCod")
    @Expose
    private String recCod;

    @SerializedName("recNombre")
    @Expose
    private String recNombre;

    @SerializedName("recNombrea")
    @Expose
    private String recNombrea;

    @SerializedName("recEstado")
    @Expose
    private String recEstado;

    @SerializedName("recTipo")
    @Expose
    private String recTipo;

    @SerializedName("aduana")
    @Expose
    private Aduana aduana;

    public String getRecCod() {
        return recCod;
    }

    public String getRecNombre() {
        return recNombre;
    }

    public String getRecNombrea() {
        return recNombrea;
    }

    public String getRecEstado() {
        return recEstado;
    }

    public String getRecTipo() {
        return recTipo;
    }

    public Aduana getAduana() {
        return aduana;
    }

    public void setRecCod(String recCod) {
        this.recCod = recCod;
    }

    public void setRecNombre(String recNombre) {
        this.recNombre = recNombre;
    }

    public void setRecNombrea(String recNombrea) {
        this.recNombrea = recNombrea;
    }

    public void setRecEstado(String recEstado) {
        this.recEstado = recEstado;
    }

    public void setRecTipo(String recTipo) {
        this.recTipo = recTipo;
    }

    public void setAduana(Aduana aduana) {
        this.aduana = aduana;
    }

    public Recinto() {

    }

    public Recinto(String recCod, String recNombre, String recNombrea, String recEstado, String recTipo, Aduana aduana) {
        this.recCod = recCod;
        this.recNombre = recNombre;
        this.recNombrea = recNombrea;
        this.recEstado = recEstado;
        this.recTipo = recTipo;
        this.aduana = aduana;
    }

    @Override
    public String toString() {
        return recNombre;
    }
}
