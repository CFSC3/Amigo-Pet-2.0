package com.ifam.amigopetfixo.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.activity.Cadastro;

public class VisitanteHelper {

    public static boolean isVisitante() {
        return FirebaseAuth.getInstance().getCurrentUser() == null;
    }

    public static boolean verificarAcesso(Context context) {
        if (isVisitante()) {
            new AlertDialog.Builder(context)
                    .setTitle("Oops! 🐾")
                    .setMessage("Essa área é exclusiva para a nossa comunidade. Crie sua conta rapidinho e venha interagir com a gente!")
                    .setPositiveButton("Fazer Cadastro", (dialog, which) -> {
                        context.startActivity(new Intent(context, Cadastro.class));
                    })
                    .setNegativeButton("Agora não", null)
                    .show();
            return false;
        }
        return true;
    }
}