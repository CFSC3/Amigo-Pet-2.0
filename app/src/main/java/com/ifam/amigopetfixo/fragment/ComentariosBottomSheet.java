package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.Model.bean.Comentario;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.ComentarioAdapter;
import com.ifam.amigopetfixo.repository.ComentarioRepository;

import java.util.ArrayList;
import java.util.List;

public class ComentariosBottomSheet extends BottomSheetDialogFragment {

    private String idPostagemSelecionada;
    private ComentarioRepository repository;

    private RecyclerView recyclerComentarios;
    private ComentarioAdapter adapter;
    private List<Comentario> listaComentarios = new ArrayList<>();
    private String idComentarioEditando = null;

    public ComentariosBottomSheet(String idPostagem) {
        this.idPostagemSelecionada = idPostagem;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottomsheet_comentarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ComentarioRepository(idPostagemSelecionada);

        configurarRecyclerView(view);
        configurarBotaoEnviar(view);
        carregarDados();
    }

    private void configurarRecyclerView(View view) {
        recyclerComentarios = view.findViewById(R.id.recyclerComentarios);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(getContext()));

        EditText editComentario = view.findViewById(R.id.editComentario);

        adapter = new ComentarioAdapter(listaComentarios, new ComentarioAdapter.OnComentarioActionListener() {
            @Override
            public void onEditarClick(Comentario comentario) {
                idComentarioEditando = comentario.getIdComentario();
                editComentario.setText(comentario.getTexto());
                editComentario.requestFocus();
            }

            @Override
            public void onExcluirClick(Comentario comentario) {
                repository.excluirComentario(comentario.getIdComentario(), new ComentarioRepository.OnComentarioAcaoListener() {
                    @Override
                    public void onSuccess() {}
                    @Override
                    public void onError(String erro) {}
                });
            }
        });
        recyclerComentarios.setAdapter(adapter);
    }

    private void configurarBotaoEnviar(View view) {
        EditText editComentario = view.findViewById(R.id.editComentario);
        ImageView btnEnviarComentario = view.findViewById(R.id.btnEnviarComentario);

        btnEnviarComentario.setOnClickListener(v -> {
            String texto = editComentario.getText().toString().trim();
            if (!texto.isEmpty()) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (idComentarioEditando == null) {
                    repository.salvarComentario(currentUserId, texto, new ComentarioRepository.OnComentarioAcaoListener() {
                        @Override
                        public void onSuccess() { editComentario.setText(""); }
                        @Override
                        public void onError(String erro) {}
                    });
                } else {
                    repository.atualizarComentario(idComentarioEditando, texto, new ComentarioRepository.OnComentarioAcaoListener() {
                        @Override
                        public void onSuccess() {
                            editComentario.setText("");
                            idComentarioEditando = null;
                        }
                        @Override
                        public void onError(String erro) {}
                    });
                }
            }
        });
    }

    private void carregarDados() {
        repository.carregarComentarios(new ComentarioRepository.OnComentariosCarregadosListener() {
            @Override
            public void onDataLoaded(List<Comentario> comentarios) {
                listaComentarios.clear();
                listaComentarios.addAll(comentarios);
                adapter.notifyDataSetChanged();

                if (listaComentarios.size() > 0) {
                    recyclerComentarios.smoothScrollToPosition(listaComentarios.size() - 1);
                }
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(getContext(), "Erro ao carregar: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }
}