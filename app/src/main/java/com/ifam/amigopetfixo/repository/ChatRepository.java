package com.ifam.amigopetfixo.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    // Agora recebe os parâmetros de citação (podem ser null se não for resposta)
    public void enviarMensagemTexto(String idSalaChat, String idRemetente, String idDestino, String texto, Mensagem citacao) {
        DatabaseReference mensagemRef = rootRef.child("Mensagens").child(idSalaChat).push();
        String idMensagem = mensagemRef.getKey();
        long tempoAtual = System.currentTimeMillis();

        Mensagem mensagem = new Mensagem();
        mensagem.setIdMensagem(idMensagem);
        mensagem.setIdRemetente(idRemetente);
        mensagem.setTexto(texto);
        mensagem.setTipoMensagem("texto");
        mensagem.setDataHora(tempoAtual);

        // Se houver uma citação, anexa ao nó
        if (citacao != null) {
            mensagem.setIdMensagemCitada(citacao.getIdMensagem());
            mensagem.setTextoMensagemCitada(citacao.getTexto() != null ? citacao.getTexto() : "Mídia");
            mensagem.setAutorMensagemCitada(citacao.getIdRemetente().equals(idRemetente) ? "Você" : "Outro");
        }

        mensagemRef.setValue(mensagem).addOnSuccessListener(aVoid -> {
            atualizarGavetaRecentes(idRemetente, idDestino, texto, tempoAtual, idSalaChat, true);
            atualizarGavetaRecentes(idDestino, idRemetente, texto, tempoAtual, idSalaChat, false);
        });
    }

    public void ouvirMensagens(String idSalaChat, String idUsuarioAtual, MensagemCallback callback) {
        salaRef = rootRef.child("Mensagens").child(idSalaChat);

        salaRef.child("exclusoes").child(idUsuarioAtual).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotExclusao) {
                long tempoCorteExclusao = snapshotExclusao.exists() ? snapshotExclusao.getValue(Long.class) : 0L;

                mensagensListener = salaRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.getKey().equals("exclusoes")) return;

                        Mensagem mensagem = snapshot.getValue(Mensagem.class);
                        if (mensagem != null && mensagem.getDataHora() > tempoCorteExclusao) {

                            if (!mensagem.getIdRemetente().equals(idUsuarioAtual)) {
                                rootRef.child("Conversas").child(idUsuarioAtual).child(idSalaChat)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshotConversa) {
                                                if (snapshotConversa.exists()) {
                                                    snapshotConversa.getRef().child("lida").setValue(true);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }

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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onErro(error.getMessage());
            }
        });
    }

    public void enviarMidia(String idSalaChat, String idRemetente, String idDestino, Uri uriMidia, String tipoMidia, Mensagem citacao) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("MidiaChat").child(idSalaChat).child(System.currentTimeMillis() + "_" + tipoMidia);

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

                if (citacao != null) {
                    mensagem.setIdMensagemCitada(citacao.getIdMensagem());
                    mensagem.setTextoMensagemCitada(citacao.getTexto() != null ? citacao.getTexto() : "Mídia");
                    mensagem.setAutorMensagemCitada(citacao.getIdRemetente().equals(idRemetente) ? "Você" : "Outro");
                }

                String textoResumo = tipoMidia.equals("imagem") ? "📷 Foto" : "🎥 Vídeo";
                mensagemRef.setValue(mensagem).addOnSuccessListener(aVoid -> {
                    atualizarGavetaRecentes(idRemetente, idDestino, textoResumo, tempoAtual, idSalaChat, true);
                    atualizarGavetaRecentes(idDestino, idRemetente, textoResumo, tempoAtual, idSalaChat, false);
                });
            });
        });
    }

    public void excluirChatLogicamente(String idSalaChat, String idUsuarioAtual, String idDestino, Runnable onSuccess) {
        // Remove da lista de recentes
        rootRef.child("Conversas").child(idUsuarioAtual).child(idSalaChat).removeValue();
        // Salva a data de corte dentro das mensagens
        rootRef.child("Mensagens").child(idSalaChat).child("exclusoes").child(idUsuarioAtual)
                .setValue(System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> onSuccess.run());
    }

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