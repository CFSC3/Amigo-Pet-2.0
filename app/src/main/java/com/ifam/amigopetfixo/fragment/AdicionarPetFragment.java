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
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.PetRepository;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class AdicionarPetFragment extends Fragment {

    private de.hdodenhof.circleimageview.CircleImageView imgAddFotoPet;
    private TextInputEditText editNomePet, editRaca, editCor, editEspecieOutro;
    private TextInputLayout layoutEspecieOutro;
    private AutoCompleteTextView dropdownEspecie;
    private RadioGroup radioGroupCastrado;
    private MaterialButton btnSalvarPet;

    private Uri uriImagemSelecionada = null;
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

    public AdicionarPetFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adicionar_pet, container, false);

        petRepository = new PetRepository();

        imgAddFotoPet = view.findViewById(R.id.imgAddFotoPet);
        editNomePet = view.findViewById(R.id.editNomePet);
        dropdownEspecie = view.findViewById(R.id.dropdownEspecie);
        layoutEspecieOutro = view.findViewById(R.id.layoutEspecieOutro);
        editEspecieOutro = view.findViewById(R.id.editEspecieOutro);
        editRaca = view.findViewById(R.id.editRaca);
        editCor = view.findViewById(R.id.editCor);
        radioGroupCastrado = view.findViewById(R.id.radioGroupCastrado);
        btnSalvarPet = view.findViewById(R.id.btnSalvarPet);

        configurarDropdownEspecie();
        configurarCliques();

        return view;
    }

    private void configurarDropdownEspecie() {
        String[] especiesComuns = {"Cachorro", "Gato", "Pássaro", "Roedor", "Réptil", "Outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, especiesComuns);
        dropdownEspecie.setAdapter(adapter);

        dropdownEspecie.setOnItemClickListener((parent, view, position, id) -> {
            String selecionado = adapter.getItem(position);
            if ("Outro".equals(selecionado)) {
                layoutEspecieOutro.setVisibility(View.VISIBLE);
            } else {
                layoutEspecieOutro.setVisibility(View.GONE);
                editEspecieOutro.setText("");
            }
        });
    }

    private void configurarCliques() {
        imgAddFotoPet.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            selecionarImagemLauncher.launch(intent);
        });

        btnSalvarPet.setOnClickListener(v -> validarESalvarPet());
    }

    private void iniciarUCrop(@NonNull Uri sourceUri) {
        String nomeArquivo = UUID.randomUUID().toString() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), nomeArquivo));

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setToolbarTitle("Ajustar Foto do Pet");

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
                .start(requireContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK && data != null) {
            uriImagemSelecionada = UCrop.getOutput(data);
            Glide.with(this).load(uriImagemSelecionada).centerCrop().into(imgAddFotoPet);

        } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
            Throwable erro = UCrop.getError(data);
            Toast.makeText(getContext(), "Erro ao cortar foto: " + erro.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validarESalvarPet() {
        String nome = editNomePet.getText().toString().trim();
        String especieSelecionada = dropdownEspecie.getText().toString().trim();
        String raca = editRaca.getText().toString().trim();
        String cor = editCor.getText().toString().trim();

        int radioId = radioGroupCastrado.getCheckedRadioButtonId();

        final String especieFinal;
        if ("Outro".equals(especieSelecionada)) {
            especieFinal = editEspecieOutro.getText().toString().trim();
        } else {
            especieFinal = especieSelecionada;
        }

        if (uriImagemSelecionada == null) {
            Toast.makeText(getContext(), "Por favor, adicione uma foto do seu pet!", Toast.LENGTH_LONG).show();
            return; // Aborta o salvamento imediatamente
        }

        if (nome.isEmpty() || especieFinal.isEmpty() || radioId == -1) {
            Toast.makeText(getContext(), "Preencha o nome, espécie e se é castrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton radioSelecionado = getView().findViewById(radioId);
        String castrado = radioSelecionado.getText().toString();

        btnSalvarPet.setEnabled(false);
        btnSalvarPet.setText("Verificando limite...");

        petRepository.verificarQuantidadePets().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int qtdAtual = task.getResult();
                if (qtdAtual >= 20) {
                    Toast.makeText(getContext(), "Você atingiu o limite máximo de 20 pets!", Toast.LENGTH_LONG).show();
                    btnSalvarPet.setEnabled(true);
                    btnSalvarPet.setText("Salvar Pet");
                } else {
                    processarSalvamento(nome, especieFinal, raca, cor, castrado);
                }
            } else {
                btnSalvarPet.setEnabled(true);
                btnSalvarPet.setText("Salvar Pet");
            }
        });
    }

    private void processarSalvamento(String nome, String especie, String raca, String cor, String castrado) {
        btnSalvarPet.setText("Salvando Pet...");

        String novoIdPet = petRepository.gerarNovoIdPet();

        if (uriImagemSelecionada != null) {
            petRepository.fazerUploadImagemPet(novoIdPet, uriImagemSelecionada).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String urlDaFoto = task.getResult();
                    salvarPetNoBanco(novoIdPet, nome, especie, raca, cor, castrado, urlDaFoto);
                } else {
                    Toast.makeText(getContext(), "Erro ao enviar a foto do pet.", Toast.LENGTH_SHORT).show();
                    btnSalvarPet.setEnabled(true);
                    btnSalvarPet.setText("Salvar Pet");
                }
            });
        } else {
            salvarPetNoBanco(novoIdPet, nome, especie, raca, cor, castrado, "");
        }
    }

    private void salvarPetNoBanco(String idPet, String nome, String especie, String raca, String cor, String castrado, String urlFoto) {
        Perfil_pet novoPet = new Perfil_pet(idPet, nome, especie, raca, cor, castrado, urlFoto);

        petRepository.salvarDadosPet(novoPet).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "Erro ao salvar o Pet.", Toast.LENGTH_SHORT).show();
                btnSalvarPet.setEnabled(true);
                btnSalvarPet.setText("Salvar Pet");
            }
        });
    }
}