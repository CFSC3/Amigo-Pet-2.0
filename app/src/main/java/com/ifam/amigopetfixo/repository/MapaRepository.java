package com.ifam.amigopetfixo.repository;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MapaRepository {

    public static class LocalMapa {
        public String id;
        public String tipo;
        public String nome;
        public String endereco;
        public LatLng coordenada;
        public String urlImagemLogo;

        public LocalMapa(String id, String tipo, String nome, String endereco, LatLng coordenada) {
            this.id = id;
            this.tipo = tipo;
            this.nome = nome;
            this.endereco = endereco;
            this.coordenada = coordenada;

            this.urlImagemLogo = null;
        }
    }

    public MapaRepository() {

    }

    // Método que o Fragment vai chamar para carregar os pinos
    public void buscarLocais(String cidadeAtualUsuario, String filtroCategoria, OnLocaisLoadedListener listener) {

        // --- INÍCIO DO MOCK (Dados Falsos Estáticos) ---

        List<LocalMapa> listaFake = new ArrayList<>();

        if (cidadeAtualUsuario != null && cidadeAtualUsuario.equalsIgnoreCase("manaus")) {

            if (filtroCategoria.equals("TODOS") || filtroCategoria.equals("VETS")) {
                listaFake.add(new LocalMapa("1", "VET", "Pettopia Clínica Veterinária", "R. Santo Antônio - Coroado", new LatLng(-3.088834, -59.985952)));
                listaFake.add(new LocalMapa("2", "VET", "Pet Shop Bicho Solto", "R. Santo Antônio, 148 - Coroado", new LatLng(-3.089904, -59.985815)));
                listaFake.add(new LocalMapa("3", "VET", "Clínica Veterinária Parque 10", "R. 6, 224 - Parque Dez de Novembro", new LatLng(-3.082792, -60.012217)));
            }

            if (filtroCategoria.equals("TODOS") || filtroCategoria.equals("ONGS")) {
                listaFake.add(new LocalMapa("4", "ONG", "Ong Anjos de Rua Manaus", "Tv. Ouroeste - Aleixo", new LatLng(-3.100713, -59.999588)));
                listaFake.add(new LocalMapa("5", "ONG", "Centro de Controle de Zoonoses", "Av. Brasil - Compensa", new LatLng(-3.108030, -60.053798)));
                listaFake.add(new LocalMapa("6", "ONG", "Casa dos Animais", "R. Cel. Ferreira de Araújo - Petrópolis", new LatLng(-3.117164, -59.995959)));
            }
        }

        listener.onSuccess(listaFake);
        // --- FIM DO MOCK ---
    }

    public interface OnLocaisLoadedListener {
        void onSuccess(List<LocalMapa> locais);
        void onError(String erro);
    }
}