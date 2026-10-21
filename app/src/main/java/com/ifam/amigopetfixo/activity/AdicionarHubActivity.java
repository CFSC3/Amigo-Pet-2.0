package com.ifam.amigopetfixo.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.fragment.AdicionarDoacaoFragment;
import com.ifam.amigopetfixo.fragment.AdicionarPostagemFragment;
import com.ifam.amigopetfixo.fragment.AdicionarEventoFragment;

public class AdicionarHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_hub);

        String tipoCriacao = getIntent().getStringExtra("TIPO_CRIACAO");
        int opcaoMidia = getIntent().getIntExtra("OPCAO_MIDIA", 0);

        if (tipoCriacao == null) {
            Toast.makeText(this, "Erro ao abrir tela de criação.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        switch (tipoCriacao) {
            case "POSTAGEM":
                carregarFragment(new AdicionarPostagemFragment());
                break;
            case "EVENTO":
                carregarFragment(new AdicionarEventoFragment());
                break;
            case "DOACAO":
                AdicionarDoacaoFragment fragmentDoacao = new AdicionarDoacaoFragment();

                Bundle bundle = new Bundle();
                bundle.putInt("opcao", opcaoMidia);
                fragmentDoacao.setArguments(bundle);

                carregarFragment(fragmentDoacao);
                break;
            default:
                finish();
        }
    }

    private void carregarFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_adicionar_hub, fragment);
        transaction.commit();
    }
}