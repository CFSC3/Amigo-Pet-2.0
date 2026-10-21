package com.ifam.amigopetfixo.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Evento;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class EventoRepository {

    private DatabaseReference eventosRef;

    public EventoRepository() {
        eventosRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("Eventos");
    }

    private void verificarExclusaoEventosVencidos(DataSnapshot snapshot, List<Evento> listaAtualizada, OnEventosLoadedListener listener) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dataAtual = new Date();

        for (DataSnapshot ds : snapshot.getChildren()) {
            Evento evento = ds.getValue(Evento.class);
            if (evento != null) {
                try {
                    String dataReferencia = evento.getDataEventoFim() != null && !evento.getDataEventoFim().isEmpty()
                            ? evento.getDataEventoFim()
                            : evento.getDataEvento();

                    Date dataFimEvento = sdf.parse(dataReferencia);

                    if (dataFimEvento != null && dataFimEvento.before(sdf.parse(sdf.format(dataAtual)))) {
                        eventosRef.child(evento.getIdEvento()).removeValue();

                        if(evento.getUrlImagem() != null) {
                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(evento.getUrlImagem());
                            imageRef.delete();
                        }
                    } else {
                        listaAtualizada.add(0, evento);
                    }
                } catch (ParseException e) {
                    listaAtualizada.add(0, evento);
                }
            }
        }
        listener.onSuccess(listaAtualizada);
    }

    public void carregarEventos(OnEventosLoadedListener listener) {
        eventosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Evento> lista = new ArrayList<>();
                if (snapshot.exists()) {
                    verificarExclusaoEventosVencidos(snapshot, lista, listener);
                } else {
                    listener.onSuccess(lista);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public void carregarEventosPorLocalizacao(String cidadeFiltro, OnEventosLoadedListener listener) {
        com.google.firebase.database.Query query = eventosRef.orderByChild("cidadeFiltro").equalTo(cidadeFiltro);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Evento> lista = new ArrayList<>();
                if (snapshot.exists()) {
                    verificarExclusaoEventosVencidos(snapshot, lista, listener);
                } else {
                    listener.onSuccess(lista);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public void criarEvento(Evento novoEvento, Uri imagemUri, String userId, OnEventoAcaoListener listener) {
        String idEvento = eventosRef.push().getKey();
        novoEvento.setIdEvento(idEvento);
        novoEvento.setIdUsuario(userId);

        String dataAtual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        novoEvento.setDataPostagem(dataAtual);

        if (imagemUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("ImagensEventos").child(idEvento + ".jpg");
            storageRef.putFile(imagemUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            novoEvento.setUrlImagem(uri.toString());
                            salvarDadosDoEvento(novoEvento, listener);
                        });
                    })
                    .addOnFailureListener(e -> listener.onError("Erro no upload: " + e.getMessage()));
        } else {
            salvarDadosDoEvento(novoEvento, listener);
        }
    }

    private void salvarDadosDoEvento(Evento evento, OnEventoAcaoListener listener) {
        if (evento.getIdEvento() != null) {
            eventosRef.child(evento.getIdEvento()).setValue(evento)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError("Erro ao salvar: " + e.getMessage()));
        }
    }

    public void buscarEventoPorId(String idEvento, OnEventoUnicoLoadedListener listener) {
        eventosRef.child(idEvento).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Evento evento = snapshot.getValue(Evento.class);
                    listener.onSuccess(evento);
                } else {
                    listener.onError("Evento não encontrado.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public interface OnEventoUnicoLoadedListener {
        void onSuccess(Evento evento);
        void onError(String erro);
    }

    public interface OnEventoAcaoListener {
        void onSuccess();
        void onError(String erro);
    }

    public interface OnEventosLoadedListener {
        void onSuccess(List<Evento> eventos);
        void onError(String erro);
    }
}