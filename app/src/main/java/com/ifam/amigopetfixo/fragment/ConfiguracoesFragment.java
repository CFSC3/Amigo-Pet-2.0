package com.ifam.amigopetfixo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.ConfiguracoesHubActivity;
import com.ifam.amigopetfixo.activity.MainActivity;
import com.ifam.amigopetfixo.repository.UsuarioRepository;

public class ConfiguracoesFragment extends Fragment {

    private MaterialButton btnTrocarSenha, btnSobre, btnTermos, btnPrivacidade, btnSair, btnExcluir;
    private UsuarioRepository usuarioRepository;

    public ConfiguracoesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracoes, container, false);

        usuarioRepository = new UsuarioRepository();

        btnTrocarSenha = view.findViewById(R.id.btnTrocarSenhaConfig);
        btnSobre = view.findViewById(R.id.btnSobreConfig);
        btnTermos = view.findViewById(R.id.btnTermosConfig);
        btnPrivacidade = view.findViewById(R.id.btnPrivacidadeConfig);
        btnSair = view.findViewById(R.id.btnSairConfig);
        btnExcluir = view.findViewById(R.id.btnExcluirConfig);

        configurarCliques();
        return view;
    }

    private void configurarCliques() {
        btnTrocarSenha.setOnClickListener(v -> {
            if (usuarioRepository.logadoViaProvedorExterno()) {
                Toast.makeText(getContext(), "Login via Google/Facebook. Altere a senha por lá.", Toast.LENGTH_LONG).show();
            } else {
                exibirDialogoTrocaSenha();
            }
        });

        btnSobre.setOnClickListener(v -> abrirFragmentoInfo("sobre", "Sobre o App"));
        btnTermos.setOnClickListener(v -> abrirFragmentoInfo("termos", "Termos e Condições"));
        btnPrivacidade.setOnClickListener(v -> abrirFragmentoInfo("privacidade", "Política de Privacidade"));

        btnSair.setOnClickListener(v -> {
            usuarioRepository.sairDoApp();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnExcluir.setOnClickListener(v -> confirmarExclusao());
    }

    private void abrirFragmentoInfo(String tipo, String titulo) {
        InformacoesAppFragment infoFragment = new InformacoesAppFragment();
        Bundle args = new Bundle();
        args.putString("tipoTela", tipo);
        infoFragment.setArguments(args);

        if (getActivity() instanceof ConfiguracoesHubActivity) {
            ((ConfiguracoesHubActivity) getActivity()).carregarFragment(infoFragment, titulo);
        }
    }

    private void exibirDialogoTrocaSenha() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nova Senha");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputNovaSenha = new EditText(requireContext());
        inputNovaSenha.setHint("Digite a nova senha");
        inputNovaSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputNovaSenha);

        builder.setView(layout);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String senha = inputNovaSenha.getText().toString();
            if (senha.length() >= 8) {
                usuarioRepository.trocarSenhaFirebase(senha, new UsuarioRepository.OnUpdateCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Senha atualizada!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(), "Erro: Requer login recente.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Mínimo de 8 caracteres.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir Conta")
                .setMessage("Esta ação destruirá permanentemente seu perfil, pets, postagens, eventos e adoções. Tem certeza absoluta?")
                .setPositiveButton("Sim, Excluir Tudo", (dialog, which) -> {
                    btnExcluir.setEnabled(false);
                    btnExcluir.setText("Excluindo dados...");

                    String usernameAtual = "";
                    usuarioRepository.excluirContaCompleta(usernameAtual, new UsuarioRepository.OnUpdateCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Conta excluída.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                            btnExcluir.setEnabled(true);
                            btnExcluir.setText("Excluir Conta Definitivamente");
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}