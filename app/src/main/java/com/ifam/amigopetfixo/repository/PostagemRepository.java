package com.ifam.amigopetfixo.repository;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.Model.bean.Postagem;

import java.util.HashMap;
import java.util.Map;

public class PostagemRepository {

    private DatabaseReference rootRef;
    private StorageReference storageRef;
    private String currentUserId;

    public PostagemRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference().child("banco-dados");
        storageRef = FirebaseStorage.getInstance().getReference();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    public void fazerUploadECriarPostagem(Postagem novaPostagem, Uri midiaUri, String tipoMidia, OnPostagemCompleteListener listener) {
        if (currentUserId == null) {
            listener.onFailure("Usuário não está logado.");
            return;
        }

        String idPostagem = rootRef.child("Postagens").push().getKey();
        novaPostagem.setIdPostagem(idPostagem);
        novaPostagem.setIdUsuario(currentUserId);

        if (midiaUri != null) {
            String extensao = tipoMidia.equals("video") ? ".mp4" : ".jpg";
            StorageReference arquivoRef = storageRef.child("MidiaPostagens").child(idPostagem + extensao);

            arquivoRef.putFile(midiaUri)
                    .addOnSuccessListener(taskSnapshot -> arquivoRef.getDownloadUrl().addOnSuccessListener(urlDownload -> {
                        novaPostagem.setUrlMidia(urlDownload.toString());
                        novaPostagem.setTipoMidia(tipoMidia);
                        salvarDadosNoBanco(novaPostagem, listener);
                    }))
                    .addOnFailureListener(e -> listener.onFailure("Erro no upload: " + e.getMessage()));
        } else {
            novaPostagem.setTipoMidia("texto");
            salvarDadosNoBanco(novaPostagem, listener);
        }
    }

    private void salvarDadosNoBanco(Postagem postagem, OnPostagemCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();

        updates.put("Postagens/" + postagem.getIdPostagem(), postagem);

        updates.put("usuario/" + currentUserId + "/postagens/" + postagem.getIdPostagem(), true);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("Erro ao salvar dados: " + e.getMessage()));
    }

    public void excluirPostagemEmCascata(String idPostagem, OnPostagemCompleteListener listener) {
        if (currentUserId == null) {
            listener.onFailure("Usuário não logado.");
            return;
        }

        rootRef.child("Postagens").child(idPostagem).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String tipoMidia = snapshot.child("tipoMidia").getValue(String.class);

                    if (tipoMidia != null && tipoMidia.equals("texto")) {
                        apagarDadosDoBancoEmCascata(idPostagem, listener);
                        return;
                    }

                    // Adapta a extensão com base no tipo captado do banco
                    String extensao = (tipoMidia != null && tipoMidia.equals("video")) ? ".mp4" : ".jpg";

                    // Apaga a mídia correta do Storage
                    StorageReference midiaRef = storageRef.child("MidiaPostagens").child(idPostagem + extensao);
                    midiaRef.delete().addOnCompleteListener(task -> {
                        apagarDadosDoBancoEmCascata(idPostagem, listener);
                    });
                } else {
                    listener.onFailure("A postagem já não existe no banco de dados.");
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                listener.onFailure("Erro ao consultar a postagem: " + error.getMessage());
            }
        });
    }

    private void apagarDadosDoBancoEmCascata(String idPostagem, OnPostagemCompleteListener listener) {
        // Apaga nó de Postagens e da árvore do Usuário
        rootRef.child("Postagens").child(idPostagem).removeValue();
        rootRef.child("usuario").child(currentUserId).child("postagens").child(idPostagem).removeValue();

        // Apaga Curtidas e Comentários
        rootRef.child("Curtidas").child(idPostagem).removeValue();
        rootRef.child("Comentarios").child(idPostagem).removeValue();

        listener.onSuccess();
    }

    public void atualizarTextoPostagem(String idPostagem, String novoTexto, OnPostagemCompleteListener listener) {
        if (currentUserId == null) {
            listener.onFailure("Usuário não logado.");
            return;
        }

        rootRef.child("Postagens").child(idPostagem).child("textoPostagem").setValue(novoTexto)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure("Erro ao atualizar: " + e.getMessage()));
    }

    public interface OnPostagemCompleteListener {
        void onSuccess();
        void onFailure(String erro);
    }
}