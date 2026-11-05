package com.ifam.amigopetfixo.repository;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FavoritoRepository {

    private final DatabaseReference rootRef;
    private final String currentUserId;

    public FavoritoRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = null;
        }
    }

    public void verificarStatusFavorito(String uidVisitado, OnStatusFavoritoListener listener) {
        if (currentUserId == null) return;

        rootRef.child("banco-dados").child("Favoritos").child(currentUserId).child(uidVisitado)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listener.onStatusChanged(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    public void alternarFavorito(String uidVisitado, boolean atualmenteFavoritado) {
        if (currentUserId == null) return;

        DatabaseReference favoritoRef = rootRef.child("banco-dados").child("Favoritos")
                .child(currentUserId).child(uidVisitado);

        if (atualmenteFavoritado) {
            favoritoRef.removeValue(); // Desfavorita
        } else {
            favoritoRef.setValue(true); // Favorita
        }
    }

    public interface OnStatusFavoritoListener {
        void onStatusChanged(boolean isFavorito);
        void onError(String erro);
    }
}