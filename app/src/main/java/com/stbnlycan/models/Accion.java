package com.stbnlycan.models;

public class Accion
{
    private int id;
    private String nombre;

    public int getRecurso() {
        return recurso;
    }

    private int recurso;

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }


    public Accion(int id, String nombre, int recurso) {
        this.id = id;
        this.nombre = nombre;
        this.recurso = recurso;
    }
}
