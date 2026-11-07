package com.ifam.amigopetfixo.usecase;

import com.ifam.amigopetfixo.repository.UsuarioRepository;
import java.util.ArrayList;

public class PesquisaUsuarioUseCase {

    private final UsuarioRepository repository;

    public PesquisaUsuarioUseCase() {
        this.repository = new UsuarioRepository();
    }

    public void executarBusca(String textoDigitado, UsuarioRepository.OnBuscaUsuariosListener callbackTela) {
        if (textoDigitado == null || textoDigitado.trim().isEmpty()) {
            callbackTela.onSuccess(new ArrayList<>());
            return;
        }

        repository.buscarUsuariosMultiplos(textoDigitado, callbackTela);
    }
}