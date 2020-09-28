package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TipoDocumento implements Serializable {
    @SerializedName("tdoCod")
    @Expose
    private Integer tdoCod;

    @SerializedName("tdoNombre")
    @Expose
    private String tdoNombre;

    @SerializedName("tdoDescripcion")
    @Expose
    private String tdoDescripcion;

    @SerializedName("documentosIngreso")
    @Expose
    private List<DocumentoIngreso> documentosIngreso = new ArrayList<>();

    public Integer getTdoCod() {
        return tdoCod;
    }

    public void setTdoCod(Integer tdoCod) {
        this.tdoCod = tdoCod;
    }

    public String getTdoNombre() {
        return tdoNombre;
    }

    public void setTdoNombre(String tdoNombre) {
        this.tdoNombre = tdoNombre;
    }

    public String getTdoDescripcion() {
        return tdoDescripcion;
    }

    public void setTdoDescripcion(String tdoDescripcion) {
        this.tdoDescripcion = tdoDescripcion;
    }

    public List<DocumentoIngreso> getDocumentosIngreso() {
        return documentosIngreso;
    }

    public void setDocumentosIngreso(List<DocumentoIngreso> documentosIngreso) {
        this.documentosIngreso = documentosIngreso;
    }

    @Override
    public String toString() {
        return tdoNombre;
    }
}
