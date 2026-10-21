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
    private MaterialButton btnConcluirSetup;

    private Uri imagemSelecionadaUri;
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
                if (idDigitado.length() < 6) {
                    inputLayoutUsername.setError("O ID deve ter pelo menos 6 caracteres.");
                    usernameValido = false;
                    return;
                }

                usuarioRepository.verificarIdDisponivel(idDigitado, new UsernameCheckCallback() {
                    @Override
                    public void onResult(boolean isAvailable) {
                        if (isAvailable) {
                            inputLayoutUsername.setError(null);
                            inputLayoutUsername.setHelperText("ID disponível!");
                            usernameValido = true;
                        } else {
                            inputLayoutUsername.setError("Esse ID já está em uso.");
                            usernameValido = false;
                        }
                    }
                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(SetupPerfilActivity.this, "Erro ao verificar rede", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean validarCampos() {
        if (editNomeExibicao.getText().toString().trim().isEmpty()) {
            editNomeExibicao.setError("Digite seu nome.");
            return false;
        }
        if (!usernameValido) {
            Toast.makeText(this, "Escolha um ID válido e disponível.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void iniciarSalvamento() {
        String nome = editNomeExibicao.getText().toString().trim();
        String idDesejado = editUsername.getText().toString().trim().toLowerCase();

        btnConcluirSetup.setEnabled(false);

        usuarioRepository.salvarPerfilCompleto(nome, idDesejado, imagemSelecionadaUri, new UsuarioRepository.OnUpdateCompleteListener() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(SetupPerfilActivity.this, Tela_principal.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(SetupPerfilActivity.this, "Erro ao salvar: " + error, Toast.LENGTH_LONG).show();
                btnConcluirSetup.setEnabled(true);
                btnConcluirSetup.setText("Pronto, vamos começar!");
            }
        });
    }
}