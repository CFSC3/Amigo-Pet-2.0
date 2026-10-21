package com.ifam.amigopetfixo.helper;

import com.google.firebase.auth.FirebaseUser;

public interface AuthCallback {
    void onSucesso(FirebaseUser user);
    void onFalha(String mensagemErro);
}