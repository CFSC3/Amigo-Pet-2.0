package com.ifam.amigopetfixo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.R;

public class EditarPerfilFragment extends Fragment {

    private TextInputEditText editNome, editDescricao;
    private ImageView editImageCapa;
    private de.hdodenhof.circleimageview.CircleImageView editImagePerfil;
    private MaterialButton btnSalvarEdicao;

    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String uid;

    // Se o usuário não trocar a foto, essas variáveis continuam null e nada é modificado no Storage.
    private Uri uriImagemPerfilSelecionada = null;
    private Uri uriImagemCapaSelecionada = null;
    private boolean isSelecionandoCapa = false;
    private int uploadsPendentes = 0;

    private final ActivityResultLauncher<Intent> selecionarImagemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imagemUri = result.getData().getData();
                    if (imagemUri != null) {
                        if (isSelecionandoCapa) {
                            uriImagemCapaSelecionada = imagemUri;
                            Glide.with(this).load(imagemUri).centerCrop().into(editImageCapa);
                        } else {
                            uriImagemPerfilSelecionada = imagemUri;
                            Glide.with(this).load(imagemUri).centerCrop().into(editImagePerfil);
                        }
                    }
                }
            }
    );

    public EditarPerfilFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_perfil, container, false);

        editNome = view.findViewById(R.id.editNome);
        editDescricao = view.findViewById(R.id.editDescricao);
        editImageCapa = view.findViewById(R.id.editImageCapa);
        editImagePerfil = view.findViewById(R.id.editImagePerfil);
        btnSalvarEdicao = view.findViewById(R.id.btnSalvarEdicao);

        uid = auth.getCurrentUser().getUid();

        carregarDadosAtuais();
        configurarCliques();

        return view;
    }

    private void carregarDadosAtuais() {
        DatabaseReference usuarioRef = referencia.child("banco-dados").child("usuario").child(uid);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nome = snapshot.child("nome").getValue(String.class);
                    String descricao = snapshot.child("texto").getValue(String.class);
                    String linkPerfil = snapshot.child("imageUsuario").getValue(String.class);
                    String linkCapa = snapshot.child("imageCapaUsuario").getValue(String.class);

                    if (nome != null) editNome.setText(nome);
                    if (descricao != null) editDescricao.setText(descricao);

                    if (linkPerfil != null && linkPerfil.startsWith("http")) {
                        Glide.with(EditarPerfilFragment.this).load(linkPerfil).into(editImagePerfil);
                    }
                    if (linkCapa != null && linkCapa.startsWith("http")) {
                        Glide.with(EditarPerfilFragment.this).load(linkCapa).into(editImageCapa);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void configurarCliques() {
        editImagePerfil.setOnClickListener(v -> abrirGaleria(false));
        editImageCapa.setOnClickListener(v -> abrirGaleria(true));

        btnSalvarEdicao.setOnClickListener(v -> {
            btnSalvarEdicao.setEnabled(false);
            btnSalvarEdicao.setText("Salvando...");
            salvarDadosNoBanco();
        });
    }

    private void abrirGaleria(boolean paraCapa) {
        isSelecionandoCapa = paraCapa;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selecionarImagemLauncher.launch(intent);
    }

    private void salvarDadosNoBanco() {
        DatabaseReference usuarioRef = referencia.child("banco-dados").child("usuario").child(uid);

        // Atualiza apenas os textos
        usuarioRef.child("nome").setValue(editNome.getText().toString());
        usuarioRef.child("texto").setValue(editDescricao.getText().toString());

        uploadsPendentes = 0;

        if (uriImagemPerfilSelecionada != null) uploadsPendentes++;
        if (uriImagemCapaSelecionada != null) uploadsPendentes++;

        // Se não houver fotos para subir, finaliza imediatamente
        if (uploadsPendentes == 0) {
            finalizarSalvamento();
            return;
        }

        if (uriImagemPerfilSelecionada != null) {
            fazerUploadImagem(uriImagemPerfilSelecionada, "perfil", "imageUsuario", usuarioRef);
        }
        if (uriImagemCapaSelecionada != null) {
            fazerUploadImagem(uriImagemCapaSelecionada, "capa", "imageCapaUsuario", usuarioRef);
        }
    }

    private void fazerUploadImagem(Uri imagemUri, String pasta, String campoBanco, DatabaseReference usuarioRef) {
        StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                .child("imagens").child(pasta).child(uid + ".jpg");

        imagemRef.putFile(imagemUri).addOnSuccessListener(taskSnapshot -> {
            imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                usuarioRef.child(campoBanco).setValue(uri.toString());
                verificarFimUpload();
            });
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erro ao salvar foto de " + pasta, Toast.LENGTH_SHORT).show();
            }
            verificarFimUpload();
        });
    }

    private void verificarFimUpload() {
        uploadsPendentes--;
        if (uploadsPendentes <= 0) {
            finalizarSalvamento();
        }
    }

    private void finalizarSalvamento() {
        if (!isAdded() || getContext() == null) return;

        Toast.makeText(getContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}