package com.ifam.amigopetfixo.Model.bean;

import android.widget.ImageView;

import java.util.Date;

public class Poste {

    private ImageView[] imagens;
    private Date data_post;
    private Boolean curtida;
    private String descricao;

    public Poste(ImageView[] imagens, Date data_post, Boolean curtida, String descricao) {
        this.imagens = imagens;
        this.data_post = data_post;
        this.curtida = curtida;
        this.descricao = descricao;
    }

    public ImageView[] getImagens() {
        return imagens;
    }

    public void setImagens(ImageView[] imagens) {
        this.imagens = imagens;
    }

    public Date getData_post() {
        return data_post;
    }

    public void setData_post(Date data_post) {
        this.data_post = data_post;
    }

    public Boolean getCurtida() {
        return curtida;
    }

    public void setCurtida(Boolean curtida) {
        this.curtida = curtida;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
