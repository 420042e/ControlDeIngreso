package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DocumentoIngreso implements Serializable {
    @SerializedName("doiCod")
    @Expose
    private Integer doiCod;

    @SerializedName("doiImagen")
    @Expose
    private String doiImagen;

    @SerializedName("doiDocumento")
    @Expose
    private String doiDocumento;

    @SerializedName("tipoDocumento")
    @Expose
    private TipoDocumento tipoDocumento;


    public Integer getDoiCod() {
        return doiCod;
    }

    public void setDoiCod(Integer doiCod) {
        this.doiCod = doiCod;
    }

    public String getDoiImagen() {
        return doiImagen;
    }

    public void setDoiImagen(String doiImagen) {
        this.doiImagen = doiImagen;
    }

    public String getDoiDocumento() {
        return doiDocumento;
    }

    public void setDoiDocumento(String doiDocumento) {
        this.doiDocumento = doiDocumento;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

}
