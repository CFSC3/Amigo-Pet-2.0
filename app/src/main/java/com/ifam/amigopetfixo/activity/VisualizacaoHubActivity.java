package com.ifam.amigopetfixo.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.fragment.VisualizacaoPetPublicoFragment;
import com.ifam.amigopetfixo.fragment.VisualizacaoUsuarioFragment;
import com.ifam.amigopetfixo.fragment.VisualizarEventoFragment;
import com.ifam.amigopetfixo.fragment.VisualizarDoacaoFragment;
import com.ifam.amigopetfixo.fragment.VisualizarPostagemThreadFragment;
import com.ifam.amigopetfixo.repository.FavoritoRepository;
import com.ifam.amigopetfixo.usecase.FavoritoUseCase;

public class VisualizacaoHubActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String tipoVisualizacao;
    private String idItem;

    private FavoritoUseCase favoritoUseCase;
    private boolean isFavoritadoAtual = false;
    private MenuItem menuFavoritar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizacao_hub);

        toolbar = findViewById(R.id.toolbarVisualizacao);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tipoVisualizacao = getIntent().getStringExtra("TIPO_VISUALIZACAO");
        idItem = getIntent().getStringExtra("ID_ITEM");
        String idDono = getIntent().getStringExtra("ID_DONO");

        if (tipoVisualizacao != null && idItem != null) {
            carregarFragment(tipoVisualizacao, idItem, idDono);
        } else {
            Toast.makeText(this, "Erro ao carregar os detalhes.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visualizacao, menu);
        menuFavoritar = menu.findItem(R.id.action_favoritar);

        if ("PERFIL_PUBLICO".equals(tipoVisualizacao) && idItem != null) {
            menuFavoritar.setVisible(true);
            configurarFavoritos();
        } else {
            menuFavoritar.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_favoritar) {
            if (favoritoUseCase != null) {
                favoritoUseCase.aoClicarFavoritar(idItem, isFavoritadoAtual);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configurarFavoritos() {
        favoritoUseCase = new FavoritoUseCase();

        favoritoUseCase.escutarStatus(idItem, new FavoritoRepository.OnStatusFavoritoListener() {
            @Override
            public void onStatusChanged(boolean isFavorito) {
                isFavoritadoAtual = isFavorito;
                if (menuFavoritar != null) {
                    menuFavoritar.setIcon(isFavorito ? R.drawable.ic_favority : R.drawable.ic_favority_border);
                }
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(VisualizacaoHubActivity.this, "Erro ao carregar favorito.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarFragment(String tipo, String idItem, String idDono) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        bundle.putString("ID_ITEM", idItem);

        if (idDono != null) {
            bundle.putString("ID_DONO", idDono);
        }

        switch (tipo) {
            case "EVENTO":
                toolbar.setTitle("Detalhes do Evento");
                fragment = new VisualizarEventoFragment();
                break;
            case "DOACAO":
                toolbar.setTitle("Detalhes da Doação");
                fragment = new VisualizarDoacaoFragment();
                break;
            case "PERFIL_PUBLICO":
                toolbar.setTitle("");
                fragment = new VisualizacaoUsuarioFragment();
                break;
            case "PET_PUBLICO":
                toolbar.setTitle("Pet");
                fragment = new VisualizacaoPetPublicoFragment();
                break;
            case "THREAD_POSTAGEM":
                toolbar.setTitle("");
                fragment = new VisualizarPostagemThreadFragment();
                break;
        }

        if (fragment != null) {
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerVisualizacao, fragment)
                    .commit();
        }
    }
}