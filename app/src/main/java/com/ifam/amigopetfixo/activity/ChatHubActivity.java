package com.ifam.amigopetfixo.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.fragment.ListaChatsFragment;
import com.ifam.amigopetfixo.fragment.ConversaFragment;

public class ChatHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_hub);

        configurarToolbar();
        processarNavegacao(getIntent().getStringExtra("Acao"));
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarChatHub);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void processarNavegacao(String acao) {
        if (acao == null || acao.equals("ListaChats")) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Mensagens");
            }
            carregarFragment(new ListaChatsFragment());
        } else if (acao.equals("AbrirConversa")) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("");
            }

            String idDestinatario = getIntent().getStringExtra("uidDestino");

            if (idDestinatario != null) {
                FirebaseDatabase.getInstance().getReference()
                        .child("banco-dados").child("usuario").child(idDestinatario)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && getSupportActionBar() != null) {
                                    String nome = snapshot.child("nome").getValue(String.class);
                                    getSupportActionBar().setTitle(nome != null ? nome : "Usuário");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Chat");
                            }
                        });
            }

            ConversaFragment fragment = new ConversaFragment();
            Bundle args = new Bundle();
            args.putString("uidDestino", idDestinatario);
            fragment.setArguments(args);
            carregarFragment(fragment);
        }
    }

    private void carregarFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameChatHub, fragment);
        transaction.commit();
    }
}