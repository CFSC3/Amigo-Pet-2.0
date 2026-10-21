package com.ifam.amigopetfixo.helper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ifam.amigopetfixo.Model.bean.Perfil_dono;
import java.util.Random;

public class DatabaseManager {

    private final DatabaseReference referencia;
    private final Random random;

    public DatabaseManager() {
        this.referencia = FirebaseDatabase.getInstance().getReference();
        this.random = new Random();
    }

    public void criarPerfilInicial(String uid, String email) {
        DatabaseReference usuarios = referencia.child("banco-dados").child("usuario").child(uid);
        String imagem = String.valueOf(Long.toHexString(random.nextLong()));

        Perfil_dono pf = new Perfil_dono();
        pf.setNome("");
        pf.setId("");
        pf.setTexto("");
        pf.setEmail(email);

        usuarios.setValue(pf);
        usuarios.child("quatidadePet").setValue("0");
        usuarios.child("imageUsuario").setValue(imagem);
        usuarios.child("postagens").setValue("0");
    }
}