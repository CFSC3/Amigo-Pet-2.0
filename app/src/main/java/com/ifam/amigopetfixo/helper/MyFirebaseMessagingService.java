package com.ifam.amigopetfixo.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.Tela_principal;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dados = remoteMessage.getData();

        String titulo = dados.get("titulo");
        String corpo = dados.get("corpo");

        if (titulo == null && remoteMessage.getNotification() != null) {
            titulo = remoteMessage.getNotification().getTitle();
            corpo = remoteMessage.getNotification().getBody();
        }

        if (titulo != null) {
            enviarNotificacaoLocal(titulo, corpo, dados);
        }
    }

    private void enviarNotificacaoLocal(String titulo, String corpo, Map<String, String> dados) {
        Intent intent = new Intent(this, Tela_principal.class);

        String acao = dados.get("Acao");
        if (acao == null) {
            acao = dados.get("acao");
        }

        if ("AbrirConversa".equals(acao)) {
            intent.putExtra("Acao", acao);
            intent.putExtra("uidDestino", dados.get("uidDestino"));
        } else if ("AbrirComentario".equals(acao)) {
            intent.putExtra("Acao", acao);
            intent.putExtra("idPostagem", dados.get("idPostagem"));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String canalId = "canal_amigo_pet";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, canalId)
                .setSmallIcon(R.drawable.logo_png)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(canalId, "Notificações Amigo Pet", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(canal);
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}