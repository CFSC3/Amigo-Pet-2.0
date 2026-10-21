package com.ifam.amigopetfixo.Model.bean;

public class CadastroQ {

    private String email;
    private String senha;
    private String confSenha;

    public CadastroQ(String email, String senha, String confSenha) {
        this.email = email;
        this.senha = senha;
        this.confSenha = confSenha;
    }

    public String getConfSenha() {
        return confSenha;
    }

    public void setConfSenha(String confSenha) {
        this.confSenha = confSenha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
