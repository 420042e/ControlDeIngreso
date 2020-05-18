package com.stbnlycan.models;

public class Recinto {
    private String recCod;
    private String recNombre;
    private String recNombrea;
    private String recEstado;
    private String recTipo;
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
}
