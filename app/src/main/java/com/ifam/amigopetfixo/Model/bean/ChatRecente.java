package com.ifam.amigopetfixo.Model.bean;

public class ChatRecente {
    private String idSalaChat;
    private String idDestinatario;
    private String ultimaMensagem;
    private boolean lida = true;
    private long timestamp;

    public ChatRecente() {}

    public String getIdSalaChat() { return idSalaChat; }
    public void setIdSalaChat(String idSalaChat) { this.idSalaChat = idSalaChat; }

    public String getIdDestinatario() { return idDestinatario; }
    public void setIdDestinatario(String idDestinatario) { this.idDestinatario = idDestinatario; }

    public String getUltimaMensagem() { return ultimaMensagem; }
    public void setUltimaMensagem(String ultimaMensagem) { this.ultimaMensagem = ultimaMensagem; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}