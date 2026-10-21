package com.ifam.amigopetfixo.repository;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UsuarioRepository {

    private DatabaseReference rootRef;
    private StorageReference storageRef;
    private String currentUserId;
    private FirebaseAuth auth;

    public UsuarioRepository() {
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }
    }

    public void verificarIdDisponivel(String idDesejado, UsernameCheckCallback callback) {
        rootRef.child("banco-dados").child("Usernames").child(idDesejado)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(!snapshot.exists());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void salvarPerfilCompleto(String nome, String idDesejado, Uri imagemUri, OnUpdateCompleteListener listener) {
        if (currentUserId == null) {
            listener.onFailure("Usuário não está logado.");
            return;
        }

        if (imagemUri != null) {
            StorageReference fotoRef = storageRef.child("imagens").child("perfil").child(currentUserId + ".jpeg");
            fotoRef.putFile(imagemUri).addOnSuccessListener(taskSnapshot -> {
                fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    atualizarBancoDeDados(nome, idDesejado, uri.toString(), listener);
                });
            }).addOnFailureListener(e -> listener.onFailure("Erro ao enviar foto: " + e.getMessage()));
        } else {
            atualizarBancoDeDados(nome, idDesejado, null, listener);
        }
    }

    private void atualizarBancoDeDados(String nome, String idDesejado, String urlFoto, OnUpdateCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("banco-dados/usuario/" + currentUserId + "/nome", nome);
        updates.put("banco-dados/usuario/" + currentUserId + "/id", idDesejado);

        if (urlFoto != null) {
            updates.put("banco-dados/usuario/" + currentUserId + "/imageUsuario", urlFoto);
        }
        updates.put("banco-dados/Usernames/" + idDesejado, currentUserId);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void buscarPerfilParaCabecalho(String uid, OnPerfilLoadedListener listener) {
        rootRef.child("banco-dados").child("usuario").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String username = snapshot.child("id").getValue(String.class);
                            String fotoUrl = snapshot.child("imageUsuario").getValue(String.class);
                            listener.onSuccess(username, fotoUrl);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

    public boolean logadoViaProvedorExterno() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getProviderId().equals("google.com") || userInfo.getProviderId().equals("facebook.com")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void trocarSenhaFirebase(String novaSenha, OnUpdateCompleteListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.updatePassword(novaSenha)
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
        } else {
            listener.onFailure("Usuário não encontrado.");
        }
    }

    public void sairDoApp() {
        auth.signOut();
    }

    public void excluirContaCompleta(String usernameParaDeletar, OnUpdateCompleteListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onFailure("Usuário não autenticado.");
            return;
        }
        String uid = user.getUid();
        DatabaseReference bdRef = rootRef.child("banco-dados");

        bdRef.child("Postagens").orderByChild("idUsuario").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) ds.getRef().removeValue();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        bdRef.child("Eventos").orderByChild("idUsuario").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) ds.getRef().removeValue();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        bdRef.child("adocoesDoacoes").orderByChild("usuarioID").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) ds.getRef().removeValue();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        storageRef.child("imagens").child("perfil").child(uid + ".jpeg").delete();
        storageRef.child("imagens").child("capa").child(uid + ".jpg").delete();

        bdRef.child("usuario").child(uid).removeValue().addOnCompleteListener(task -> {
            if (usernameParaDeletar != null && !usernameParaDeletar.isEmpty()) {
                bdRef.child("Usernames").child(usernameParaDeletar).removeValue();
            }
            user.delete()
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(e -> listener.onFailure("Faça login novamente para confirmar a exclusão."));
        });
    }

    // --- INTERFACES ---
    public interface OnPerfilLoadedListener {
        void onSuccess(String username, String fotoUrl);
        void onError(String erro);
    }

    public interface OnUpdateCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}