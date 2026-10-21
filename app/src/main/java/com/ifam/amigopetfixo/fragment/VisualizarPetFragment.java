package com.ifam.amigopetfixo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.UsuarioHubActivity;
import com.ifam.amigopetfixo.repository.PetRepository;

public class VisualizarPetFragment extends Fragment {

    private de.hdodenhof.circleimageview.CircleImageView imgPet;
    private TextView textNomePet, textEspecie, textRaca, textCor, textCastrado;
    private MaterialButton btnEditarPet, btnExcluirPet;

    private String idPetSelecionado;
    private PetRepository petRepository;
    private ValueEventListener petListener;

    public VisualizarPetFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visualizar_pet, container, false);

        petRepository = new PetRepository();

        if (getArguments() != null) {
            idPetSelecionado = getArguments().getString("idPetSelecionado");
        }

        imgPet = view.findViewById(R.id.imgPetVisualizar);
        textNomePet = view.findViewById(R.id.textNomePetVisualizar);
        textEspecie = view.findViewById(R.id.textEspecieVisualizar);
        textRaca = view.findViewById(R.id.textRacaVisualizar);
        textCor = view.findViewById(R.id.textCorVisualizar);
        textCastrado = view.findViewById(R.id.textCastradoVisualizar);
        btnEditarPet = view.findViewById(R.id.btnEditarPet);
        btnExcluirPet = view.findViewById(R.id.btnExcluirPet);

        if (idPetSelecionado != null) {
            carregarDadosPet();
            configurarCliques();
        } else {
            Toast.makeText(getContext(), "Erro ao carregar os dados do pet.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
        }

        return view;
    }

    private void carregarDadosPet() {
        petListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Perfil_pet pet = snapshot.getValue(Perfil_pet.class);
                    if (pet != null) {
                        textNomePet.setText(pet.getNomePet());
                        textEspecie.setText(pet.getEspecie());
                        textRaca.setText(pet.getRaca());
                        textCor.setText(pet.getCor());
                        textCastrado.setText(pet.getCastrado());

                        String urlFoto = pet.getUrlFotoPet();
                        if (urlFoto != null && !urlFoto.isEmpty()) {
                            Glide.with(VisualizarPetFragment.this).load(urlFoto).centerCrop().into(imgPet);
                        } else {
                            imgPet.setImageResource(R.drawable.icon_pet_profile);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        petRepository.getPetsReference().child(idPetSelecionado).addValueEventListener(petListener);
    }

    private void configurarCliques() {
        btnEditarPet.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UsuarioHubActivity.class);
            intent.putExtra("Acao", "EditarPet");
            intent.putExtra("idPetSelecionado", idPetSelecionado);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        btnExcluirPet.setOnClickListener(v -> confirmarExclusao());
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(getContext())
                .setTitle("Excluir Pet")
                .setMessage("Tem certeza que deseja excluir este pet? Esta ação apagará todas as informações e fotos dele permanentemente.")
                .setPositiveButton("Excluir", (dialog, which) -> processarExclusao())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void processarExclusao() {
        btnExcluirPet.setEnabled(false);
        btnExcluirPet.setText("Excluindo...");

        petRepository.excluirPet(idPetSelecionado).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Pet excluído com sucesso.", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().finish(); // Fecha a tela de visualizar e volta pro Perfil
            } else {
                Toast.makeText(getContext(), "Erro ao excluir o pet.", Toast.LENGTH_SHORT).show();
                btnExcluirPet.setEnabled(true);
                btnExcluirPet.setText("Excluir Pet");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (petListener != null && idPetSelecionado != null) {
            petRepository.getPetsReference().child(idPetSelecionado).removeEventListener(petListener);
        }
    }
}