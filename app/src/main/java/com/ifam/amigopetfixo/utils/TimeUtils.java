package com.ifam.amigopetfixo.utils;

import android.text.format.DateFormat;
import java.util.Calendar;

public class TimeUtils {
    public static String formatarTimestamp(long tempoMillis) {
        Calendar agora = Calendar.getInstance();
        Calendar tempoMensagem = Calendar.getInstance();
        tempoMensagem.setTimeInMillis(tempoMillis);

        if (agora.get(Calendar.YEAR) == tempoMensagem.get(Calendar.YEAR) &&
                agora.get(Calendar.DAY_OF_YEAR) == tempoMensagem.get(Calendar.DAY_OF_YEAR)) {
            // É hoje: retorna apenas a hora
            return DateFormat.format("HH:mm", tempoMensagem).toString();
        } else if (agora.get(Calendar.YEAR) == tempoMensagem.get(Calendar.YEAR) &&
                agora.get(Calendar.DAY_OF_YEAR) - tempoMensagem.get(Calendar.DAY_OF_YEAR) == 1) {
            // Foi ontem
            return "Ontem";
        } else {
            // Dias anteriores: retorna a data completa
            return DateFormat.format("dd/MM/yyyy", tempoMensagem).toString();
        }
    }
}