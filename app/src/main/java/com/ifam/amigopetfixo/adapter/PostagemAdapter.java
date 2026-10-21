package com.ifam.amigopetfixo.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.VisualizacaoHubActivity;
import com.ifam.amigopetfixo.fragment.ComentariosBottomSheet;

import java.util.List;

public class PostagemAdapter extends RecyclerView.Adapter<PostagemAdapter.MyViewHolder> {

    private List<Postagem> listapostagem;

    public PostagemAdapter(List<Postagem> p) {
        this.listapostagem = p;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.postagem_detalhe, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Postagem postagem = listapostagem.get(position);

        holder.txtTextoPostagem.setText(postagem.getTextoPostagem());

        Glide.with(holder.itemView.getContext())
                .load(postagem.getUrlMidia())
                .centerCrop()
                .into(holder.imgMidiaPostagem);

        DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference()
                .child("banco-dados").child("usuario").child(postagem.getIdUsuario());

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nomeReal = snapshot.child("nome").getValue(String.class);
                    String usernameID = snapshot.child("id").getValue(String.class);
                    String fotoPerfilUrl = snapshot.child("imageUsuario").getValue(String.class);

                    if (nomeReal != null) {
                        holder.txtNomeAutor.setText(nomeReal);
                    }

                    if (usernameID != null) {
                        holder.txtUsernameAutor.setText("@" + usernameID);
                    }

                    if (fotoPerfilUrl != null && !fotoPerfilUrl.isEmpty() && !fotoPerfilUrl.equals("default")) {
                        Glide.with(holder.itemView.getContext())
                                .load(fotoPerfilUrl)
                                .placeholder(R.drawable.padrao)
                                .into(holder.imgAvatarAutor);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        if (holder.videoMidiaPostagem.isPlaying()) {
            holder.videoMidiaPostagem.stopPlayback();
        }
        holder.videoMidiaPostagem.setVisibility(View.GONE);
        holder.imgMidiaPostagem.setVisibility(View.VISIBLE);

        if ("video".equals(postagem.getTipoMidia())) {
            holder.iconePlayVideo.setVisibility(View.VISIBLE);

            holder.imgMidiaPostagem.setOnClickListener(v -> {
                holder.imgMidiaPostagem.setVisibility(View.GONE);
                holder.iconePlayVideo.setVisibility(View.GONE);
                holder.videoMidiaPostagem.setVisibility(View.VISIBLE);

                android.widget.MediaController mediaController = new android.widget.MediaController(v.getContext());
                mediaController.setAnchorView(holder.videoMidiaPostagem);
                holder.videoMidiaPostagem.setMediaController(mediaController);

                holder.videoMidiaPostagem.setVideoURI(android.net.Uri.parse(postagem.getUrlMidia()));
                holder.videoMidiaPostagem.start();
            });
        } else {
            holder.iconePlayVideo.setVisibility(View.GONE);
            holder.imgMidiaPostagem.setOnClickListener(null);
        }

        // LÓGICA DE CURTIDAS (PATINHA)
        // Verifica se é visitante para evitar o NullPointerException
        boolean isVisitante = com.ifam.amigopetfixo.helper.VisitanteHelper.isVisitante();
        String currentUserId = isVisitante ? "" : FirebaseAuth.getInstance().getCurrentUser().getUid();

        // LÓGICA DE CURTIDAS (PATINHA)
        DatabaseReference curtidasRef = FirebaseDatabase.getInstance().getReference()
                .child("banco-dados").child("Curtidas").child(postagem.getIdPostagem());

        curtidasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long totalCurtidas = snapshot.getChildrenCount();

                if (!isVisitante && postagem.getIdUsuario().equals(currentUserId)) {
                    holder.txtNumeroCurtidas.setVisibility(View.GONE);
                } else {
                    holder.txtNumeroCurtidas.setVisibility(View.VISIBLE);
                    holder.txtNumeroCurtidas.setText(String.valueOf(totalCurtidas));
                }

                if (!isVisitante && snapshot.hasChild(currentUserId)) {
                    holder.iconeCoracao.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.iconeCoracao.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.colorTextPrimary));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.btnCurtir.setOnClickListener(v -> {
            if (!com.ifam.amigopetfixo.helper.VisitanteHelper.verificarAcesso(v.getContext())) return;

            curtidasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(currentUserId)) {
                        curtidasRef.child(currentUserId).removeValue();
                    } else {
                        curtidasRef.child(currentUserId).setValue(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        });

        // LÓGICA DE CONTAGEM DE COMENTÁRIOS
        DatabaseReference contagemComentariosRef = FirebaseDatabase.getInstance().getReference()
                .child("banco-dados").child("Comentarios").child(postagem.getIdPostagem());

        contagemComentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.txtNumeroComentarios.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.btnComentar.setOnClickListener(v -> {
            if (!com.ifam.amigopetfixo.helper.VisitanteHelper.verificarAcesso(v.getContext())) return;

            ComentariosBottomSheet bottomSheet = new ComentariosBottomSheet(postagem.getIdPostagem());
            bottomSheet.show(((androidx.fragment.app.FragmentActivity) v.getContext()).getSupportFragmentManager(), "ComentariosBottomSheet");
        });

        View.OnClickListener listenerPerfil = v -> {
            if (!com.ifam.amigopetfixo.helper.VisitanteHelper.verificarAcesso(v.getContext())) return;

            if (postagem.getIdUsuario().equals(currentUserId)) {
                try {
                    BottomNavigationView bottomNav =
                            ((android.app.Activity) v.getContext()).findViewById(R.id.botao_navegacao);

                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_perfil_usuario);
                    }
                } catch (Exception e) {
                    android.widget.Toast.makeText(v.getContext(), "Navegue pelo menu inferior", android.widget.Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(v.getContext(), VisualizacaoHubActivity.class);
                intent.putExtra("TIPO_VISUALIZACAO", "PERFIL_PUBLICO");
                intent.putExtra("ID_ITEM", postagem.getIdUsuario());
                v.getContext().startActivity(intent);
            }
        };

        holder.imgAvatarAutor.setOnClickListener(listenerPerfil);
        holder.txtNomeAutor.setOnClickListener(listenerPerfil);
        holder.txtUsernameAutor.setOnClickListener(listenerPerfil);

    }

    @Override
    public int getItemCount() {
        return listapostagem != null ? listapostagem.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeAutor, txtUsernameAutor, txtTextoPostagem;
        ShapeableImageView imgAvatarAutor;
        ImageView imgMidiaPostagem, iconePlayVideo;
        TextView txtNumeroCurtidas, txtNumeroComentarios;
        ImageView iconeCoracao;
        View btnCurtir;
        View btnComentar;
        private VideoView videoMidiaPostagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeAutor = itemView.findViewById(R.id.txtNomeAutor);
            txtUsernameAutor = itemView.findViewById(R.id.txtUsernameAutor);
            txtTextoPostagem = itemView.findViewById(R.id.txtTextoPostagem);
            imgAvatarAutor = itemView.findViewById(R.id.imgAvatarAutor);
            imgMidiaPostagem = itemView.findViewById(R.id.imgMidiaPostagem);
            iconePlayVideo = itemView.findViewById(R.id.iconePlayVideo);
            txtNumeroCurtidas = itemView.findViewById(R.id.txtNumeroCurtidas);
            txtNumeroComentarios = itemView.findViewById(R.id.txtNumeroComentarios);
            iconeCoracao = itemView.findViewById(R.id.iconeCoracao);
            btnCurtir = itemView.findViewById(R.id.btnCurtir);
            btnComentar = itemView.findViewById(R.id.btnComentar);
            videoMidiaPostagem = itemView.findViewById(R.id.videoMidiaPostagem);
        }
    }
}