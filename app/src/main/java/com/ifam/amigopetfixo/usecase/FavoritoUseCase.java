package com.ifam.amigopetfixo.usecase;

import com.ifam.amigopetfixo.repository.FavoritoRepository;

public class FavoritoUseCase {

    private final FavoritoRepository repository;

    public FavoritoUseCase() {
        this.repository = new FavoritoRepository();
    }

    public void escutarStatus(String uidVisitado, FavoritoRepository.OnStatusFavoritoListener listener) {
        if (uidVisitado != null && !uidVisitado.isEmpty()) {
            repository.verificarStatusFavorito(uidVisitado, listener);
        }
    }

    public void aoClicarFavoritar(String uidVisitado, boolean isFavorito) {
        if (uidVisitado != null && !uidVisitado.isEmpty()) {
            repository.alternarFavorito(uidVisitado, isFavorito);
        }
    }
}