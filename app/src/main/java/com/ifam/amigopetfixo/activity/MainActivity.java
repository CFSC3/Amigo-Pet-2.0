package com.ifam.amigopetfixo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.R;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth usuario;
    private Button btnLogin, btnVisitante, btnCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        aplicarTemaSalvo();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        usuario = FirebaseAuth.getInstance();

        btnLogin = findViewById(R.id.btnLogin);
        btnVisitante = findViewById(R.id.btnVisitante);
        btnCadastro = findViewById(R.id.btnCadastro);

        if (isUsuarioLogado()) {
            btnLogin.setVisibility(View.GONE);
            btnVisitante.setVisibility(View.GONE);
            btnCadastro.setVisibility(View.GONE);
            redirecionarParaPrincipal();
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            btnVisitante.setVisibility(View.VISIBLE);
            btnCadastro.setVisibility(View.VISIBLE);
            configurarBotoes();
        }
    }

    private void aplicarTemaSalvo() {
        SharedPreferences prefs = getSharedPreferences("tema_prefs", Context.MODE_PRIVATE);
        int temaSalvo = prefs.getInt("tema_selecionado", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(temaSalvo);
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
                Intent intent;
                if (snapshot.exists() && snapshot.child("id").exists() && !snapshot.child("id").getValue(String.class).isEmpty()) {
                    intent = new Intent(MainActivity.this, Tela_principal.class);
                } else {
                    intent = new Intent(MainActivity.this, SetupPerfilActivity.class);
                }
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                startActivity(new Intent(MainActivity.this, Tela_principal.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }

    private void configurarBotoes() {
        btnLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginT.class)));
        btnVisitante.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Tela_principal.class)));
        btnCadastro.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Cadastro.class)));
    }
}