package com.ifam.amigopetfixo.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.helper.AuthCallback;
import com.ifam.amigopetfixo.helper.DatabaseManager;
import com.ifam.amigopetfixo.helper.EmailAuthManager;
import com.ifam.amigopetfixo.helper.FacebookAuthManager;
import com.ifam.amigopetfixo.helper.GoogleAuthManager;

public class Cadastro extends AppCompatActivity implements AuthCallback {

    private TextInputLayout layoutEmail, layoutSenha, layoutConfSenha;
    private TextInputEditText editEmail, editSenha, editConfSenha;
    private EmailAuthManager emailAuthManager;
    private GoogleAuthManager googleAuthManager;
    private FacebookAuthManager facebookAuthManager;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarGerenciadores();
        inicializarComponentes();
        configurarCliques();
    }

    private void inicializarGerenciadores() {
        emailAuthManager = new EmailAuthManager();
        databaseManager = new DatabaseManager();
        googleAuthManager = new GoogleAuthManager(this, getString(R.string.default_web_client_id));
        facebookAuthManager = new FacebookAuthManager();
    }

    private void inicializarComponentes() {
        layoutEmail = findViewById(R.id.textInputLayout2);
        layoutSenha = findViewById(R.id.textInputLayout4);
        layoutConfSenha = findViewById(R.id.textInputLayout5);
        editEmail = findViewById(R.id.textoEmail);
        editSenha = findViewById(R.id.textoSenha);
        editConfSenha = findViewById(R.id.textoConfSenha);
    }

    private void configurarCliques() {
        findViewById(R.id.btnCadastro).setOnClickListener(v -> tentarCadastroManual());

        findViewById(R.id.txtIrParaLogin).setOnClickListener(v -> {
            startActivity(new Intent(Cadastro.this, LoginT.class));
            finish();
        });

        findViewById(R.id.btnGoogle).setOnClickListener(v ->
                googleLoginLauncher.launch(googleAuthManager.getSignInIntent())
        );

        findViewById(R.id.btnFacebook).setOnClickListener(v ->
                facebookAuthManager.iniciarLogin(this, this)
        );
    }

    private void tentarCadastroManual() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        String confSenha = editConfSenha.getText().toString().trim();

        layoutEmail.setError(null);
        layoutSenha.setError(null);
        layoutConfSenha.setError(null);

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Formato de e-mail inválido.");
            return;
        }
        if (senha.length() < 8) {
            layoutSenha.setError("Mínimo 8 caracteres.");
            return;
        }
        if (!senha.equals(confSenha)) {
            layoutConfSenha.setError("Senhas não coincidem.");
            return;
        }

        emailAuthManager.cadastrar(email, senha, this);
    }

    // Retorno da janela do Google
    private final ActivityResultLauncher<Intent> googleLoginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    googleAuthManager.processarResultadoGoogle(result.getData(), this);
                }
            }
    );

    @Override
    public void onSucesso(FirebaseUser user) {
        if (user != null && user.getMetadata() != null) {
            long tempoCriacao = user.getMetadata().getCreationTimestamp();
            long tempoUltimoLogin = user.getMetadata().getLastSignInTimestamp();

            if (Math.abs(tempoUltimoLogin - tempoCriacao) < 3000) {
                databaseManager.criarPerfilInicial(user.getUid(), user.getEmail());
                startActivity(new Intent(this, Apresentacao.class));
                finish();
            } else {
                Toast.makeText(this, "Você já tem uma conta! Fazendo login...", Toast.LENGTH_LONG).show();

                com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                        .child("banco-dados").child("usuario").child(user.getUid());

                ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.child("id").exists() && !snapshot.child("id").getValue(String.class).isEmpty()) {
                            startActivity(new Intent(Cadastro.this, Tela_principal.class));
                        } else {
                            startActivity(new Intent(Cadastro.this, SetupPerfilActivity.class));
                        }
                        finish();
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        startActivity(new Intent(Cadastro.this, Tela_principal.class));
                        finish();
                    }
                });
            }
        }
    }

    @Override
    public void onFalha(String mensagemErro) {
        Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookAuthManager.onActivityResult(requestCode, resultCode, data);
    }
}