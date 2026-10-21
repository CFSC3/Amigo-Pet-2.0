package com.ifam.amigopetfixo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.AdicionarHubActivity;
import com.ifam.amigopetfixo.adapter.PostagemAdapter;
import com.ifam.amigopetfixo.helper.VisitanteHelper;
import com.ifam.amigopetfixo.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class PostagensFragment extends Fragment {

    private RecyclerView recyclerPostagem;
    private TextView textFeedVazio;
    private FloatingActionButton fabAdicionarPost;
    private SwipeRefreshLayout swipeRefreshFeed;

    private PostagemAdapter adapter;
    private List<Postagem> postagens = new ArrayList<>();

    private DatabaseReference postagensRef = FirebaseDatabase.getInstance().getReference()
            .child("banco-dados").child("Postagens");

    private UsuarioRepository usuarioRepository;

    public PostagensFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_postagens, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerPostagem = view.findViewById(R.id.recyclerPostagem);
        textFeedVazio = view.findViewById(R.id.textFeedVazio);
        fabAdicionarPost = view.findViewById(R.id.fabAdicionarPost);
        swipeRefreshFeed = view.findViewById(R.id.swipeRefreshFeed);

        usuarioRepository = new UsuarioRepository();

        recyclerPostagem.setHasFixedSize(true);
        recyclerPostagem.setItemViewCacheSize(20);
        recyclerPostagem.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PostagemAdapter(postagens);
        recyclerPostagem.setAdapter(adapter);

        fabAdicionarPost.setOnClickListener(v -> {
            if (!VisitanteHelper.verificarAcesso(getContext())) return;

            Intent intent = new Intent(getActivity(), AdicionarHubActivity.class);
            intent.putExtra("TIPO_CRIACAO", "POSTAGEM");
            startActivity(intent);
        });

        carregarPostagens();

        swipeRefreshFeed.setOnRefreshListener(() -> {
            carregarPostagens();
        });
    }

    private void carregarPostagens() {
        postagensRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postagens.clear();

                if (dataSnapshot.exists()) {
                    textFeedVazio.setVisibility(View.GONE);
                    recyclerPostagem.setVisibility(View.VISIBLE);

                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        Postagem p = s.getValue(Postagem.class);
                        if (p != null) {
                            postagens.add(0, p);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    atualizarDadosDeAutores();

                } else {
                    textFeedVazio.setVisibility(View.VISIBLE);
                    recyclerPostagem.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }

                if (swipeRefreshFeed != null && swipeRefreshFeed.isRefreshing()) {
                    swipeRefreshFeed.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (swipeRefreshFeed != null && swipeRefreshFeed.isRefreshing()) {
                    swipeRefreshFeed.setRefreshing(false);
                }
                if (getContext() != null) {
                }
            }
        });
    }

    private void atualizarDadosDeAutores() {
        for (int i = 0; i < postagens.size(); i++) {
            final Postagem postagemAtual = postagens.get(i);
            final int posicao = i;

            usuarioRepository.buscarPerfilParaCabecalho(postagemAtual.getIdUsuario(), new UsuarioRepository.OnPerfilLoadedListener() {
                @Override
                public void onSuccess(String username, String fotoUrl) {

                    postagemAtual.setUsername(username);
                    postagemAtual.setNomeUsuario(username);

                    adapter.notifyItemChanged(posicao);
                }

                @Override
                public void onError(String erro) {

                }
            });
        }
    }
}