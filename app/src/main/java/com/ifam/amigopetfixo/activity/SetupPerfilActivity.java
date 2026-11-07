package com.ifam.amigopetfixo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.UsernameCheckCallback;
import com.ifam.amigopetfixo.repository.UsuarioRepository;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class SetupPerfilActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ShapeableImageView imagePerfil;
    private FloatingActionButton fabTrocarFoto;
    private TextInputEditText editNomeExibicao;
    private TextInputEditText editUsername;
    private TextInputLayout inputLayoutUsername;
    private TextInputLayout inputLayoutNome;
    private MaterialButton btnConcluirSetup;

    private Uri imagemSelecionadaUri = null;
    private boolean usernameValido = false;

    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_perfil);

        usuarioRepository = new UsuarioRepository();

        inicializarComponentes();
        configurarEventosDeClique();
        configurarVerificacaoDeUsername();
    }

    private void inicializarComponentes() {
        imagePerfil = findViewById(R.id.imagePerfil);
        fabTrocarFoto = findViewById(R.id.fabTrocarFoto);
        editNomeExibicao = findViewById(R.id.editNomeExibicao);
        editUsername = findViewById(R.id.editUsername);
        inputLayoutUsername = findViewById(R.id.inputLayoutUsername);
        inputLayoutNome = findViewById(R.id.inputLayoutNome);
        btnConcluirSetup = findViewById(R.id.btnConcluirSetup);
    }

    private void configurarEventosDeClique() {
        fabTrocarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnConcluirSetup.setOnClickListener(v -> {
            if (validarCampos()) {
                iniciarSalvamento();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            iniciarUCrop(data.getData());
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            imagemSelecionadaUri = UCrop.getOutput(data);
            imagePerfil.setImageURI(imagemSelecionadaUri);
        } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
            Throwable erro = UCrop.getError(data);
            Toast.makeText(this, "Erro: " + erro.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarUCrop(@NonNull Uri sourceUri) {
        String nomeArquivo = UUID.randomUUID().toString() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), nomeArquivo));
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setToolbarTitle("Ajustar Foto");

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(this);
    }

    private void configurarVerificacaoDeUsername() {
        editUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String idDigitado = s.toString().trim().toLowerCase();

                // Sem ponto, sem espaço, sem acento
                if (!idDigitado.matches("^[a-z0-9_]{8,}$")) {
                    if (idDigitado.length() < 8) {
                        inputLayoutUsername.setError("O ID deve ter pelo menos 8 caracteres.");
                    } else {
                        inputLayoutUsername.setError("Use apenas letras, números e underline (_).");
                    }
                    usernameValido = false;
                    return; // Interrompe aqui, não deixa ir pro Firebase e bugar tudo
                }

                inputLayoutUsername.setError(null);
                inputLayoutUsername.setHelperText("Verificando disponibilidade...");

                usuarioRepository.verificarIdDisponivel(idDigitado, new UsernameCheckCallback() {
                    @Override
                    public void onResult(boolean isAvailable) {
                        if (isAvailable) {
                            inputLayoutUsername.setError(null);
                            inputLayoutUsername.setHelperText("ID disponível!");
                            usernameValido = true;
                        } else {
                            inputLayoutUsername.setHelperText(null);
                            inputLayoutUsername.setError("Esse ID já está em uso.");
                            usernameValido = false;
                        }
                    }
                    @Override
                    public void onError(String errorMessage) {
                        inputLayoutUsername.setHelperText(null);
                        inputLayoutUsername.setError("Erro de rede. Tente novamente.");
                        usernameValido = false;
                    }
                });
            }
        });
    }

    private boolean validarCampos() {
        // Trava 1: Imagem Obrigatória
        if (imagemSelecionadaUri == null) {
            Toast.makeText(this, "Por favor, adicione uma foto de perfil.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Trava 2: Nome Real (Mínimo de 4 letras)
        String nomeDigitado = editNomeExibicao.getText().toString().trim();
        if (nomeDigitado.isEmpty() || nomeDigitado.length() < 4) {
            inputLayoutNome.setError("Seu nome deve ter pelo menos 4 caracteres.");
            editNomeExibicao.requestFocus();
            return false;
        } else {
            inputLayoutNome.setError(null);
        }

        // Trava 3: Username Aprovado
        if (!usernameValido) {
            inputLayoutUsername.setError("Escolha um ID válido e disponível.");
            editUsername.requestFocus();
            return false;
        }

        return true;
    }

    private void iniciarSalvamento() {
        String nome = editNomeExibicao.getText().toString().trim();
        String idDesejado = editUsername.getText().toString().trim().toLowerCase();

        btnConcluirSetup.setEnabled(false);
        btnConcluirSetup.setText("Salvando...");

        usuarioRepository.salvarPerfilCompleto(nome, idDesejado, imagemSelecionadaUri, new UsuarioRepository.OnUpdateCompleteListener() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(SetupPerfilActivity.this, Tela_principal.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa o histórico de telas
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                btnConcluirSetup.setEnabled(true);
                btnConcluirSetup.setText("Pronto, vamos começar!");
            }
        });
    }
}