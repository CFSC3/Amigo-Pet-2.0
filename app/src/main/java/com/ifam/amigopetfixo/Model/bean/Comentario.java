package com.ifam.amigopetfixo.Model.bean;

public class Comentario {
    private String idComentario;
    private String idUsuario;
    private String texto;

    public Comentario() {
    }

    public String getIdComentario() { return idComentario; }
    public void setIdComentario(String idComentario) { this.idComentario = idComentario; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}