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

    // Identificador único para empilhar mensagens
    private static final String GRUPO_MENSAGENS = "com.ifam.amigopetfixo.MENSAGENS";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dados = remoteMessage.getData();

        String titulo = dados.get("titulo");
        String corpo = dados.get("corpo");
        String acao = dados.get("Acao") != null ? dados.get("Acao") : dados.get("acao");
        String uidDestino = dados.get("uidDestino");
        String idPostagem = dados.get("idPostagem");

        // O usuário já está na tela
        if ("AbrirConversa".equals(acao) && uidDestino != null && uidDestino.equals(AppStatus.idChatAberto)) {
            return; // Aborta a notificação. O usuário já está lendo no chat!
        }

        if ("AbrirComentario".equals(acao) && idPostagem != null && idPostagem.equals(AppStatus.idPostagemAberta)) {
            return; // Aborta a notificação. O usuário já está lendo os comentários!
        }

        if (titulo == null && remoteMessage.getNotification() != null) {
            titulo = remoteMessage.getNotification().getTitle();
            corpo = remoteMessage.getNotification().getBody();
        }

        if (titulo != null) {
            enviarNotificacaoLocal(titulo, corpo, acao, uidDestino, idPostagem);
        }
    }

    private void enviarNotificacaoLocal(String titulo, String corpo, String acao, String uidDestino, String idPostagem) {
        Intent intent = new Intent(this, Tela_principal.class);

        // Cada notificação recebe um ID fixo com base no remetente ou postagem.
        // Assim, 5 mensagens do "João" atualizam 1 único balão, em vez de criar 5 balões novos.
        int idNotificacao = 0;

        if ("AbrirConversa".equals(acao)) {
            intent.putExtra("Acao", acao);
            intent.putExtra("uidDestino", uidDestino);
            idNotificacao = uidDestino != null ? uidDestino.hashCode() : (int) System.currentTimeMillis();
        } else if ("AbrirComentario".equals(acao)) {
            intent.putExtra("Acao", acao);
            intent.putExtra("idPostagem", idPostagem);
            idNotificacao = idPostagem != null ? idPostagem.hashCode() : (int) System.currentTimeMillis();
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, idNotificacao, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String canalId = "canal_amigo_pet";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(canalId, "Notificações Amigo Pet", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(canal);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, canalId)
                .setSmallIcon(R.drawable.logo_png)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setAutoCancel(true)
                .setGroup(GRUPO_MENSAGENS)
                .setContentIntent(pendingIntent);

        manager.notify(idNotificacao, builder.build());

        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(this, canalId)
                .setContentTitle("Amigo Pet")
                .setSmallIcon(R.drawable.logo_png)
                .setStyle(new NotificationCompat.InboxStyle().setSummaryText("Novas mensagens"))
                .setGroup(GRUPO_MENSAGENS)
                .setGroupSummary(true)
                .setAutoCancel(true);

        manager.notify(999, summaryBuilder.build());
    }
}