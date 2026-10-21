package com.ifam.amigopetfixo.repository;

import android.net.Uri;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;

import java.util.ArrayList;
import java.util.List;

public class PetRepository {

    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;
    private final String userId;

    public PetRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.storageReference = FirebaseStorage.getInstance().getReference();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Retorna a referência do nó principal de pets do usuário
    public DatabaseReference getPetsReference() {
        return databaseReference.child("banco-dados").child("usuario").child(userId).child("pets");
    }

    // Gera um novo ID único para o Pet
    public String gerarNovoIdPet() {
        return getPetsReference().push().getKey();
    }

    // Verifica a quantidade atual de pets
    public Task<Integer> verificarQuantidadePets() {
        return getPetsReference().get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                return (int) task.getResult().getChildrenCount();
            }
            return 0;
        });
    }

    public Task<Void> salvarDadosPet(Perfil_pet pet) {
        return getPetsReference().child(pet.getIdPet()).setValue(pet);
    }

    public Task<String> fazerUploadImagemPet(String idPet, Uri uriImagem) {
        StorageReference fotoRef = storageReference.child("imagens").child("pets").child(userId).child(idPet + ".jpg");

        return fotoRef.putFile(uriImagem)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return fotoRef.getDownloadUrl();
                })
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toString();
                    } else {
                        throw new Exception("Falha ao obter URL de download.");
                    }
                });
    }

    // Exclui o pet do Database e a foto do Storage
    public Task<Void> excluirPet(String idPet) {
        Task<Void> taskDatabase = getPetsReference().child(idPet).removeValue();

        StorageReference fotoRef = storageReference.child("imagens").child("pets").child(userId).child(idPet + ".jpg");
        Task<Void> taskStorage = fotoRef.delete().continueWithTask(t -> {
            return Tasks.forResult(null);
        });

        return Tasks.whenAll(taskDatabase, taskStorage);
    }
}