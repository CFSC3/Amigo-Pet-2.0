package com.ifam.amigopetfixo.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ifam.amigopetfixo.Model.bean.Mensagem;
import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class ChatRepository {

    private final DatabaseReference rootRef;
    private ChildEventListener mensagensListener;
    private DatabaseReference salaRef;

    public ChatRepository() {
        this.rootRef = FirebaseDatabase.getInstance().getReference().child("banco-dados");
    }

    public interface MensagemCallback {
        void onMensagemRecebida(Mensagem mensagem);
        void onErro(String erro);
    }

    public void enviarMensagemTexto(String idSalaChat, String idRemetente, String idDestino, String texto) {
        DatabaseReference mensagemRef = rootRef.child("Mensagens").child(idSalaChat).push();
        String idMensagem = mensagemRef.getKey();
        long tempoAtual = System.currentTimeMillis();

        Mensagem mensagem = new Mensagem();
        mensagem.setIdMensagem(idMensagem);
        mensagem.setIdRemetente(idRemetente);
        mensagem.setTexto(texto);
        mensagem.setTipoMensagem("texto");
        mensagem.setDataHora(tempoAtual);

        mensagemRef.setValue(mensagem).addOnSuccessListener(aVoid -> {
            atualizarGavetaRecentes(idRemetente, idDestino, texto, tempoAtual, idSalaChat, true);
            atualizarGavetaRecentes(idDestino, idRemetente, texto, tempoAtual, idSalaChat, false);
        });
    }

    public void ouvirMensagens(String idSalaChat, MensagemCallback callback) {
        salaRef = rootRef.child("Mensagens").child(idSalaChat);

        mensagensListener = salaRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                if (mensagem != null) {
                    callback.onMensagemRecebida(mensagem);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onErro(error.getMessage());
            }
        });
    }

    public void enviarMidia(String idSalaChat, String idRemetente, String idDestino, Uri uriMidia, String tipoMidia) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("MidiaChat")
                .child(idSalaChat)
                .child(System.currentTimeMillis() + "_" + tipoMidia);

        storageRef.putFile(uriMidia).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                DatabaseReference mensagemRef = rootRef.child("Mensagens").child(idSalaChat).push();
                long tempoAtual = System.currentTimeMillis();

                Mensagem mensagem = new Mensagem();
                mensagem.setIdMensagem(mensagemRef.getKey());
                mensagem.setIdRemetente(idRemetente);
                mensagem.setUrlMidia(uri.toString());
                mensagem.setTipoMensagem(tipoMidia);
                mensagem.setDataHora(tempoAtual);

                String textoResumo = tipoMidia.equals("imagem") ? "📷 Foto" : "🎥 Vídeo";

                mensagemRef.setValue(mensagem).addOnSuccessListener(aVoid -> {
                    // Atualizado para incluir o estado "lida"
                    atualizarGavetaRecentes(idRemetente, idDestino, textoResumo, tempoAtual, idSalaChat, true);
                    atualizarGavetaRecentes(idDestino, idRemetente, textoResumo, tempoAtual, idSalaChat, false);
                });
            });
        });
    }

    public void excluirChatLogicamente(String idSalaChat, String idUsuarioAtual, String idDestino, Runnable onSuccess) {
        rootRef.child("Conversas").child(idUsuarioAtual).child(idSalaChat).removeValue();

        rootRef.child("Mensagens").child(idSalaChat)
                .child("exclusoes")
                .child(idUsuarioAtual)
                .setValue(System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> onSuccess.run());
    }

    // Mantida apenas a versão correta e atualizada do método
    private void atualizarGavetaRecentes(String donoDoNo, String idDestinatario, String ultimaMensagem, long timestamp, String idSalaChat, boolean lida) {
        Map<String, Object> mapChat = new HashMap<>();
        mapChat.put("idSalaChat", idSalaChat);
        mapChat.put("idDestinatario", idDestinatario);
        mapChat.put("ultimaMensagem", ultimaMensagem);
        mapChat.put("timestamp", timestamp);
        mapChat.put("lida", lida);

        rootRef.child("Conversas").child(donoDoNo).child(idSalaChat).updateChildren(mapChat);
    }

    public void removerListener() {
        if (salaRef != null && mensagensListener != null) {
            salaRef.removeEventListener(mensagensListener);
        }
    }
}