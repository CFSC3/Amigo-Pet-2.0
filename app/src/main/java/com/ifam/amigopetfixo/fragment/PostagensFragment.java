package com.ifam.amigopetfixo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.AdicionarHubActivity;
import com.ifam.amigopetfixo.adapter.PesquisaUsuarioAdapter;
import com.ifam.amigopetfixo.adapter.PostagemAdapter;
import com.ifam.amigopetfixo.helper.VisitanteHelper;
import com.ifam.amigopetfixo.repository.UsuarioRepository;
import com.ifam.amigopetfixo.usecase.PesquisaUsuarioUseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private androidx.appcompat.widget.SearchView searchView;
    private RecyclerView recyclerPesquisa;
    private PesquisaUsuarioUseCase pesquisaUseCase;

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

        recyclerPostagem = view.findViewById(R.id.recyclerPostagem);
        recyclerPesquisa = view.findViewById(R.id.recyclerPesquisa);
        MaterialToolbar toolbar = view.findViewById(R.id.materialToolbar);

        pesquisaUseCase = new PesquisaUsuarioUseCase();

        // Configurando a lista de pesquisa
        recyclerPesquisa.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPesquisa.setHasFixedSize(true);

        // Injetando o menu de pesquisa na Toolbar
        toolbar.inflateMenu(R.menu.menu_postagens);
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_pesquisar);
        searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        // Aplicar a cor dinâmica (colorTextPrimary) aos ícones do menu
        android.graphics.drawable.Drawable searchDrawable = toolbar.getMenu().findItem(R.id.action_pesquisar).getIcon();
        if (searchDrawable != null) {
            searchDrawable.mutate();
            searchDrawable.setTint(getResources().getColor(R.color.colorTextPrimary, getContext().getTheme()));
        }

        android.graphics.drawable.Drawable chatDrawable = toolbar.getMenu().findItem(R.id.action_chat).getIcon();
        if (chatDrawable != null) {
            chatDrawable.mutate();
            chatDrawable.setTint(getResources().getColor(R.color.colorTextPrimary, getContext().getTheme()));
        }

        // Escutando a digitação em tempo real
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pesquisaUseCase.executarBusca(newText, new UsuarioRepository.OnBuscaUsuariosListener() {
                    @Override
                    public void onSuccess(List<Map<String, String>> usuariosEncontrados) {
                        if (usuariosEncontrados == null) {
                            // Se o texto for vazio/curto, esconde a pesquisa e volta o feed
                            recyclerPesquisa.setVisibility(View.GONE);
                            swipeRefreshFeed.setVisibility(View.VISIBLE);
                            fabAdicionarPost.setVisibility(View.VISIBLE);
                        } else {
                            // achou usuários, esconde o feed e mostra os resultados
                            swipeRefreshFeed.setVisibility(View.GONE);
                            fabAdicionarPost.setVisibility(View.GONE);
                            recyclerPesquisa.setVisibility(View.VISIBLE);

                            recyclerPesquisa.setAdapter(new PesquisaUsuarioAdapter(usuariosEncontrados, getContext()));
                        }
                    }

                    @Override
                    public void onError(String erro) {
                        Toast.makeText(getContext(), "Erro na busca: " + erro, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
        });

        // Quando o usuário fechar a lupa, garantir que o feed volte ao normal
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                recyclerPesquisa.setVisibility(View.GONE);
                swipeRefreshFeed.setVisibility(View.VISIBLE);
                fabAdicionarPost.setVisibility(View.VISIBLE);
                return true;
            }
        });

        // Botão do Chat na Toolbar
        MenuItem chatItem = toolbar.getMenu().findItem(R.id.action_chat);
        if (chatItem != null) {
            chatItem.setOnMenuItemClickListener(item -> {
                if (!VisitanteHelper.verificarAcesso(getContext())) return false;

                Intent intent = new Intent(getActivity(), com.ifam.amigopetfixo.activity.ChatHubActivity.class);
                intent.putExtra("Acao", "ListaChats"); // Abre a caixa de entrada
                startActivity(intent);
                return true;
            });
        }

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