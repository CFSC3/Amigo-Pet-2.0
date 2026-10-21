package com.ifam.amigopetfixo.helper;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordRecoveryManager {
    private final FirebaseAuth auth;

    public PasswordRecoveryManager() {
        this.auth = FirebaseAuth.getInstance();
    }

    public void recuperarSenha(String email, RecoveryCallback callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onFalha("Por favor, digite seu e-mail no campo acima para recuperar a senha.");
            return;
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSucesso("E-mail de recuperação enviado! Verifique sua caixa de entrada.");
            } else {
                callback.onFalha("Erro ao enviar e-mail. Verifique se o endereço está correto.");
            }
        });
    }

    // Uma interface simples só para devolver a mensagem de sucesso ou erro do e-mail
    public interface RecoveryCallback {
        void onSucesso(String mensagem);
        void onFalha(String erro);
    }
}