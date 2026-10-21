package com.ifam.amigopetfixo.helper;

import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginManager {
    private final FirebaseAuth auth;

    public EmailLoginManager() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void entrar(String email, String senha, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                callback.onSucesso(auth.getCurrentUser());
            } else {
                callback.onFalha("E-mail ou senha incorreto.");
            }
        });
    }
}