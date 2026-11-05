package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.PesquisaUsuarioAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaFavoritosFragment extends Fragment {

    private RecyclerView recyclerFavoritos;
    private TextView textNenhumFavorito;
    private DatabaseReference rootRef;
    private String currentUserId;
    private PesquisaUsuarioAdapter adapter;
    private List<Map<String, String>> listaUsuariosFavoritos = new ArrayList<>();

    public ListaFavoritosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_favoritos, container, false);

        recyclerFavoritos = view.findViewById(R.id.recyclerFavoritos);
        textNenhumFavorito = view.findViewById(R.id.textNenhumFavorito);

        rootRef = FirebaseDatabase.getInstance().getReference();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        recyclerFavoritos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PesquisaUsuarioAdapter(listaUsuariosFavoritos, getContext());
        recyclerFavoritos.setAdapter(adapter);

        carregarFavoritos();

        return view;
    }

    private void carregarFavoritos() {
        if (currentUserId == null) return;

        rootRef.child("banco-dados").child("Favoritos").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaUsuariosFavoritos.clear();
                        if (snapshot.exists()) {
                            textNenhumFavorito.setVisibility(View.GONE);
                            recyclerFavoritos.setVisibility(View.VISIBLE);

                            for (DataSnapshot favSnap : snapshot.getChildren()) {
                                String uidFavoritado = favSnap.getKey();
                                buscarDetalhesUsuario(uidFavoritado);
                            }
                        } else {
                            textNenhumFavorito.setVisibility(View.VISIBLE);
                            recyclerFavoritos.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void buscarDetalhesUsuario(String uid) {
        rootRef.child("banco-dados").child("usuario").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && isAdded()) {
                            Map<String, String> usuario = new HashMap<>();
                            usuario.put("uid", uid);
                            usuario.put("username", snapshot.child("id").getValue(String.class));
                            usuario.put("nome", snapshot.child("nome").getValue(String.class));
                            usuario.put("fotoUrl", snapshot.child("imageUsuario").getValue(String.class));

                            listaUsuariosFavoritos.add(usuario);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}