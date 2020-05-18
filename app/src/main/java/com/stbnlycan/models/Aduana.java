package com.stbnlycan.models;

public class Aduana {
    private String aduCod;
    private String aduNombre;
    private String aduPais;
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

    public Aduana(String aduCod, String aduNombre, String aduPais, String aduEstado) {
        this.aduCod = aduCod;
        this.aduNombre = aduNombre;
        this.aduPais = aduPais;
        this.aduEstado = aduEstado;
    }
}
