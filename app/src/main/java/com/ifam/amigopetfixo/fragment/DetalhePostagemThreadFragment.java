package com.ifam.amigopetfixo.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Comentario;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.ComentarioAdapter;
import com.ifam.amigopetfixo.repository.ComentarioRepository;
import com.ifam.amigopetfixo.repository.PostagemRepository;

import java.util.ArrayList;
import java.util.List;

public class DetalhePostagemThreadFragment extends Fragment {

    private String idPostagemSelecionada;
    private PostagemRepository postagemRepository;
    private ComentarioRepository comentarioRepository;

    private RecyclerView recyclerComentariosThread;
    private ComentarioAdapter comentarioAdapter;
    private List<Comentario> listaComentarios = new ArrayList<>();

    // Variável para controlar edição de comentários
    private String idComentarioEditando = null;

    // Views do Card de Postagem
    private ShapeableImageView imgAvatarAutor;
    private TextView txtNomeAutor, txtUsernameAutor, txtTextoPostagem, txtNumeroCurtidas, txtNumeroComentarios;
    private ImageView imgMidiaPostagem, iconePlayVideo, iconeCoracao;
    private VideoView videoMidiaPostagem;

    // Ações da Thread
    private ImageButton btnEditarPostagem, btnExcluirPostagem;
    private EditText editComentarioThread;
    private ImageView btnEnviarComentarioThread;
    private View btnCurtir, btnComentar;

    public DetalhePostagemThreadFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalhe_postagem_thread, container, false);

        if (getArguments() != null) {
            idPostagemSelecionada = getArguments().getString("ID_ITEM");
        }

        postagemRepository = new PostagemRepository();
        if (idPostagemSelecionada != null) {
            comentarioRepository = new ComentarioRepository(idPostagemSelecionada);
        }

        inicializarViews(view);
        configurarRecyclerView();
        configurarBotaoEnviar();

        if (idPostagemSelecionada != null) {
            carregarDetalhesPostagem();
            carregarComentarios();
            configurarBotoesAcao();
        } else {
            Toast.makeText(getContext(), "Erro ao carregar a postagem.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void inicializarViews(View view) {
        View cardView = view.findViewById(R.id.cardPostagemThread);
        imgAvatarAutor = cardView.findViewById(R.id.imgAvatarAutor);
        txtNomeAutor = cardView.findViewById(R.id.txtNomeAutor);
        txtUsernameAutor = cardView.findViewById(R.id.txtUsernameAutor);
        txtTextoPostagem = cardView.findViewById(R.id.txtTextoPostagem);
        imgMidiaPostagem = cardView.findViewById(R.id.imgMidiaPostagem);
        iconePlayVideo = cardView.findViewById(R.id.iconePlayVideo);
        txtNumeroCurtidas = cardView.findViewById(R.id.txtNumeroCurtidas);
        txtNumeroComentarios = cardView.findViewById(R.id.txtNumeroComentarios);
        iconeCoracao = cardView.findViewById(R.id.iconeCoracao);
        videoMidiaPostagem = cardView.findViewById(R.id.videoMidiaPostagem);

        btnEditarPostagem = view.findViewById(R.id.btnEditarPostagem);
        btnExcluirPostagem = view.findViewById(R.id.btnExcluirPostagem);
        editComentarioThread = view.findViewById(R.id.editComentarioThread);
        btnEnviarComentarioThread = view.findViewById(R.id.btnEnviarComentarioThread);
        recyclerComentariosThread = view.findViewById(R.id.recyclerComentariosThread);
        btnCurtir = cardView.findViewById(R.id.btnCurtir);
        btnComentar = cardView.findViewById(R.id.btnComentar);
    }

    private void configurarRecyclerView() {
        recyclerComentariosThread.setLayoutManager(new LinearLayoutManager(getContext()));
        comentarioAdapter = new ComentarioAdapter(listaComentarios, new ComentarioAdapter.OnComentarioActionListener() {
            @Override
            public void onEditarClick(Comentario comentario) {
                idComentarioEditando = comentario.getIdComentario();
                editComentarioThread.setText(comentario.getTexto());
                editComentarioThread.requestFocus();

                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(editComentarioThread, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }

            @Override
            public void onExcluirClick(Comentario comentario) {
                comentarioRepository.excluirComentario(comentario.getIdComentario(), new ComentarioRepository.OnComentarioAcaoListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Comentário excluído", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String erro) {
                        Toast.makeText(getContext(), "Erro ao excluir", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerComentariosThread.setAdapter(comentarioAdapter);
    }

    private void carregarDetalhesPostagem() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("Postagens").child(idPostagemSelecionada);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Postagem postagem = snapshot.getValue(Postagem.class);
                    if (postagem != null) {
                        txtTextoPostagem.setText(postagem.getTextoPostagem());
                        Glide.with(DetalhePostagemThreadFragment.this).load(postagem.getUrlMidia()).into(imgMidiaPostagem);
                        carregarAutorPostagem(postagem.getIdUsuario());
                        configurarCurtidasEComentariosContador();

                        if ("video".equals(postagem.getTipoMidia())) {
                            iconePlayVideo.setVisibility(View.VISIBLE);
                            imgMidiaPostagem.setOnClickListener(v -> {
                                imgMidiaPostagem.setVisibility(View.GONE);
                                iconePlayVideo.setVisibility(View.GONE);
                                videoMidiaPostagem.setVisibility(View.VISIBLE);
                                android.widget.MediaController mediaController = new android.widget.MediaController(getContext());
                                mediaController.setAnchorView(videoMidiaPostagem);
                                videoMidiaPostagem.setMediaController(mediaController);
                                videoMidiaPostagem.setVideoURI(android.net.Uri.parse(postagem.getUrlMidia()));
                                videoMidiaPostagem.start();
                            });
                        } else {
                            iconePlayVideo.setVisibility(View.GONE);
                            imgMidiaPostagem.setOnClickListener(null);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void carregarAutorPostagem(String idUsuario) {
        DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("usuario").child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    txtNomeAutor.setText(snapshot.child("nome").getValue(String.class));
                    txtUsernameAutor.setText("@" + snapshot.child("id").getValue(String.class));
                    String foto = snapshot.child("imageUsuario").getValue(String.class);
                    if (foto != null && !foto.equals("default")) {
                        Glide.with(DetalhePostagemThreadFragment.this).load(foto).placeholder(R.drawable.padrao).into(imgAvatarAutor);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void configurarCurtidasEComentariosContador() {

        DatabaseReference curtidasRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("Curtidas").child(idPostagemSelecionada);
        curtidasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded()) {
                    txtNumeroCurtidas.setVisibility(View.GONE);
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    if (snapshot.hasChild(currentUserId)) {
                        iconeCoracao.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    } else {
                        iconeCoracao.setColorFilter(getResources().getColor(R.color.colorTextPrimary));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference contagemComentariosRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("Comentarios").child(idPostagemSelecionada);
        contagemComentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded()) {
                    txtNumeroComentarios.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void carregarComentarios() {
        comentarioRepository.carregarComentarios(new ComentarioRepository.OnComentariosCarregadosListener() {
            @Override
            public void onDataLoaded(List<Comentario> comentarios) {
                listaComentarios.clear();
                listaComentarios.addAll(comentarios);
                comentarioAdapter.notifyDataSetChanged();

                if (listaComentarios.size() > 0) {
                    recyclerComentariosThread.smoothScrollToPosition(listaComentarios.size() - 1);
                }
            }
            @Override
            public void onError(String erro) {
                if(getContext() != null) Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarBotaoEnviar() {
        btnEnviarComentarioThread.setOnClickListener(v -> {
            String texto = editComentarioThread.getText().toString().trim();
            if (!texto.isEmpty()) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (idComentarioEditando == null) {
                    comentarioRepository.salvarComentario(currentUserId, texto, new ComentarioRepository.OnComentarioAcaoListener() {
                        @Override
                        public void onSuccess() {
                            editComentarioThread.setText("");
                        }
                        @Override
                        public void onError(String erro) {
                            if(getContext() != null) Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    comentarioRepository.atualizarComentario(idComentarioEditando, texto, new ComentarioRepository.OnComentarioAcaoListener() {
                        @Override
                        public void onSuccess() {
                            editComentarioThread.setText("");
                            idComentarioEditando = null;
                            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                            if (imm != null) imm.hideSoftInputFromWindow(editComentarioThread.getWindowToken(), 0);
                        }
                        @Override
                        public void onError(String erro) {
                            if(getContext() != null) Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void configurarBotoesAcao() {
        btnExcluirPostagem.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Excluir Postagem")
                .setMessage("Deseja apagar essa postagem e todos os comentários? Essa ação é irreversível.")
                .setPositiveButton("Excluir", (dialog, which) -> processarExclusao())
                .setNegativeButton("Cancelar", null)
                .show());

        btnEditarPostagem.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(), com.ifam.amigopetfixo.activity.UsuarioHubActivity.class);
            intent.putExtra("Acao", "EditarPostagem");
            intent.putExtra("ID_ITEM", idPostagemSelecionada);
            startActivity(intent);
        });

        btnCurtir.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference curtidasRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("Curtidas").child(idPostagemSelecionada);

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
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        btnComentar.setOnClickListener(v -> {
            editComentarioThread.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editComentarioThread, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void processarExclusao() {
        postagemRepository.excluirPostagemEmCascata(idPostagemSelecionada, new PostagemRepository.OnPostagemCompleteListener() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) getActivity().finish();
            }

            @Override
            public void onFailure(String erro) {
                if(getContext() != null) Toast.makeText(getContext(), "Erro: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().hide();
            }
        } catch (Exception e) {}
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().show();
            }
        } catch (Exception e) {}
    }
}