package com.ifam.amigopetfixo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.ChatRecente;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.ChatHubActivity;
import com.ifam.amigopetfixo.utils.TimeUtils;
import java.util.List;

public class ChatRecenteAdapter extends RecyclerView.Adapter<ChatRecenteAdapter.ViewHolder> {

    private List<ChatRecente> listaChats;
    private Context context;
    private OnChatLongClickListener longClickListener;

    public ChatRecenteAdapter(List<ChatRecente> listaChats, Context context, OnChatLongClickListener longClickListener) {
        this.listaChats = listaChats;
        this.context = context;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_recente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRecente chat = listaChats.get(position);
        holder.textUltimaMensagem.setText(chat.getUltimaMensagem());
        holder.textHoraChat.setText(TimeUtils.formatarTimestamp(chat.getTimestamp()));

        // Verifica se a mensagem não foi lida
        if (!chat.isLida()) {
            holder.textUltimaMensagem.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.textUltimaMensagem.setTextColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
            // Aplica a borda ao redor da foto com um leve espaçamento
            holder.imageFotoChat.setPadding(8, 8, 8, 8);
            holder.imageFotoChat.setBackgroundResource(R.drawable.bg_avatar_nao_lido);
        } else {
            holder.textUltimaMensagem.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.textUltimaMensagem.setTextColor(context.getResources().getColor(R.color.colorTextPrimary, context.getTheme()));
            // Remove a borda
            holder.imageFotoChat.setPadding(0, 0, 0, 0);
            holder.imageFotoChat.setBackgroundResource(android.R.color.transparent);
        }

        String idDestinatario = chat.getIdDestinatario();

        if (idDestinatario != null && !idDestinatario.isEmpty()) {
            FirebaseDatabase.getInstance().getReference().child("banco-dados").child("usuario")
                    .child(idDestinatario)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String nome = snapshot.child("nome").getValue(String.class);
                                String foto = snapshot.child("imageUsuario").getValue(String.class);

                                holder.textNomeChat.setText(nome != null ? nome : "Usuário");

                                if (foto != null && !foto.isEmpty()) {
                                    Glide.with(context).load(foto).circleCrop().into(holder.imageFotoChat);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        } else {
            holder.textNomeChat.setText("Usuário Desconhecido");
        }

        holder.itemView.setOnClickListener(v -> {
            if (idDestinatario != null && !idDestinatario.isEmpty()) {
                Intent intent = new Intent(context, ChatHubActivity.class);
                intent.putExtra("Acao", "AbrirConversa");
                intent.putExtra("uidDestino", idDestinatario);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onChatLongClick(chat);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() { return listaChats.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageFotoChat;
        TextView textHoraChat, textNomeChat, textUltimaMensagem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageFotoChat = itemView.findViewById(R.id.imageFotoChat);
            textNomeChat = itemView.findViewById(R.id.textNomeChat);
            textUltimaMensagem = itemView.findViewById(R.id.textUltimaMensagem);
            textHoraChat = itemView.findViewById(R.id.textHoraChat);
        }
    }

    public interface OnChatLongClickListener {
        void onChatLongClick(ChatRecente chat);
    }
}