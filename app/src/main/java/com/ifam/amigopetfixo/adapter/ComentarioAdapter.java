package com.ifam.amigopetfixo.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.Model.bean.Comentario;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.UsuarioHubActivity;
import com.ifam.amigopetfixo.activity.VisualizacaoHubActivity;
import com.ifam.amigopetfixo.repository.UsuarioRepository;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.MyViewHolder> {
    private List<Comentario> listaComentarios;
    private UsuarioRepository usuarioRepository;
    private OnComentarioActionListener listener;
    private String currentUserId;

    public interface OnComentarioActionListener {
        void onEditarClick(Comentario comentario);
        void onExcluirClick(Comentario comentario);
    }

    public ComentarioAdapter(List<Comentario> listaComentarios, OnComentarioActionListener listener) {
        this.listaComentarios = listaComentarios;
        this.usuarioRepository = new UsuarioRepository();
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);
        holder.txtTextoComentario.setText(comentario.getTexto());

        usuarioRepository.buscarPerfilParaCabecalho(comentario.getIdUsuario(), new UsuarioRepository.OnPerfilLoadedListener() {
            @Override
            public void onSuccess(String username, String fotoUrl) {
                holder.txtUsernameComentario.setText("@" + username);
                if (fotoUrl != null && !fotoUrl.equals("default")) {
                    Glide.with(holder.itemView.getContext()).load(fotoUrl).placeholder(R.drawable.ic_avatar_pet).into(holder.imgAvatarComentario);
                }
            }
            @Override
            public void onError(String erro) { }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (comentario.getIdUsuario().equals(currentUserId)) {
                intent = new Intent(v.getContext(), com.ifam.amigopetfixo.activity.Tela_principal.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else {
                intent = new Intent(v.getContext(), com.ifam.amigopetfixo.activity.VisualizacaoHubActivity.class);
                intent.putExtra("TIPO_VISUALIZACAO", "PERFIL_PUBLICO");
                intent.putExtra("ID_ITEM", comentario.getIdUsuario());
            }
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (comentario.getIdUsuario().equals(currentUserId)) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenu().add(0, 0, 0, "Editar");
                popupMenu.getMenu().add(0, 1, 1, "Excluir");
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        listener.onEditarClick(comentario);
                    } else if (item.getItemId() == 1) {
                        listener.onExcluirClick(comentario);
                    }
                    return true;
                });
                popupMenu.show();
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() { return listaComentarios.size(); }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarComentario;
        TextView txtUsernameComentario, txtTextoComentario;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarComentario = itemView.findViewById(R.id.imgAvatarComentario);
            txtUsernameComentario = itemView.findViewById(R.id.txtUsernameComentario);
            txtTextoComentario = itemView.findViewById(R.id.txtTextoComentario);
        }
    }
}