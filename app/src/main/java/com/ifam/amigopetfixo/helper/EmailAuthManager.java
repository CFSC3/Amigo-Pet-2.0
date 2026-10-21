package com.ifam.amigopetfixo.helper;

import com.google.firebase.auth.FirebaseAuth;

public class EmailAuthManager {

    private final FirebaseAuth auth;

    public EmailAuthManager() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void cadastrar(String email, String senha, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                callback.onSucesso(auth.getCurrentUser());
            } else {
                String excecao = "";
                try {
                    throw task.getException();
                } catch (com.google.firebase.auth.FirebaseAuthWeakPasswordException e) {
                    excecao = "A senha precisa ter pelo menos 8 caracteres.";
                } catch (com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                    excecao = "O formato do e-mail é inválido.";
                } catch (com.google.firebase.auth.FirebaseAuthUserCollisionException e) {
                    excecao = "Este e-mail já está cadastrado! Faça login.";
                } catch (Exception e) {
                    excecao = "Erro ao cadastrar: " + e.getMessage();
                }
                callback.onFalha(excecao);
            }
        });
    }
}