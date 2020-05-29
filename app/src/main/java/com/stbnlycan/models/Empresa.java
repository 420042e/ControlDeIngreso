package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Empresa implements Serializable {

    @SerializedName("empCod")
    @Expose
    private String empCod;

    @SerializedName("empNombre")
    @Expose
    private String empNombre;

    @SerializedName("empObs")
    @Expose
    private String empObs;

    public String getEmpCod() {
        return empCod;
    }

    public void setEmpCod(String empCod) {
        this.empCod = empCod;
    }

    public String getEmpNombre() {
        return empNombre;
    }

    public void setEmpNombre(String empNombre) {
        this.empNombre = empNombre;
    }

    public String getEmpObs() {
        return empObs;
    }

    public void setEmpObs(String empObs) {
        this.empObs = empObs;
    }

    public Empresa() {
    }

    public Empresa(String empCod, String empNombre, String empObs) {
        this.empCod = empCod;
        this.empNombre = empNombre;
        this.empObs = empObs;
    }

    @Override
    public String toString() {
        return empNombre;
    }
}
