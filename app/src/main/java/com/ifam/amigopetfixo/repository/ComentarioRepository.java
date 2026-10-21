package com.ifam.amigopetfixo.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Comentario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComentarioRepository {
    private DatabaseReference comentariosRef;

    public ComentarioRepository(String idPostagem) {
        comentariosRef = FirebaseDatabase.getInstance().getReference()
                .child("banco-dados").child("Comentarios").child(idPostagem);
    }

    public void salvarComentario(String idUsuario, String texto, OnComentarioAcaoListener listener) {
        String idComentario = comentariosRef.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idComentario", idComentario);
        map.put("idUsuario", idUsuario);
        map.put("texto", texto);

        if (idComentario != null) {
            comentariosRef.child(idComentario).setValue(map)
                    .addOnSuccessListener(a -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onError(e.getMessage()));
        }
    }

    public void atualizarComentario(String idComentario, String novoTexto, OnComentarioAcaoListener listener) {
        comentariosRef.child(idComentario).child("texto").setValue(novoTexto)
                .addOnSuccessListener(a -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void excluirComentario(String idComentario, OnComentarioAcaoListener listener) {
        comentariosRef.child(idComentario).removeValue()
                .addOnSuccessListener(a -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void carregarComentarios(OnComentariosCarregadosListener listener) {
        comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comentario> lista = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comentario comentario = new Comentario();
                    comentario.setIdComentario(ds.child("idComentario").getValue(String.class));
                    comentario.setIdUsuario(ds.child("idUsuario").getValue(String.class));
                    comentario.setTexto(ds.child("texto").getValue(String.class));
                    if (comentario.getTexto() != null && !comentario.getTexto().isEmpty()) lista.add(comentario);
                }
                listener.onDataLoaded(lista);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public interface OnComentarioAcaoListener {
        void onSuccess();
        void onError(String erro);
    }
    public interface OnComentariosCarregadosListener {
        void onDataLoaded(List<Comentario> comentarios);
        void onError(String erro);
    }
}