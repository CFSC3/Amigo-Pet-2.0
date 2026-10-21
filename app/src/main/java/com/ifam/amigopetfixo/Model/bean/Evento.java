package com.ifam.amigopetfixo.Model.bean;

public class Evento {

    private String idEvento;
    private String idUsuario;
    private String titulo;
    private String resumo;
    private String descricao;
    private String urlImagem;
    private String dataPostagem;
    private String dataEvento;
    private String dataEventoFim;
    private String horarioEvento;
    private String horarioEventoFim;
    private String endereco;
    private String cidadeFiltro;

    public Evento() {
    }

    // Getters e Setters
    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getUrlImagem() { return urlImagem; }
    public void setUrlImagem(String urlImagem) { this.urlImagem = urlImagem; }

    public String getDataPostagem() { return dataPostagem; }
    public void setDataPostagem(String dataPostagem) { this.dataPostagem = dataPostagem; }

    public String getDataEvento() { return dataEvento; }
    public void setDataEvento(String dataEvento) { this.dataEvento = dataEvento; }

    public String getDataEventoFim() { return dataEventoFim; }
    public void setDataEventoFim(String dataEventoFim) { this.dataEventoFim = dataEventoFim; }

    public String getHorarioEvento() { return horarioEvento; }
    public void setHorarioEvento(String horarioEvento) { this.horarioEvento = horarioEvento; }

    public String getHorarioEventoFim() { return horarioEventoFim; }
    public void setHorarioEventoFim(String horarioEventoFim) { this.horarioEventoFim = horarioEventoFim; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidadeFiltro() { return cidadeFiltro; }
    public void setCidadeFiltro(String cidadeFiltro) { this.cidadeFiltro = cidadeFiltro; }
}