package com.ifam.amigopetfixo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.PetRepository;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class EditarPetFragment extends Fragment {

    private de.hdodenhof.circleimageview.CircleImageView imgEditFotoPet;
    private TextInputEditText editNomePetEdit, editRacaEdit, editCorEdit, editEspecieOutroEdit;
    private TextInputLayout layoutEspecieOutroEdit;
    private AutoCompleteTextView dropdownEspecieEdit;
    private RadioGroup radioGroupCastradoEdit;
    private MaterialButton btnSalvarEdicaoPet;

    private String idPetSelecionado;
    private String urlFotoAtual = "";
    private Uri uriNovaImagem = null;
    private PetRepository petRepository;

    private final ActivityResultLauncher<Intent> selecionarImagemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        iniciarUCrop(uri);
                    }
                }
            }
    );

    public EditarPetFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_pet, container, false);

        petRepository = new PetRepository();
        if (getArguments() != null) {
            idPetSelecionado = getArguments().getString("idPetSelecionado");
        }

        imgEditFotoPet = view.findViewById(R.id.imgEditFotoPet);
        editNomePetEdit = view.findViewById(R.id.editNomePetEdit);
        dropdownEspecieEdit = view.findViewById(R.id.dropdownEspecieEdit);
        layoutEspecieOutroEdit = view.findViewById(R.id.layoutEspecieOutroEdit);
        editEspecieOutroEdit = view.findViewById(R.id.editEspecieOutroEdit);
        editRacaEdit = view.findViewById(R.id.editRacaEdit);
        editCorEdit = view.findViewById(R.id.editCorEdit);
        radioGroupCastradoEdit = view.findViewById(R.id.radioGroupCastradoEdit);
        btnSalvarEdicaoPet = view.findViewById(R.id.btnSalvarEdicaoPet);

        configurarDropdownEspecie();
        configurarCliques();
        carregarDadosAtuais();

        return view;
    }

    private void carregarDadosAtuais() {
        if (idPetSelecionado == null) return;

        petRepository.getPetsReference().child(idPetSelecionado).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Perfil_pet pet = snapshot.getValue(Perfil_pet.class);
                    if (pet != null) {
                        editNomePetEdit.setText(pet.getNomePet());
                        editRacaEdit.setText(pet.getRaca());
                        editCorEdit.setText(pet.getCor());
                        urlFotoAtual = pet.getUrlFotoPet() != null ? pet.getUrlFotoPet() : "";

                        // Preencher Dropdown de Espécie
                        String especie = pet.getEspecie();
                        String[] comuns = {"Cachorro", "Gato", "Pássaro", "Roedor", "Réptil"};
                        if (Arrays.asList(comuns).contains(especie)) {
                            dropdownEspecieEdit.setText(especie, false);
                        } else {
                            dropdownEspecieEdit.setText("Outro", false);
                            layoutEspecieOutroEdit.setVisibility(View.VISIBLE);
                            editEspecieOutroEdit.setText(especie);
                        }

                        // Preencher RadioButton
                        if ("Sim".equals(pet.getCastrado())) {
                            radioGroupCastradoEdit.check(R.id.radioSimEdit);
                        } else if ("Não".equals(pet.getCastrado())) {
                            radioGroupCastradoEdit.check(R.id.radioNaoEdit);
                        }

                        // Preencher Foto
                        if (!urlFotoAtual.isEmpty()) {
                            Glide.with(EditarPetFragment.this).load(urlFotoAtual).centerCrop().into(imgEditFotoPet);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void configurarDropdownEspecie() {
        String[] especiesComuns = {"Cachorro", "Gato", "Pássaro", "Roedor", "Réptil", "Outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, especiesComuns);
        dropdownEspecieEdit.setAdapter(adapter);

        dropdownEspecieEdit.setOnItemClickListener((parent, view, position, id) -> {
            if ("Outro".equals(adapter.getItem(position))) {
                layoutEspecieOutroEdit.setVisibility(View.VISIBLE);
            } else {
                layoutEspecieOutroEdit.setVisibility(View.GONE);
                editEspecieOutroEdit.setText("");
            }
        });
    }

    private void configurarCliques() {
        imgEditFotoPet.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            selecionarImagemLauncher.launch(intent);
        });
        btnSalvarEdicaoPet.setOnClickListener(v -> validarESalvarPet());
    }

    private void iniciarUCrop(@NonNull Uri sourceUri) {
        String nomeArquivo = UUID.randomUUID().toString() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), nomeArquivo));
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setToolbarTitle("Ajustar Foto do Pet");
        UCrop.of(sourceUri, destinationUri).withAspectRatio(1, 1).withOptions(options).start(requireContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK && data != null) {
            uriNovaImagem = UCrop.getOutput(data);
            Glide.with(this).load(uriNovaImagem).centerCrop().into(imgEditFotoPet);
        }
    }

    private void validarESalvarPet() {
        String nome = editNomePetEdit.getText().toString().trim();
        String especieSelecionada = dropdownEspecieEdit.getText().toString().trim();
        String raca = editRacaEdit.getText().toString().trim();
        String cor = editCorEdit.getText().toString().trim();
        int radioId = radioGroupCastradoEdit.getCheckedRadioButtonId();

        final String especieFinal = "Outro".equals(especieSelecionada) ? editEspecieOutroEdit.getText().toString().trim() : especieSelecionada;

        if (nome.isEmpty() || especieFinal.isEmpty() || radioId == -1) {
            Toast.makeText(getContext(), "Preencha o nome, espécie e se é castrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton radioSelecionado = getView().findViewById(radioId);
        String castrado = radioSelecionado.getText().toString();

        btnSalvarEdicaoPet.setEnabled(false);
        btnSalvarEdicaoPet.setText("Salvando...");

        if (uriNovaImagem != null) {
            petRepository.fazerUploadImagemPet(idPetSelecionado, uriNovaImagem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    salvarPetNoBanco(nome, especieFinal, raca, cor, castrado, task.getResult());
                } else {
                    Toast.makeText(getContext(), "Erro no upload da foto.", Toast.LENGTH_SHORT).show();
                    resetarBotao();
                }
            });
        } else {
            salvarPetNoBanco(nome, especieFinal, raca, cor, castrado, urlFotoAtual);
        }
    }

    private void salvarPetNoBanco(String nome, String especie, String raca, String cor, String castrado, String urlFoto) {
        Perfil_pet petAtualizado = new Perfil_pet(idPetSelecionado, nome, especie, raca, cor, castrado, urlFoto);
        petRepository.salvarDadosPet(petAtualizado).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Alterações salvas!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().finish();
            } else {
                Toast.makeText(getContext(), "Erro ao atualizar dados.", Toast.LENGTH_SHORT).show();
                resetarBotao();
            }
        });
    }

    private void resetarBotao() {
        btnSalvarEdicaoPet.setEnabled(true);
        btnSalvarEdicaoPet.setText("Salvar Alterações");
    }
}