package com.ifam.amigopetfixo.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Adocoes;

import java.util.ArrayList;
import java.util.List;

public class DoacaoRepository {

    private DatabaseReference rootRef;

    public DoacaoRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("adocoesDoacoes");
    }

    public void carregarDoacoes(OnDoacoesCarregadasListener listener) {
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Adocoes> lista = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Adocoes adocao = ds.getValue(Adocoes.class);
                    if (adocao != null) {
                        lista.add(0, adocao);
                    }
                }
                listener.onDataLoaded(lista);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public void excluirDoacaoEmCascata(String idDoacao, String idUsuario, String nomeImagem, OnDoacaoCompleteListener listener) {
        // Apaga nó principal
        rootRef.child(idDoacao).removeValue();

        // Apaga da árvore do usuário
        FirebaseDatabase.getInstance().getReference().child("banco-dados").child("usuario")
                .child(idUsuario).child("adocoesDoacoes").child(idDoacao).removeValue();

        // Apaga a imagem associada no Storage
        if (nomeImagem != null && !nomeImagem.equals("Não informado") && !nomeImagem.isEmpty()) {
            com.google.firebase.storage.StorageReference fotoRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference()
                    .child("BancoImagens").child("DoacoesAdocoes").child(nomeImagem + ".png");
            fotoRef.delete().addOnCompleteListener(task -> listener.onSuccess());
        } else {
            listener.onSuccess();
        }
    }

    public void atualizarInformacoesDoacao(String idDoacao, String idUsuario, java.util.Map<String, Object> atualizacoes, OnDoacaoCompleteListener listener) {
        rootRef.child(idDoacao).updateChildren(atualizacoes)
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance().getReference().child("banco-dados").child("usuario")
                            .child(idUsuario).child("adocoesDoacoes").child(idDoacao).updateChildren(atualizacoes)
                            .addOnSuccessListener(aVoid2 -> listener.onSuccess())
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnDoacaoCompleteListener {
        void onSuccess();
        void onFailure(String erro);
    }

    public interface OnDoacoesCarregadasListener {
        void onDataLoaded(List<Adocoes> doacoes);
        void onError(String erro);
    }
}