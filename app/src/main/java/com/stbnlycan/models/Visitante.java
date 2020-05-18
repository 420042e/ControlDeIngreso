package com.stbnlycan.models;

import java.io.Serializable;

public class Visitante implements Serializable {
    private  String vteCi;
    private String vteCorreo;
    private String vteImagen;
    private String vteNombre;
    private String vteApellidos;
    private String vteTelefono;
    private String vteDireccion;
    private String vteEstado;
    private String vteLlave;
    private String vteFecha;
    private TipoVisitante tipoVisitante;
    private Empresa empresa;

    public String getVteCi() {
        return vteCi;
    }

    public void setVteCi(String vteCi) {
        this.vteCi = vteCi;
    }

    public String getVteCorreo() {
        return vteCorreo;
    }

    public void setVteCorreo(String vteCorreo) {
        this.vteCorreo = vteCorreo;
    }

    public String getVteImagen() {
        return vteImagen;
    }

    public void setVteImagen(String vteImagen) {
        this.vteImagen = vteImagen;
    }

    public String getVteNombre() {
        return vteNombre;
    }

    public void setVteNombre(String vteNombre) {
        this.vteNombre = vteNombre;
    }

    public String getVteApellidos() {
        return vteApellidos;
    }

    public void setVteApellidos(String vteApellidos) {
        this.vteApellidos = vteApellidos;
    }

    public String getVteTelefono() {
        return vteTelefono;
    }

    public void setVteTelefono(String vteTelefono) {
        this.vteTelefono = vteTelefono;
    }

    public String getVteDireccion() {
        return vteDireccion;
    }

    public void setVteDireccion(String vteDireccion) {
        this.vteDireccion = vteDireccion;
    }

    public String getVteEstado() {
        return vteEstado;
    }

    public void setVteEstado(String vteEstado) {
        this.vteEstado = vteEstado;
    }

    public String getVteLlave() {
        return vteLlave;
    }

    public void setVteLlave(String vteLlave) {
        this.vteLlave = vteLlave;
    }

    public String getVteFecha() {
        return vteFecha;
    }

    public void setVteFecha(String vteFecha) {
        this.vteFecha = vteFecha;
    }

    public TipoVisitante getTipoVisitante() {
        return tipoVisitante;
    }

    public void setTipoVisitante(TipoVisitante tipoVisitante) {
        this.tipoVisitante = tipoVisitante;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Visitante() {
    }
}
