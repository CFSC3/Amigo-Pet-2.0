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
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.AdicionarHubActivity;
import com.ifam.amigopetfixo.adapter.PesquisaUsuarioAdapter;
import com.ifam.amigopetfixo.adapter.PostagemAdapter;
import com.ifam.amigopetfixo.helper.VisitanteHelper;
import com.ifam.amigopetfixo.repository.UsuarioRepository;
import com.ifam.amigopetfixo.usecase.FeedUseCase;
import com.ifam.amigopetfixo.usecase.PesquisaPostagemUseCase;
import com.ifam.amigopetfixo.usecase.PesquisaUsuarioUseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostagensFragment extends Fragment {

    // Views
    private RecyclerView recyclerPostagem, recyclerPesquisa;
    private TextView textFeedVazio;
    private FloatingActionButton fabAdicionarPost;
    private SwipeRefreshLayout swipeRefreshFeed;
    private MaterialToolbar toolbar;

    // Adapters e Listas
    private PostagemAdapter adapterFeed;
    private List<Postagem> listaPostagens = new ArrayList<>();

    // UseCases (Regras de Negócio e Paginação)
    private FeedUseCase feedUseCase;
    private PesquisaUsuarioUseCase pesquisaUsuarioUseCase;
    private PesquisaPostagemUseCase pesquisaPostagemUseCase;
    private UsuarioRepository usuarioRepository;

    // Estado Atual
    private boolean isModoPesquisaPost = false;
    private String queryPostAtual = "";

    public PostagensFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_postagens, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializarComponentes(view);
        configurarToolbarEMenus();
        configurarRecyclerViews();
        configurarScrollInfinito();

        // Carrega o primeiro lote de 25 feeds ao abrir
        carregarFeedPrincipal(true);

        swipeRefreshFeed.setOnRefreshListener(() -> {
            if (isModoPesquisaPost) {
                pesquisarPostsNoFeed(queryPostAtual, true);
            } else {
                carregarFeedPrincipal(true);
            }
        });

        fabAdicionarPost.setOnClickListener(v -> {
            if (!VisitanteHelper.verificarAcesso(getContext())) return;
            Intent intent = new Intent(getActivity(), AdicionarHubActivity.class);
            intent.putExtra("TIPO_CRIACAO", "POSTAGEM");
            startActivity(intent);
        });
    }

    private void inicializarComponentes(View view) {
        recyclerPostagem = view.findViewById(R.id.recyclerPostagem);
        recyclerPesquisa = view.findViewById(R.id.recyclerPesquisa);
        textFeedVazio = view.findViewById(R.id.textFeedVazio);
        fabAdicionarPost = view.findViewById(R.id.fabAdicionarPost);
        swipeRefreshFeed = view.findViewById(R.id.swipeRefreshFeed);
        toolbar = view.findViewById(R.id.materialToolbar);

        feedUseCase = new FeedUseCase();
        pesquisaUsuarioUseCase = new PesquisaUsuarioUseCase();
        pesquisaPostagemUseCase = new PesquisaPostagemUseCase();
        usuarioRepository = new UsuarioRepository();
    }

    private void configurarRecyclerViews() {
        recyclerPesquisa.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPesquisa.setHasFixedSize(true);

        recyclerPostagem.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPostagem.setHasFixedSize(true);
        recyclerPostagem.setItemViewCacheSize(20);
        adapterFeed = new PostagemAdapter(listaPostagens, false);
        recyclerPostagem.setAdapter(adapterFeed);
    }

    private void configurarToolbarEMenus() {
        toolbar.inflateMenu(R.menu.menu_postagens);

        for (int i = 0; i < toolbar.getMenu().size(); i++) {
            android.graphics.drawable.Drawable icon = toolbar.getMenu().getItem(i).getIcon();
            if (icon != null) {
                icon.mutate();
                icon.setTint(getResources().getColor(R.color.branco, getContext().getTheme()));
            }
        }

        MenuItem itemPesquisaUsuario = toolbar.getMenu().findItem(R.id.action_pesquisar_usuario);
        MenuItem itemPesquisaPost = toolbar.getMenu().findItem(R.id.action_pesquisar_post);
        MenuItem itemChat = toolbar.getMenu().findItem(R.id.action_chat);

        androidx.appcompat.widget.SearchView searchViewUsuario = (androidx.appcompat.widget.SearchView) itemPesquisaUsuario.getActionView();
        androidx.appcompat.widget.SearchView searchViewPost = (androidx.appcompat.widget.SearchView) itemPesquisaPost.getActionView();

        // LÓGICA DA LUPA 1 (USUÁRIOS)
        searchViewUsuario.setQueryHint("Nome ou @username");
        searchViewUsuario.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.trim().isEmpty()) {
                    restaurarViewNormal();
                    return true;
                }

                final String pesquisaAtual = newText;

                pesquisaUsuarioUseCase.executarBusca(pesquisaAtual, new UsuarioRepository.OnBuscaUsuariosListener() {
                    @Override
                    public void onSuccess(List < Map < String , String > > usuarios) {

                        String textoNaLupa = searchViewUsuario.getQuery().toString();

                        if (textoNaLupa.trim().isEmpty() || !textoNaLupa.equals(pesquisaAtual)) {
                            return;
                        }

                        if (usuarios == null || usuarios.isEmpty()) {
                            restaurarViewNormal();
                        } else {
                            swipeRefreshFeed.setVisibility(View.GONE);
                            recyclerPostagem.setVisibility(View.GONE);
                            fabAdicionarPost.setVisibility(View.GONE);
                            recyclerPesquisa.setVisibility(View.VISIBLE);
                            recyclerPesquisa.setAdapter(new PesquisaUsuarioAdapter(usuarios, getContext()));
                        }
                    }

                    @Override
                    public void onError(String erro) {}
                });
                return true;
            }
        });

        itemPesquisaUsuario.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                if (!VisitanteHelper.verificarAcesso(getContext())) {
                    return false; // Bloqueia a expansão do campo de busca
                }

                if (itemPesquisaPost.isActionViewExpanded()) {
                    itemPesquisaPost.collapseActionView();
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                restaurarViewNormal();
                return true;
            }
        });

        // LÓGICA DA LUPA 2 (FILTRAR POSTS)
        searchViewPost.setQueryHint("Filtrar feed");
        searchViewPost.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.isEmpty()) {
                    restaurarViewNormal();
                    isModoPesquisaPost = true;
                    queryPostAtual = query;

                    if (swipeRefreshFeed != null) swipeRefreshFeed.setRefreshing(true);
                    pesquisarPostsNoFeed(query, true);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        itemPesquisaPost.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                if (!VisitanteHelper.verificarAcesso(getContext())) {
                    return false; // Bloqueia a expansão do campo de busca
                }

                if (itemPesquisaUsuario.isActionViewExpanded()) {
                    itemPesquisaUsuario.collapseActionView();
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isModoPesquisaPost = false;
                queryPostAtual = "";
                carregarFeedPrincipal(true);
                return true;
            }
        });

        // ABRIR CHAT
        if (itemChat != null) {
            itemChat.setOnMenuItemClickListener(item -> {
                if (!VisitanteHelper.verificarAcesso(getContext())) return false;
                Intent intent = new Intent(getActivity(), com.ifam.amigopetfixo.activity.ChatHubActivity.class);
                intent.putExtra("Acao", "ListaChats");
                startActivity(intent);
                return true;
            });
        }
    }

    private void restaurarViewNormal() {
        recyclerPesquisa.setVisibility(View.GONE);
        swipeRefreshFeed.setVisibility(View.VISIBLE);
        recyclerPostagem.setVisibility(View.VISIBLE);
        fabAdicionarPost.setVisibility(View.VISIBLE);
    }

    // MÉTODOS DE DADOS E PAGINAÇÃO

    private void configurarScrollInfinito() {
        recyclerPostagem.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // Rolando para baixo
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= listaPostagens.size() - 5) {

                        if (isModoPesquisaPost) {
                            if (!pesquisaPostagemUseCase.isLoading()) pesquisarPostsNoFeed(queryPostAtual, false);
                        } else {
                            if (!feedUseCase.isLoading()) carregarFeedPrincipal(false);
                        }
                    }
                }
            }
        });
    }

    private void carregarFeedPrincipal(boolean isRefresh) {
        feedUseCase.carregarFeed(isRefresh, new FeedUseCase.OnFeedLoadListener() {
            @Override
            public void onSuccess(List<Postagem> postagensCarregadas, boolean foiRefresh) {
                processarResultadoLista(postagensCarregadas, foiRefresh);
            }
            @Override
            public void onError(String erro) {
                pararLoading();
            }
        });
    }

    private void pesquisarPostsNoFeed(String query, boolean isRefresh) {
        pesquisaPostagemUseCase.pesquisarFeed(query, isRefresh, new FeedUseCase.OnFeedLoadListener() {
            @Override
            public void onSuccess(List<Postagem> postagensCarregadas, boolean foiRefresh) {
                processarResultadoLista(postagensCarregadas, foiRefresh);
            }
            @Override
            public void onError(String erro) {
                pararLoading();
                Toast.makeText(getContext(), "Nenhum post encontrado para esta palavra.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processarResultadoLista(List<Postagem> postagensNovas, boolean isRefresh) {
        pararLoading();

        if (isRefresh) {
            listaPostagens.clear();
        }

        int startPos = listaPostagens.size();
        listaPostagens.addAll(postagensNovas);

        if (isRefresh) {
            adapterFeed.notifyDataSetChanged();
        } else {
            adapterFeed.notifyItemRangeInserted(startPos, postagensNovas.size());
        }

        textFeedVazio.setVisibility(listaPostagens.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerPostagem.setVisibility(listaPostagens.isEmpty() ? View.GONE : View.VISIBLE);

        atualizarDadosDeAutores(startPos, postagensNovas.size());
    }

    private void pararLoading() {
        if (swipeRefreshFeed != null && swipeRefreshFeed.isRefreshing()) {
            swipeRefreshFeed.setRefreshing(false);
        }
    }

    private void atualizarDadosDeAutores(int startIndex, int count) {
        for (int i = startIndex; i < startIndex + count; i++) {
            if (i >= listaPostagens.size()) break;

            final Postagem postagemAtual = listaPostagens.get(i);
            final int posicao = i;

            usuarioRepository.buscarPerfilParaCabecalho(postagemAtual.getIdUsuario(), new UsuarioRepository.OnPerfilLoadedListener() {
                @Override
                public void onSuccess(String username, String fotoUrl) {
                    postagemAtual.setUsername(username);
                    postagemAtual.setNomeUsuario(username);
                    adapterFeed.notifyItemChanged(posicao);
                }
                @Override
                public void onError(String erro) {}
            });
        }
    }
}