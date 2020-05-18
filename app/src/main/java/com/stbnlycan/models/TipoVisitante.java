package com.stbnlycan.models;

import java.io.Serializable;

public class TipoVisitante implements Serializable {
    private String tviCod;
    private String tviNombre;
    private String tviDescripcion;
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
