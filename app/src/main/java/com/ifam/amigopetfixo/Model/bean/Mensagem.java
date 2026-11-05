package com.ifam.amigopetfixo.Model.bean;

public class Mensagem {
    private String idMensagem;
    private String idRemetente;
    private String texto;
    private String urlMidia;
    private String tipoMensagem;
    private long dataHora;

    public Mensagem() {}

    public String getIdMensagem() { return idMensagem; }
    public void setIdMensagem(String idMensagem) { this.idMensagem = idMensagem; }

    public String getIdRemetente() { return idRemetente; }
    public void setIdRemetente(String idRemetente) { this.idRemetente = idRemetente; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getUrlMidia() { return urlMidia; }
    public void setUrlMidia(String urlMidia) { this.urlMidia = urlMidia; }

    public String getTipoMensagem() { return tipoMensagem; }
    public void setTipoMensagem(String tipoMensagem) { this.tipoMensagem = tipoMensagem; }

    public long getDataHora() { return dataHora; }
    public void setDataHora(long dataHora) { this.dataHora = dataHora; }
}