package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListaUsuarios {
    @SerializedName("content")
    @Expose
    private List<Usuario> lUsuario;

    public List<Usuario> getlUsuario() {
        return lUsuario;
    }

    public void setlUsuario(List<Usuario> lUsuario) {
        this.lUsuario = lUsuario;
    }
}
