package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;
import com.ifam.amigopetfixo.R;

public class VisualizacaoPetPublicoFragment extends Fragment {

    private de.hdodenhof.circleimageview.CircleImageView imgPetPublico;
    private TextView textNomePetPublico, textEspeciePublico, textRacaPublico, textCorPublico, textCastradoPublico;

    private String idPetSelecionado;
    private String idDonoSelecionado;

    public VisualizacaoPetPublicoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visualizacao_pet_publico, container, false);

        if (getArguments() != null) {
            idPetSelecionado = getArguments().getString("ID_ITEM");
            idDonoSelecionado = getArguments().getString("ID_DONO");
        }

        imgPetPublico = view.findViewById(R.id.imgPetPublico);
        textNomePetPublico = view.findViewById(R.id.textNomePetPublic);
        textEspeciePublico = view.findViewById(R.id.textEspeciePublic);
        textRacaPublico = view.findViewById(R.id.textRacaPublic);
        textCorPublico = view.findViewById(R.id.textCorPublic);
        textCastradoPublico = view.findViewById(R.id.textCastradoPublic);

        if (idPetSelecionado != null && idDonoSelecionado != null) {
            carregarDadosPetPublico();
        } else {
            Toast.makeText(getContext(), "Erro ao carregar dados do pet visitante.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
        }

        return view;
    }

    private void carregarDadosPetPublico() {

        DatabaseReference petPublicoRef = FirebaseDatabase.getInstance().getReference()
                .child("banco-dados").child("usuario").child(idDonoSelecionado).child("pets").child(idPetSelecionado);

        petPublicoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Perfil_pet pet = snapshot.getValue(Perfil_pet.class);
                    if (pet != null) {
                        textNomePetPublico.setText(pet.getNomePet());
                        textEspeciePublico.setText(pet.getEspecie());
                        textRacaPublico.setText(pet.getRaca());
                        textCorPublico.setText(pet.getCor());
                        textCastradoPublico.setText(pet.getCastrado());

                        // Dinamizar Título do Hub
                        if (getActivity() != null && pet.getNomePet() != null) {
                            androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbarVisualizacao);
                            if (toolbar != null) {
                                toolbar.setTitle(pet.getNomePet());
                            }
                        }

                        String urlFoto = pet.getUrlFotoPet();
                        if (urlFoto != null && !urlFoto.isEmpty()) {
                            Glide.with(VisualizacaoPetPublicoFragment.this).load(urlFoto).centerCrop().into(imgPetPublico);
                        } else {
                            imgPetPublico.setImageResource(R.drawable.icon_pet_profile);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}