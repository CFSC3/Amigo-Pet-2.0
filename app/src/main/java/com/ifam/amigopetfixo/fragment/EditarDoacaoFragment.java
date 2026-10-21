package com.ifam.amigopetfixo.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.Model.bean.Adocoes;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.DoacaoRepository;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditarDoacaoFragment extends Fragment {

    private String idDoacaoSelecionada;
    private String currentUserId;
    private String nomeImagemAtual;
    private Uri imagemUriSelecionada;
    private Bitmap imagemBitmapCapturada;

    private DoacaoRepository repository;

    private TextInputEditText editTitulo, editContato, editBairro, editEndereco, editDescricao;
    private ChipGroup chipGroupCategoria;
    private ShapeableImageView imgMidiaEditar;
    private ImageButton btnVoltar;
    private MaterialButton btnSalvar;

    private final ActivityResultLauncher<Intent> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imagemUriSelecionada = result.getData().getData();
                    imagemBitmapCapturada = null;
                    imgMidiaEditar.setImageURI(imagemUriSelecionada);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    imagemBitmapCapturada = (Bitmap) extras.get("data");
                    imagemUriSelecionada = null; // Limpa a URI da galeria
                    imgMidiaEditar.setImageBitmap(imagemBitmapCapturada);
                }
            }
    );

    public EditarDoacaoFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_doacao, container, false);

        if (getArguments() != null) {
            idDoacaoSelecionada = getArguments().getString("ID_ITEM");
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = new DoacaoRepository();

        inicializarViews(view);

        if (idDoacaoSelecionada != null) {
            carregarDadosAtuais();
            configurarBotoes();
        } else {
            if (getActivity() != null) getActivity().finish();
        }

        return view;
    }

    private void inicializarViews(View view) {
        editTitulo = view.findViewById(R.id.editTituloDoacao);
        editDescricao = view.findViewById(R.id.editDescricaoDoacao);
        chipGroupCategoria = view.findViewById(R.id.chipGroupCategoria);
        editEndereco = view.findViewById(R.id.editEnderecoDoacao);
        editBairro = view.findViewById(R.id.editBairroDoacao);
        editContato = view.findViewById(R.id.editContatoDoacao);

        imgMidiaEditar = view.findViewById(R.id.imgMidiaEditarDoacao);
        btnVoltar = view.findViewById(R.id.btnVoltarEditarDoacao);
        btnSalvar = view.findViewById(R.id.btnSalvarEdicaoDoacao);
    }

    private void carregarDadosAtuais() {
        DatabaseReference doacaoRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("adocoesDoacoes").child(idDoacaoSelecionada);
        doacaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Adocoes adocao = snapshot.getValue(Adocoes.class);
                    if (adocao != null) {
                        editTitulo.setText(adocao.getTitulo());
                        editDescricao.setText(adocao.getDescricao());
                        editEndereco.setText(adocao.getEndereco());
                        editBairro.setText(adocao.getBairro());
                        editContato.setText(adocao.getContato());
                        nomeImagemAtual = adocao.getImagem();

                        marcarCategoriaNoChipGroup(adocao.getCategoria());

                        if (nomeImagemAtual != null && !nomeImagemAtual.equals("Não informado")) {
                            StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                                    .child("BancoImagens").child("DoacoesAdocoes").child(nomeImagemAtual + ".png");

                            imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                if (isAdded()) {
                                    Glide.with(EditarDoacaoFragment.this)
                                            .load(uri)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .centerCrop()
                                            .into(imgMidiaEditar);
                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void marcarCategoriaNoChipGroup(String categoriaSalva) {
        if (categoriaSalva == null) return;
        for (int i = 0; i < chipGroupCategoria.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategoria.getChildAt(i);
            if (chip.getText().toString().equals(categoriaSalva)) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void configurarBotoes() {
        btnVoltar.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().finish();
        });

        imgMidiaEditar.setOnClickListener(v -> mostrarDialogoEscolhaImagem());

        btnSalvar.setOnClickListener(v -> salvarAlteracoes());
    }

    private void mostrarDialogoEscolhaImagem() {
        String[] opcoes = {"Tirar Foto", "Escolher da Galeria"};
        new AlertDialog.Builder(getContext())
                .setTitle("Alterar Imagem")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        // Câmera
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(takePictureIntent);
                    } else if (which == 1) {
                        // Galeria
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galeriaLauncher.launch(pickPhotoIntent);
                    }
                })
                .show();
    }

    private void salvarAlteracoes() {
        btnSalvar.setEnabled(false);
        btnSalvar.setText("Salvando...");

        String categoriaSelecionada = "Geral";
        int chipId = chipGroupCategoria.getCheckedChipId();
        if (chipId != View.NO_ID) {
            Chip chip = getView().findViewById(chipId);
            categoriaSelecionada = chip.getText().toString();
        }

        Map<String, Object> atualizacoes = new HashMap<>();
        atualizacoes.put("titulo", editTitulo.getText().toString().trim());
        atualizacoes.put("descricao", editDescricao.getText().toString().trim());
        atualizacoes.put("categoria", categoriaSelecionada);
        atualizacoes.put("endereco", editEndereco.getText().toString().trim());
        atualizacoes.put("bairro", editBairro.getText().toString().trim());
        atualizacoes.put("contato", editContato.getText().toString().trim());

        if (imagemUriSelecionada != null || imagemBitmapCapturada != null) {
            String idParaSalvar = (nomeImagemAtual != null && !nomeImagemAtual.equals("Não informado"))
                    ? nomeImagemAtual : UUID.randomUUID().toString();

            atualizacoes.put("imagem", idParaSalvar);

            StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                    .child("BancoImagens").child("DoacoesAdocoes").child(idParaSalvar + ".png");

            // Faz upload dependendo da origem da foto
            if (imagemUriSelecionada != null) {
                imagemRef.putFile(imagemUriSelecionada).addOnSuccessListener(taskSnapshot -> atualizarBancoDeDados(atualizacoes))
                        .addOnFailureListener(e -> restaurarBotaoSalvar("Erro no upload da galeria"));
            } else if (imagemBitmapCapturada != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagemBitmapCapturada.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();
                imagemRef.putBytes(data).addOnSuccessListener(taskSnapshot -> atualizarBancoDeDados(atualizacoes))
                        .addOnFailureListener(e -> restaurarBotaoSalvar("Erro no upload da câmera"));
            }
        } else {
            atualizarBancoDeDados(atualizacoes);
        }
    }

    private void atualizarBancoDeDados(Map<String, Object> atualizacoes) {
        repository.atualizarInformacoesDoacao(idDoacaoSelecionada, currentUserId, atualizacoes, new DoacaoRepository.OnDoacaoCompleteListener() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) getActivity().finish();
            }

            @Override
            public void onFailure(String erro) {
                restaurarBotaoSalvar("Erro: " + erro);
            }
        });
    }

    private void restaurarBotaoSalvar(String mensagemErro) {
        if (getContext() != null) Toast.makeText(getContext(), mensagemErro, Toast.LENGTH_SHORT).show();
        btnSalvar.setEnabled(true);
        btnSalvar.setText("Salvar");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }
}