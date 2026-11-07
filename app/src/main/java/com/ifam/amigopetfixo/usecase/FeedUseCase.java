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

public class FeedUseCase {
    private final PostagemRepository repository;
    private String ultimoIdCarregado = null;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private static final int LIMITE_POR_PAGINA = 25;

    public FeedUseCase() {
        this.repository = new PostagemRepository();
    }

    public void carregarFeed(boolean isRefresh, OnFeedLoadListener listener) {
        if (isLoading || (isLastPage && !isRefresh)) return;

        isLoading = true;
        if (isRefresh) {
            ultimoIdCarregado = null;
            isLastPage = false;
        }

        // Se houver último ID, pedimos +1 porque o Firebase inclui o próprio item no endAt()
        int limiteBusca = (ultimoIdCarregado == null) ? LIMITE_POR_PAGINA : LIMITE_POR_PAGINA + 1;

        repository.buscarPostagensPaginadas(limiteBusca, ultimoIdCarregado, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Postagem> novasPostagens = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    Postagem p = s.getValue(Postagem.class);
                    if (p != null) novasPostagens.add(p);
                }

                // Inverte para exibir os mais recentes no topo
                Collections.reverse(novasPostagens);

                if (ultimoIdCarregado != null && !novasPostagens.isEmpty()) {
                    // Remove o item duplicado
                    if (novasPostagens.get(0).getIdPostagem().equals(ultimoIdCarregado)) {
                        novasPostagens.remove(0);
                    }
                }

                if (novasPostagens.size() < LIMITE_POR_PAGINA) {
                    isLastPage = true; // Chegamos ao fim absoluto do banco
                }

                if (!novasPostagens.isEmpty()) {
                    ultimoIdCarregado = novasPostagens.get(novasPostagens.size() - 1).getIdPostagem(); // O mais antigo desta leva
                }

                isLoading = false;
                listener.onSuccess(novasPostagens, isRefresh);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading = false;
                listener.onError(error.getMessage());
            }
        });
    }

    public boolean isLoading() { return isLoading; }

    public interface OnFeedLoadListener {
        void onSuccess(List<Postagem> postagens, boolean isRefresh);
        void onError(String erro);
    }
}