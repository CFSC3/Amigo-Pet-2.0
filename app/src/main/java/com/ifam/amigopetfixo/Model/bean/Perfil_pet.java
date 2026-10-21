package com.ifam.amigopetfixo.Model.bean;

public class Perfil_pet {

    private String idPet;
    private String nomePet;
    private String especie;
    private String raca;
    private String cor;
    private String castrado;
    private String urlFotoPet;

    public Perfil_pet() {
    }

    public Perfil_pet(String idPet, String nomePet, String especie, String raca, String cor, String castrado, String urlFotoPet) {
        this.idPet = idPet;
        this.nomePet = nomePet;
        this.especie = especie;
        this.raca = raca;
        this.cor = cor;
        this.castrado = castrado;
        this.urlFotoPet = urlFotoPet;
    }

    // Getters e Setters
    public String getIdPet() { return idPet; }
    public void setIdPet(String idPet) { this.idPet = idPet; }

    public String getNomePet() { return nomePet; }
    public void setNomePet(String nomePet) { this.nomePet = nomePet; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getCastrado() { return castrado; }
    public void setCastrado(String castrado) { this.castrado = castrado; }

    public String getUrlFotoPet() { return urlFotoPet; }
    public void setUrlFotoPet(String urlFotoPet) { this.urlFotoPet = urlFotoPet; }
}