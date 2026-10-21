package com.ifam.amigopetfixo.Model.bean;

public class Postagem {
    private String idPostagem;
    private String idUsuario;
    private String nomeUsuario;
    private String username; // O nosso novo @username
    private String textoPostagem;
    private String urlMidia; // Link da foto OU da miniatura (thumbnail) do vídeo
    private String tipoMidia; // "foto" ou "video"
    private String urlVideo; // Link do vídeo real no Storage (se o tipo for "video")

    public Postagem() {
        // Construtor vazio exigido pelo Firebase
    }

    public Postagem(String idPostagem, String idUsuario, String nomeUsuario, String username,
                    String textoPostagem, String urlMidia, String tipoMidia, String urlVideo) {
        this.idPostagem = idPostagem;
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.username = username;
        this.textoPostagem = textoPostagem;
        this.urlMidia = urlMidia;
        this.tipoMidia = tipoMidia;
        this.urlVideo = urlVideo;
    }

    // Getters e Setters
    public String getIdPostagem() { return idPostagem; }
    public void setIdPostagem(String idPostagem) { this.idPostagem = idPostagem; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTextoPostagem() { return textoPostagem; }
    public void setTextoPostagem(String textoPostagem) { this.textoPostagem = textoPostagem; }

    public String getUrlMidia() { return urlMidia; }
    public void setUrlMidia(String urlMidia) { this.urlMidia = urlMidia; }

    public String getTipoMidia() { return tipoMidia; }
    public void setTipoMidia(String tipoMidia) { this.tipoMidia = tipoMidia; }

    public String getUrlVideo() { return urlVideo; }
    public void setUrlVideo(String urlVideo) { this.urlVideo = urlVideo; }
}