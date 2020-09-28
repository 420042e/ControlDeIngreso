package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Horario implements Serializable
{
    @SerializedName("horCod")
    @Expose
    private String horCod;

    @SerializedName("horNombre")
    @Expose
    private String horNombre;

    @SerializedName("horDescripcion")
    @Expose
    private String horDescripcion;

    @SerializedName("horDias")
    @Expose
    private String horDias;

    @SerializedName("horHoraEntrada")
    @Expose
    private String horHoraEntrada;

    @SerializedName("horMinEntrada")
    @Expose
    private String horMinEntrada;

    @SerializedName("horHoraSalida")
    @Expose
    private String horHoraSalida;

    @SerializedName("horMinSalida")
    @Expose
    private String horMinSalida;

    @SerializedName("horEstado")
    @Expose
    private String horEstado;

    @SerializedName("recinto")
    @Expose
    private Recinto recinto;

    @SerializedName("tipoVisitante")
    @Expose
    private TipoVisitante tipoVisitante;

    public String getHorCod() {
        return horCod;
    }

    public void setHorCod(String horCod) {
        this.horCod = horCod;
    }

    public String getHorNombre() {
        return horNombre;
    }

    public void setHorNombre(String horNombre) {
        this.horNombre = horNombre;
    }

    public String getHorDescripcion() {
        return horDescripcion;
    }

    public void setHorDescripcion(String horDescripcion) {
        this.horDescripcion = horDescripcion;
    }

    public String getHorDias() {
        return horDias;
    }

    public void setHorDias(String horDias) {
        this.horDias = horDias;
    }

    public String getHorHoraEntrada() {
        return horHoraEntrada;
    }

    public void setHorHoraEntrada(String horHoraEntrada) {
        this.horHoraEntrada = horHoraEntrada;
    }

    public String getHorMinEntrada() {
        return horMinEntrada;
    }

    public void setHorMinEntrada(String horMinEntrada) {
        this.horMinEntrada = horMinEntrada;
    }

    public String getHorHoraSalida() {
        return horHoraSalida;
    }

    public void setHorHoraSalida(String horHoraSalida) {
        this.horHoraSalida = horHoraSalida;
    }

    public String getHorMinSalida() {
        return horMinSalida;
    }

    public void setHorMinSalida(String horMinSalida) {
        this.horMinSalida = horMinSalida;
    }

    public String getHorEstado() {
        return horEstado;
    }

    public void setHorEstado(String horEstado) {
        this.horEstado = horEstado;
    }

    public Recinto getRecinto() {
        return recinto;
    }

    public void setRecinto(Recinto recinto) {
        this.recinto = recinto;
    }

    public TipoVisitante getTipoVisitante() {
        return tipoVisitante;
    }

    public void setTipoVisitante(TipoVisitante tipoVisitante) {
        this.tipoVisitante = tipoVisitante;
    }
}
