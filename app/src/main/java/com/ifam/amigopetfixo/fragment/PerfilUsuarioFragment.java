package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.ifam.amigopetfixo.Model.bean.Adocoes;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.activity.ConfiguracoesHubActivity;
import com.ifam.amigopetfixo.activity.UsuarioHubActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.AdocoesAdapter;
import com.ifam.amigopetfixo.adapter.FeedUsuarioAdapter;
import com.ifam.amigopetfixo.adapter.PetCarrosselAdapter;

import java.util.ArrayList;
import java.util.List;

public class PerfilUsuarioFragment extends Fragment {

    private MaterialButton btnEditarPerfil, btnAdicionarPet;
    private ImageButton btnMenuConfiguracoes;
    private RecyclerView recyclerPetsCarrossel, recyclerFeedUsuario;
    private TextView textNomePerfil, textBioPerfil;
    private ImageView imageCapaPerfil;
    private de.hdodenhof.circleimageview.CircleImageView imagePerfilUsuario;

    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private PetCarrosselAdapter petAdapter;
    private List<Perfil_pet> listaPets = new ArrayList<>();

    private FeedUsuarioAdapter feedAdapter;
    private List<Postagem> listaPostagensFeed = new ArrayList<>();

    private MaterialButtonToggleGroup toggleGroupFeed;
    private AdocoesAdapter doacoesAdapter;
    private List<Adocoes> listaDoacoesUsuario = new ArrayList<>();

    public PerfilUsuarioFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false);

        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnAdicionarPet = view.findViewById(R.id.btnAdicionarPet);
        btnMenuConfiguracoes = view.findViewById(R.id.btnMenuConfiguracoes);
        recyclerPetsCarrossel = view.findViewById(R.id.recyclerPetsCarrossel);
        recyclerFeedUsuario = view.findViewById(R.id.recyclerFeedUsuario);
        textNomePerfil = view.findViewById(R.id.textNomePerfil);
        textBioPerfil = view.findViewById(R.id.textBioPerfil);
        imagePerfilUsuario = view.findViewById(R.id.imagePerfilUsuario);
        imageCapaPerfil = view.findViewById(R.id.imageCapaPerfil);
        toggleGroupFeed = view.findViewById(R.id.toggleGroupFeed);

        configurarRecyclerViews();
        configurarCliques();
        carregarDadosUsuario();

        return view;
    }

    private void configurarRecyclerViews() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            petAdapter = new PetCarrosselAdapter(listaPets, currentUser.getUid());
            recyclerPetsCarrossel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            recyclerPetsCarrossel.setAdapter(petAdapter);

            feedAdapter = new FeedUsuarioAdapter(listaPostagensFeed, true);
            recyclerFeedUsuario.setLayoutManager(new GridLayoutManager(getContext(), 3));
            recyclerFeedUsuario.setAdapter(feedAdapter);

        }

        doacoesAdapter = new com.ifam.amigopetfixo.adapter.AdocoesAdapter(listaDoacoesUsuario, adocao -> {
            Intent intent = new Intent(getActivity(), UsuarioHubActivity.class);
            intent.putExtra("Acao", "DetalheDoacaoProprietario");
            intent.putExtra("ID_ITEM", adocao.getIdAdocao());
            startActivity(intent);
        });

        toggleGroupFeed.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnAbaPostagens) {
                    recyclerFeedUsuario.setLayoutManager(new GridLayoutManager(getContext(), 3));
                    recyclerFeedUsuario.setAdapter(feedAdapter);
                } else if (checkedId == R.id.btnAbaDoacoes) {
                    recyclerFeedUsuario.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerFeedUsuario.setAdapter(doacoesAdapter);
                }
            }
        });
    }

    private void carregarDadosUsuario() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        DatabaseReference usuarioRef = referencia.child("banco-dados").child("usuario").child(uid);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nome = snapshot.child("nome").getValue(String.class);
                    String bio = snapshot.child("texto").getValue(String.class);
                    String imagemNome = snapshot.child("imageUsuario").getValue(String.class);
                    String imagemCapaNome = snapshot.child("imageCapaUsuario").getValue(String.class);

                    textNomePerfil.setText(nome != null && !nome.isEmpty() ? nome : "Usuário");
                    textBioPerfil.setText(bio != null && !bio.isEmpty() ? bio : "Nenhuma descrição informada.");

                    baixarImagemDinamica(imagemNome, imagePerfilUsuario, "perfil");
                    baixarImagemDinamica(imagemCapaNome, imageCapaPerfil, "capa");

                    carregarPets(uid);
                    carregarPostagensUsuario(snapshot.child("postagens"));
                    carregarDoacoesUsuario(snapshot.child("adocoesDoacoes"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void baixarImagemDinamica(String dadoImagem, ImageView imageView, String pastaStorage) {

        if (!isAdded() || getActivity() == null) {
            return;
        }

        if (dadoImagem == null || dadoImagem.isEmpty()) return;

        if (dadoImagem.startsWith("http")) {
            Glide.with(this)
                    .load(dadoImagem)
                    .centerCrop()
                    .into(imageView);
        } else {

            StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                    .child("imagens").child(pastaStorage).child(dadoImagem);

            imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (isAdded()) {
                    Glide.with(this)
                            .load(uri)
                            .centerCrop()
                            .into(imageView);
                }
            }).addOnFailureListener(e -> {
            });
        }
    }

    private void carregarPostagensUsuario(DataSnapshot postagensSnapshot) {
        listaPostagensFeed.clear();
        if (!postagensSnapshot.exists()) {
            feedAdapter.notifyDataSetChanged();
            return;
        }

        for (DataSnapshot postSnap : postagensSnapshot.getChildren()) {
            String idPostagem = postSnap.getKey();
            if (idPostagem != null) {
                buscarDetalhesPostagem(idPostagem);
            }
        }
    }

    private void buscarDetalhesPostagem(String idPostagem) {
        DatabaseReference postRef = referencia.child("banco-dados").child("Postagens").child(idPostagem);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Postagem postagem = snapshot.getValue(Postagem.class);
                    if (postagem != null) {
                        listaPostagensFeed.add(0, postagem);
                        feedAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void carregarDoacoesUsuario(DataSnapshot doacoesSnapshot) {
        listaDoacoesUsuario.clear();
        if (!doacoesSnapshot.exists()) {
            doacoesAdapter.notifyDataSetChanged();
            return;
        }

        for (DataSnapshot postSnap : doacoesSnapshot.getChildren()) {
            String idDoacao = postSnap.getKey();
            if (idDoacao != null) buscarDetalhesDoacao(idDoacao);
        }
    }

    private void buscarDetalhesDoacao(String idDoacao) {
        DatabaseReference doacaoRef = referencia.child("banco-dados").child("adocoesDoacoes").child(idDoacao);
        doacaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    com.ifam.amigopetfixo.Model.bean.Adocoes adocao = snapshot.getValue(com.ifam.amigopetfixo.Model.bean.Adocoes.class);
                    if (adocao != null) {
                        adocao.setIdAdocao(idDoacao);
                        listaDoacoesUsuario.add(0, adocao);
                        doacoesAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void carregarPets(String uid) {
        DatabaseReference petsRef = referencia.child("banco-dados").child("usuario").child(uid).child("pets");

        petsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaPets.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot petSnap : snapshot.getChildren()) {
                        Perfil_pet pet = petSnap.getValue(Perfil_pet.class);
                        if (pet != null) {
                            listaPets.add(pet);
                        }
                    }
                }
                petAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void configurarCliques() {
        btnEditarPerfil.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), UsuarioHubActivity.class);
                intent.putExtra("Acao", "EditarPerfil");
                startActivity(intent);
            }
        });

        // Já deixei o do Pet pronto também!
        btnAdicionarPet.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), UsuarioHubActivity.class);
                intent.putExtra("Acao", "AdicionarPet");
                startActivity(intent);
            }
        });

        btnMenuConfiguracoes.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ConfiguracoesHubActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }
}