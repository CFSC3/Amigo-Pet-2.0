package com.ifam.amigopetfixo.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.helper.AuthCallback;
import com.ifam.amigopetfixo.helper.EmailLoginManager;
import com.ifam.amigopetfixo.helper.FacebookAuthManager;
import com.ifam.amigopetfixo.helper.GoogleAuthManager;
import com.ifam.amigopetfixo.helper.PasswordRecoveryManager;

public class LoginT extends AppCompatActivity implements AuthCallback {

    private TextInputLayout layoutEmail, layoutSenha;
    private TextInputEditText editEmail, editSenha;

    // Reaproveitando os Managers
    private EmailLoginManager emailLoginManager;
    private PasswordRecoveryManager passwordRecoveryManager;
    private GoogleAuthManager googleAuthManager;
    private FacebookAuthManager facebookAuthManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_t);

        inicializarGerenciadores();
        inicializarComponentes();
        configurarCliques();
    }

    private void inicializarGerenciadores() {
        emailLoginManager = new EmailLoginManager();
        passwordRecoveryManager = new PasswordRecoveryManager();
        facebookAuthManager = new FacebookAuthManager();
        googleAuthManager = new GoogleAuthManager(this, getString(R.string.default_web_client_id));
    }

    private void inicializarComponentes() {
        layoutEmail = findViewById(R.id.layoutEmailLogin);
        layoutSenha = findViewById(R.id.layoutSenhaLogin);
        editEmail = findViewById(R.id.editEmailLogin);
        editSenha = findViewById(R.id.editSenhaLogin);
    }

    private void configurarCliques() {
        // Botão de Entrar (Manual)
        findViewById(R.id.btnEntrarLogin).setOnClickListener(v -> tentarLoginManual());

        // Esqueci a Senha
        findViewById(R.id.txtEsqueciSenha).setOnClickListener(v -> recuperarSenha());

        // Ir para Cadastro
        findViewById(R.id.txtIrParaCadastro).setOnClickListener(v -> {
            startActivity(new Intent(LoginT.this, Cadastro.class));
            finish();
        });

        // Login Social
        findViewById(R.id.btnGoogleLogin).setOnClickListener(v ->
                googleLoginLauncher.launch(googleAuthManager.getSignInIntent())
        );

        findViewById(R.id.btnFacebookLogin).setOnClickListener(v ->
                facebookAuthManager.iniciarLogin(this, this)
        );
    }

    private void tentarLoginManual() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        layoutEmail.setError(null);
        layoutSenha.setError(null);

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Formato de e-mail inválido.");
            return;
        }
        if (senha.isEmpty()) {
            layoutSenha.setError("Digite sua senha.");
            return;
        }

        emailLoginManager.entrar(email, senha, this);
    }

    private void recuperarSenha() {
        String email = editEmail.getText().toString().trim();

        passwordRecoveryManager.recuperarSenha(email, new PasswordRecoveryManager.RecoveryCallback() {
            @Override
            public void onSucesso(String mensagem) {
                Toast.makeText(LoginT.this, mensagem, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFalha(String erro) {
                layoutEmail.setError(erro);
            }
        });
    }

    // Retorno do Google
    private final ActivityResultLauncher<Intent> googleLoginLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    googleAuthManager.processarResultadoGoogle(result.getData(), this);
                }
            }
    );

    // Retorno do Facebook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookAuthManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSucesso(FirebaseUser user) {
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                .child("banco-dados").child("usuario").child(user.getUid());

        ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("id").exists() && !snapshot.child("id").getValue(String.class).isEmpty()) {
                    startActivity(new Intent(LoginT.this, Tela_principal.class));
                } else {
                    startActivity(new Intent(LoginT.this, SetupPerfilActivity.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                startActivity(new Intent(LoginT.this, Tela_principal.class));
                finish();
            }
        });
    }

    @Override
    public void onFalha(String mensagemErro) {
        Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show();
    }
}