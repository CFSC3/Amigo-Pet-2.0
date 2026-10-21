package com.ifam.amigopetfixo.Model.bean;

import android.widget.ImageView;

public class Perfil_dono {

    private String nome;
    private String texto;
    private String email;
    private String id;
    private String qtd;
    private ImageView foto;

    public Perfil_dono() {
    }

    public Perfil_dono(String nome, String celular, String texto, String email, String id, ImageView foto, String qtd) {
        this.nome = nome;
        this.texto = texto;
        this.email = email;
        this.id = id;
        this.foto = foto;
        this.qtd = qtd;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ImageView getFoto() {
        return foto;
    }

    public void setFoto(ImageView foto) {
        this.foto = foto;
    }

    public String getQtd() {
        return qtd;
    }

    public void setQtd(String qtd) {
        this.qtd = qtd;
    }

}
