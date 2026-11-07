package com.ifam.amigopetfixo.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.Model.bean.Comentario;
import com.ifam.amigopetfixo.R;
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

        Linkify.addLinks(holder.txtTextoComentario, Linkify.WEB_URLS);

        String idAutor = comentario.getIdUsuario();

        if (idAutor != null && !idAutor.isEmpty()) {
            usuarioRepository.buscarPerfilParaCabecalho(idAutor, new UsuarioRepository.OnPerfilLoadedListener() {
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
        } else {
            holder.txtUsernameComentario.setText("@Desconhecido");
        }

        // Clique para ir ao perfil
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

        View.OnLongClickListener longClickListener = v -> {
            abrirMenuBottomSheet(v.getContext(), comentario);
            return true;
        };

        holder.itemView.setOnLongClickListener(longClickListener);
        holder.txtTextoComentario.setOnLongClickListener(longClickListener);
    }

    private void abrirMenuBottomSheet(Context context, Comentario comentario) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        // Criando o container principal
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 48, 32, 48);

        layout.setBackgroundResource(R.drawable.fundo_arredondado_bottomsheet);

        boolean isDonoDoComentario = comentario.getIdUsuario().equals(currentUserId);

        // COPIAR
        TextView btnCopiar = criarBotaoMenu(context, "Copiar");
        btnCopiar.setOnClickListener(v -> {
            copiarTextoParaAreaDeTransferencia(context, comentario.getTexto());
            bottomSheetDialog.dismiss();
        });
        layout.addView(btnCopiar);

        if (isDonoDoComentario) {
            // EDITAR
            TextView btnEditar = criarBotaoMenu(context, "Editar");
            btnEditar.setOnClickListener(v -> {
                listener.onEditarClick(comentario);
                bottomSheetDialog.dismiss();
            });
            layout.addView(btnEditar);

            // EXCLUIR
            TextView btnExcluir = criarBotaoMenu(context, "Excluir");

            btnExcluir.setOnClickListener(v -> {
                listener.onExcluirClick(comentario);
                bottomSheetDialog.dismiss();
            });
            layout.addView(btnExcluir);
        }

        bottomSheetDialog.setContentView(layout);
        bottomSheetDialog.show();
    }

    private TextView criarBotaoMenu(Context context, String texto) {
        TextView textView = new TextView(context);
        textView.setText(texto);

        textView.setTextSize(16f);
        textView.setTypeface(null, android.graphics.Typeface.NORMAL);
        textView.setTextColor(context.getResources().getColor(R.color.colorTextPrimary));

        textView.setGravity(android.view.Gravity.CENTER_VERTICAL);
        textView.setPadding(48, 36, 48, 36);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(params);

        android.util.TypedValue outValue = new android.util.TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        textView.setBackgroundResource(outValue.resourceId);

        return textView;
    }

    private void copiarTextoParaAreaDeTransferencia(Context context, String texto) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Comentário Amigo Pet", texto);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Comentário copiado!", Toast.LENGTH_SHORT).show();
        }
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