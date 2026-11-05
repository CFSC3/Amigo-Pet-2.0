package com.ifam.amigopetfixo.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ifam.amigopetfixo.Model.bean.Mensagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.utils.TimeUtils;
import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_ENVIADA = 1;
    private static final int TIPO_RECEBIDA = 2;

    private List<Mensagem> listaMensagens;
    private Context context;
    private String idUsuarioAtual;

    public MensagemAdapter(List<Mensagem> listaMensagens, Context context, String idUsuarioAtual) {
        this.listaMensagens = listaMensagens;
        this.context = context;
        this.idUsuarioAtual = idUsuarioAtual;
    }

    @Override
    public int getItemViewType(int position) {
        Mensagem mensagem = listaMensagens.get(position);
        if (mensagem.getIdRemetente() != null && mensagem.getIdRemetente().equals(idUsuarioAtual)) {
            return TIPO_ENVIADA;
        }
        return TIPO_RECEBIDA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TIPO_ENVIADA) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_mensagem_enviada, parent, false);
            return new EnviadaViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_mensagem_recebida, parent, false);
            return new RecebidaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensagem mensagem = listaMensagens.get(position);
        String horaFormatada = TimeUtils.formatarTimestamp(mensagem.getDataHora());

        if (holder.getItemViewType() == TIPO_ENVIADA) {
            EnviadaViewHolder enviadaHolder = (EnviadaViewHolder) holder;
            enviadaHolder.textHora.setText(horaFormatada);
            configurarBalao(mensagem, enviadaHolder.textMensagem, enviadaHolder.layoutMidia, enviadaHolder.imageMidia, enviadaHolder.iconePlay);
        } else {
            RecebidaViewHolder recebidaHolder = (RecebidaViewHolder) holder;
            recebidaHolder.textHora.setText(horaFormatada);
            configurarBalao(mensagem, recebidaHolder.textMensagem, recebidaHolder.layoutMidia, recebidaHolder.imageMidia, recebidaHolder.iconePlay);
        }
    }

    private void configurarBalao(Mensagem mensagem, TextView textMensagem, FrameLayout layoutMidia, ImageView imageMidia, ImageView iconePlay) {
        if (mensagem.getUrlMidia() != null && !mensagem.getUrlMidia().isEmpty()) {
            layoutMidia.setVisibility(View.VISIBLE);
            textMensagem.setVisibility(mensagem.getTexto() != null && !mensagem.getTexto().isEmpty() ? View.VISIBLE : View.GONE);
            imageMidia.setVisibility(View.VISIBLE);

            Glide.with(context).load(mensagem.getUrlMidia()).into(imageMidia);

            if ("video".equals(mensagem.getTipoMensagem())) {
                iconePlay.setVisibility(View.VISIBLE);

                // Dispara o player nativo do celular
                layoutMidia.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(mensagem.getUrlMidia()), "video/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        context.startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        Toast.makeText(context, "Nenhum aplicativo encontrado para reproduzir o vídeo.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                iconePlay.setVisibility(View.GONE);

                // Dispara a galeria ou visualizador de fotos nativo do celular
                layoutMidia.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(mensagem.getUrlMidia()), "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        context.startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        Toast.makeText(context, "Nenhum aplicativo encontrado para visualizar a imagem.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            layoutMidia.setVisibility(View.GONE);
            textMensagem.setVisibility(View.VISIBLE);
        }
        textMensagem.setText(mensagem.getTexto());
    }

    @Override
    public int getItemCount() {
        return listaMensagens.size();
    }

    static class EnviadaViewHolder extends RecyclerView.ViewHolder {
        TextView textMensagem, textHora;
        FrameLayout layoutMidia;
        ImageView imageMidia, iconePlay;

        EnviadaViewHolder(View itemView) {
            super(itemView);
            textMensagem = itemView.findViewById(R.id.textMensagemEnviada);
            textHora = itemView.findViewById(R.id.textHoraEnviada);
            layoutMidia = itemView.findViewById(R.id.layoutMidiaEnviada);
            imageMidia = itemView.findViewById(R.id.imageMidiaEnviada);
            iconePlay = itemView.findViewById(R.id.iconePlayEnviado);
        }
    }

    static class RecebidaViewHolder extends RecyclerView.ViewHolder {
        TextView textMensagem, textHora;
        FrameLayout layoutMidia;
        ImageView imageMidia, iconePlay;

        RecebidaViewHolder(View itemView) {
            super(itemView);
            textMensagem = itemView.findViewById(R.id.textMensagemRecebida);
            textHora = itemView.findViewById(R.id.textHoraRecebida);
            layoutMidia = itemView.findViewById(R.id.layoutMidiaRecebida);
            imageMidia = itemView.findViewById(R.id.imageMidiaRecebida);
            iconePlay = itemView.findViewById(R.id.iconePlayRecebido);
        }
    }
}