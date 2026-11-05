package com.ifam.amigopetfixo.usecase;

import com.ifam.amigopetfixo.repository.UsuarioRepository;
import java.util.List;
import java.util.Map;

public class PesquisaUsuarioUseCase {

    private final UsuarioRepository repository;

    public PesquisaUsuarioUseCase() {
        this.repository = new UsuarioRepository();
    }

    public void executarBusca(String textoDigitado, UsuarioRepository.OnBuscaUsuariosListener callbackTela) {
        if (textoDigitado == null || textoDigitado.trim().length() < 2) {
            callbackTela.onSuccess(null);
            return;
        }

        String queryLimpa = textoDigitado.trim().replace("@", "");
        repository.buscarUsuariosPorUsername(queryLimpa, callbackTela);
    }
}