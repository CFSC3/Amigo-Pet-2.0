package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.FeedUsuarioAdapter;
import com.ifam.amigopetfixo.adapter.PetCarrosselAdapter;

import java.util.ArrayList;
import java.util.List;

public class VisualizacaoUsuarioFragment extends Fragment {

    private RecyclerView recyclerPetsVisita, recyclerFeedVisita;
    private TextView textNomeVisita, textBioVisita;
    private ImageView imageCapaVisita;
    private de.hdodenhof.circleimageview.CircleImageView imagePerfilVisita;

    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();

    private PetCarrosselAdapter petAdapter;
    private List<Perfil_pet> listaPets = new ArrayList<>();

    private FeedUsuarioAdapter feedAdapter;
    private List<Postagem> listaPostagensFeed = new ArrayList<>();

    private String idUsuarioVisitado;

    public VisualizacaoUsuarioFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visualizacao_usuario, container, false);

        if (getArguments() != null) {
            idUsuarioVisitado = getArguments().getString("ID_ITEM");
        }

        recyclerPetsVisita = view.findViewById(R.id.recyclerPetsVisita);
        recyclerFeedVisita = view.findViewById(R.id.recyclerFeedVisita);
        textNomeVisita = view.findViewById(R.id.textNomeVisita);
        textBioVisita = view.findViewById(R.id.textBioVisita);
        imagePerfilVisita = view.findViewById(R.id.imagePerfilVisita);
        imageCapaVisita = view.findViewById(R.id.imageCapaVisita);

        configurarRecyclerViews();

        if (idUsuarioVisitado != null) {
            carregarDadosUsuario(idUsuarioVisitado);
        } else {
            textNomeVisita.setText("Usuário não encontrado");
            textBioVisita.setText("");
        }

        return view;
    }

    private void configurarRecyclerViews() {
        // Passamos o ID do usuário visitado para o Adapter dos Pets (para ele abrir o Pet certo depois)
        petAdapter = new PetCarrosselAdapter(listaPets, idUsuarioVisitado);
        recyclerPetsVisita.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerPetsVisita.setAdapter(petAdapter);

        feedAdapter = new FeedUsuarioAdapter(listaPostagensFeed, false);
        recyclerFeedVisita.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerFeedVisita.setAdapter(feedAdapter);
    }

    private void carregarDadosUsuario(String uid) {
        DatabaseReference usuarioRef = referencia.child("banco-dados").child("usuario").child(uid);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    String nome = snapshot.child("nome").getValue(String.class);
                    String bio = snapshot.child("texto").getValue(String.class);
                    String imagemNome = snapshot.child("imageUsuario").getValue(String.class);
                    String imagemCapaNome = snapshot.child("imageCapaUsuario").getValue(String.class);

                    String username = snapshot.child("id").getValue(String.class);
                    if (username != null && getActivity() != null) {
                        androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbarVisualizacao);
                        if (toolbar != null) {
                            toolbar.setTitle("@" + username);
                        }
                    }

                    textNomeVisita.setText(nome != null && !nome.isEmpty() ? nome : "Usuário");
                    textBioVisita.setText(bio != null && !bio.isEmpty() ? bio : "Nenhuma descrição informada.");

                    baixarImagemDinamica(imagemNome, imagePerfilVisita, "perfil");
                    baixarImagemDinamica(imagemCapaNome, imageCapaVisita, "capa");

                    carregarPets(uid);
                    carregarPostagensUsuario(snapshot.child("postagens"));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void baixarImagemDinamica(String dadoImagem, ImageView imageView, String pastaStorage) {
        if (dadoImagem == null || dadoImagem.isEmpty() || !isAdded()) return;

        if (dadoImagem.startsWith("http")) {
            Glide.with(this).load(dadoImagem).centerCrop().into(imageView);
        } else {
            StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                    .child("imagens").child(pastaStorage).child(dadoImagem);

            imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (isAdded()) {
                    Glide.with(this).load(uri).centerCrop().into(imageView);
                }
            }).addOnFailureListener(e -> {});
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
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("Postagens").child(idPostagem);
        postRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Postagem postagem = snapshot.getValue(Postagem.class);
                    if (postagem != null) {
                        listaPostagensFeed.add(0, postagem);
                        feedAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {}
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
}