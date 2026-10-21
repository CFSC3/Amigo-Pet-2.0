package com.ifam.amigopetfixo.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.fragment.VisualizacaoPetPublicoFragment;
import com.ifam.amigopetfixo.fragment.VisualizacaoUsuarioFragment;
import com.ifam.amigopetfixo.fragment.VisualizarEventoFragment;
import com.ifam.amigopetfixo.fragment.VisualizarDoacaoFragment;
import com.ifam.amigopetfixo.fragment.VisualizarPostagemThreadFragment;

public class VisualizacaoHubActivity extends AppCompatActivity {

    private Toolbar toolbar;

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

        String tipoVisualizacao = getIntent().getStringExtra("TIPO_VISUALIZACAO");
        String idItem = getIntent().getStringExtra("ID_ITEM");
        String idDono = getIntent().getStringExtra("ID_DONO");

        if (tipoVisualizacao != null && idItem != null) {
            carregarFragment(tipoVisualizacao, idItem, idDono);
        } else {
            Toast.makeText(this, "Erro ao carregar os detalhes.", Toast.LENGTH_SHORT).show();
            finish();
        }
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