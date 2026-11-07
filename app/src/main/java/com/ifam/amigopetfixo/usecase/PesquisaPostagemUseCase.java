package com.ifam.amigopetfixo.usecase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.repository.PostagemRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PesquisaPostagemUseCase {
    private final PostagemRepository repository;
    private String ultimoIdCarregado = null;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private static final int BATCH_SIZE = 60;
    private String currentQuery = "";

    public PesquisaPostagemUseCase() {
        this.repository = new PostagemRepository();
    }

    public void pesquisarFeed(String query, boolean isRefresh, FeedUseCase.OnFeedLoadListener listener) {
        if (isLoading || (isLastPage && !isRefresh)) return;

        query = query.toLowerCase().trim();
        if (isRefresh || !currentQuery.equals(query)) {
            ultimoIdCarregado = null;
            isLastPage = false;
            currentQuery = query;
        }

        isLoading = true;
        // Passamos o 'isRefresh' adiante para garantir a limpeza da tela
        buscarLoteRecursivo(new ArrayList<>(), isRefresh, listener);
    }

    private void buscarLoteRecursivo(List<Postagem> encontradas, boolean isRefresh, FeedUseCase.OnFeedLoadListener listener) {
        int limiteBusca = (ultimoIdCarregado == null) ? BATCH_SIZE : BATCH_SIZE + 1;

        repository.buscarPostagensPaginadas(limiteBusca, ultimoIdCarregado, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Postagem> lote = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Postagem p = s.getValue(Postagem.class);
                    if (p != null) lote.add(p);
                }

                Collections.reverse(lote);

                if (ultimoIdCarregado != null && !lote.isEmpty()) {
                    if (lote.get(0).getIdPostagem().equals(ultimoIdCarregado)) {
                        lote.remove(0);
                    }
                }

                if (lote.size() < BATCH_SIZE) {
                    isLastPage = true;
                }

                if (!lote.isEmpty()) {
                    ultimoIdCarregado = lote.get(lote.size() - 1).getIdPostagem();
                }

                for (Postagem p : lote) {
                    if (p.getTextoPostagem() != null && p.getTextoPostagem().toLowerCase().contains(currentQuery)) {
                        encontradas.add(p);
                    }
                }

                if (encontradas.size() >= 10 || isLastPage) {
                    isLoading = false;
                    // Avisa a tela que deve limpar o feed antigo e colocar os resultados novos no topo!
                    listener.onSuccess(encontradas, isRefresh);
                } else {
                    buscarLoteRecursivo(encontradas, isRefresh, listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading = false;
                listener.onError(error.getMessage());
            }
        });
    }

    public boolean isLoading() { return isLoading; }
}