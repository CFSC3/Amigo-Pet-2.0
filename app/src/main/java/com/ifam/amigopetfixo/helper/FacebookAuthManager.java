package com.ifam.amigopetfixo.helper;

import android.app.Activity;
import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class FacebookAuthManager {

    private final FirebaseAuth auth;
    private final CallbackManager callbackManager;

    public FacebookAuthManager() {
        this.auth = FirebaseAuth.getInstance();
        this.callbackManager = CallbackManager.Factory.create();
    }

    public void iniciarLogin(Activity activity, AuthCallback authCallback) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"));

        // Ouve a resposta da janelinha do Facebook
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Sucesso no Facebook! Agora manda o token para o Firebase
                autenticarNoFirebase(loginResult.getAccessToken(), authCallback);
            }

            @Override
            public void onCancel() {
                authCallback.onFalha("Login com Facebook cancelado pelo usuário.");
            }

            @Override
            public void onError(FacebookException error) {
                authCallback.onFalha("Erro no Facebook: " + error.getMessage());
            }
        });
    }

    // Repassa o resultado para o CallbackManager processar
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // Pega o passe livre do Facebook e cadastra no Firebase
    private void autenticarNoFirebase(AccessToken token, AuthCallback callback) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                callback.onSucesso(auth.getCurrentUser());
            } else {
                callback.onFalha("Falha na autenticação do Firebase via Facebook.");
            }
        });
    }
}