package com.ifam.amigopetfixo.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.fragment.AdicionarPetFragment;
import com.ifam.amigopetfixo.fragment.DetalheDoacaoProprietarioFragment;
import com.ifam.amigopetfixo.fragment.DetalhePostagemThreadFragment;
import com.ifam.amigopetfixo.fragment.EditarDoacaoFragment;
import com.ifam.amigopetfixo.fragment.EditarPerfilFragment;
import com.ifam.amigopetfixo.fragment.EditarPetFragment;
import com.ifam.amigopetfixo.fragment.EditarPostagemFragment;
import com.ifam.amigopetfixo.fragment.VisualizarPetFragment;

public class UsuarioHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_hub);

        Toolbar toolbar = findViewById(R.id.toolbarUsuarioHub);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        String acao = getIntent().getStringExtra("Acao");

        if (acao != null) {
            switch (acao) {
                case "EditarPerfil":
                    getSupportActionBar().setTitle("Editar Perfil");
                    carregarFragment(new EditarPerfilFragment());
                    break;
                case "AdicionarPet":
                    getSupportActionBar().setTitle("Adicionar Pet");
                    carregarFragment(new AdicionarPetFragment());
                    break;
                case "VisualizarPet":
                    getSupportActionBar().setTitle("Detalhes do Pet");
                    VisualizarPetFragment vizFragment = new VisualizarPetFragment();
                    Bundle vizArgs = new Bundle();
                    vizArgs.putString("idPetSelecionado", getIntent().getStringExtra("idPetSelecionado"));
                    vizFragment.setArguments(vizArgs);
                    carregarFragment(vizFragment);
                    break;
                case "EditarPet":
                    getSupportActionBar().setTitle("Editar Pet");
                    EditarPetFragment editFragment = new EditarPetFragment();
                    Bundle editArgs = new Bundle();
                    editArgs.putString("idPetSelecionado", getIntent().getStringExtra("idPetSelecionado"));
                    editFragment.setArguments(editArgs);
                    carregarFragment(editFragment);
                    break;
                case "ThreadPostagem":
                    getSupportActionBar().setTitle("Postagem");
                    DetalhePostagemThreadFragment threadFragment = new DetalhePostagemThreadFragment();
                    Bundle threadArgs = new Bundle();
                    threadArgs.putString("ID_ITEM", getIntent().getStringExtra("ID_ITEM"));
                    threadFragment.setArguments(threadArgs);
                    carregarFragment(threadFragment);
                    break;
                case "EditarPostagem":
                    EditarPostagemFragment editarFragment = new EditarPostagemFragment();
                    Bundle editarArgs = new Bundle();
                    editarArgs.putString("ID_ITEM", getIntent().getStringExtra("ID_ITEM"));
                    editarFragment.setArguments(editarArgs);
                    carregarFragment(editarFragment);
                    break;
                case "DetalheDoacaoProprietario":
                    DetalheDoacaoProprietarioFragment detalheDoacaoFragment = new DetalheDoacaoProprietarioFragment();
                    Bundle doacaoArgs = new Bundle();
                    doacaoArgs.putString("ID_ITEM", getIntent().getStringExtra("ID_ITEM"));
                    detalheDoacaoFragment.setArguments(doacaoArgs);
                    carregarFragment(detalheDoacaoFragment);
                    break;

                case "EditarDoacao":
                    EditarDoacaoFragment editarDoacaoFragment = new EditarDoacaoFragment();
                    Bundle editarDoacaoArgs = new Bundle();
                    editarDoacaoArgs.putString("ID_ITEM", getIntent().getStringExtra("ID_ITEM"));
                    editarDoacaoFragment.setArguments(editarDoacaoArgs);
                    carregarFragment(editarDoacaoFragment);
                    break;

            }
        }
    }

    private void carregarFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameUsuarioHub, fragment);
        transaction.commit();
    }
}