package com.ifam.amigopetfixo.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.R;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usuario = FirebaseAuth.getInstance();

        if (isUsuarioLogado()) {
            redirecionarParaPrincipal();
            return;
        }

        setContentView(R.layout.activity_main);
        configurarBotoes();
    }

    private boolean isUsuarioLogado() {
        return usuario.getCurrentUser() != null;
    }

    private void redirecionarParaPrincipal() {
        String uid = usuario.getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                .child("banco-dados").child("usuario").child(uid);

        ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("id").exists() && !snapshot.child("id").getValue(String.class).isEmpty()) {
                    startActivity(new Intent(MainActivity.this, Tela_principal.class));
                } else {
                    startActivity(new Intent(MainActivity.this, SetupPerfilActivity.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                startActivity(new Intent(MainActivity.this, Tela_principal.class));
                finish();
            }
        });
    }
    private void configurarBotoes() {
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnVisitante = findViewById(R.id.btnVisitante);
        Button btnCadastro = findViewById(R.id.btnCadastro);

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginT.class));
        });

        btnVisitante.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Tela_principal.class));
        });

        btnCadastro.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Cadastro.class));
        });
    }
}