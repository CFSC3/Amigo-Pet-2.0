package com.ifam.amigopetfixo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.Model.bean.Adocoes;
import com.ifam.amigopetfixo.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AdicionarDoacaoFragment extends Fragment {

    private FirebaseAuth usuario = FirebaseAuth.getInstance();
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private ImageView foto;
    private TextInputEditText titulo, dscricao, endereco, cidade, bairro, contato;
    private ChipGroup chipGroupCategoria;
    private MaterialButton cancelar, salvar;
    private Random random = new Random();

    private FusedLocationProviderClient fusedLocationClient;
    private String estadoDetectado = "";
    private String cidadeDetectada = "";

    private Uri imagemUriSelecionada;
    private Bitmap imagemBitmapCapturada;
    private Uri imagemUriCamera;

    private final ActivityResultLauncher<android.net.Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), sucesso -> {
                if (sucesso && imagemUriCamera != null) {
                    imagemUriSelecionada = imagemUriCamera;
                    imagemBitmapCapturada = null;
                    foto.setImageURI(imagemUriSelecionada);
                }
            });

    private final ActivityResultLauncher<String> galeriaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagemUriSelecionada = uri;
                    imagemBitmapCapturada = null;
                    foto.setImageURI(uri);
                }
            });

    public AdicionarDoacaoFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adicionar_doacao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foto = view.findViewById(R.id.imageViewDoacao);
        titulo = view.findViewById(R.id.editTextTitulo);
        dscricao = view.findViewById(R.id.editTextDescricao);
        endereco = view.findViewById(R.id.editTextEndereco);
        cidade = view.findViewById(R.id.editTextCidade);
        bairro = view.findViewById(R.id.editTextBairro);
        contato = view.findViewById(R.id.editTextContato);
        chipGroupCategoria = view.findViewById(R.id.chipGroupCategoria);
        cancelar = view.findViewById(R.id.btnCancelarDoacao);
        salvar = view.findViewById(R.id.btnSalvarDoacao);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        obterEstadoSilenciosamente();

        if (getArguments() != null) {
            int opcao = getArguments().getInt("opcao", 0);
            if (opcao == 1) {
                galeriaLauncher.launch("image/*");
            } else if (opcao == 2) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.Images.Media.TITLE, "Foto_Doacao");
                imagemUriCamera = requireContext().getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                cameraLauncher.launch(imagemUriCamera);
            }
        }

        configurarBotoes();
    }

    private void obterEstadoSilenciosamente() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), new java.util.Locale("pt", "BR"));
                    try {
                        java.util.List<android.location.Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            estadoDetectado = addresses.get(0).getAdminArea();

                            String local = addresses.get(0).getSubAdminArea();
                            if (local == null) local = addresses.get(0).getLocality();

                            if (local != null) {
                                cidadeDetectada = local;
                                cidade.setText(cidadeDetectada);
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        } catch (SecurityException e) {}
    }

    private void configurarBotoes() {
        cancelar.setOnClickListener(v -> requireActivity().onBackPressed());
        salvar.setOnClickListener(v -> salvarDoacao());
    }

    private void salvarDoacao() {
        int chipSelecionadoId = chipGroupCategoria.getCheckedChipId();
        String cidadeDigitada = cidade.getText().toString().trim();

        if (titulo.getText().toString().trim().isEmpty() ||
                dscricao.getText().toString().trim().isEmpty() ||
                contato.getText().toString().trim().isEmpty() ||
                bairro.getText().toString().trim().isEmpty() ||
                endereco.getText().toString().trim().isEmpty() ||
                chipSelecionadoId == View.NO_ID) {

            Toast.makeText(getContext(), "Preencha todos os campos e selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação Antifraude de Localização
        if (!cidadeDetectada.isEmpty() && !cidadeDigitada.equalsIgnoreCase(cidadeDetectada)) {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Localização Incompatível")
                    .setMessage("Opa! Você não se encontra nesta cidade. Para garantir a segurança das entregas, você só pode registrar doações na sua cidade atual (" + cidadeDetectada + ").")
                    .setPositiveButton("Entendi", null)
                    .show();
            return;
        }

        salvar.setEnabled(false);
        cancelar.setEnabled(false);
        salvar.setText("Doando...");

        Chip chipSelecionado = getView().findViewById(chipSelecionadoId);
        String categoriaTexto = chipSelecionado.getText().toString();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        String dataAtual = dateFormat.format(new Date());
        String idImagem = String.valueOf(Long.toHexString(random.nextLong()));

        Adocoes adocoes = new Adocoes();
        adocoes.setTitulo(titulo.getText().toString());
        adocoes.setDescricao(dscricao.getText().toString());
        adocoes.setEndereco(endereco.getText().toString());
        adocoes.setCidade(cidadeDigitada);
        adocoes.setBairro(bairro.getText().toString());
        adocoes.setCategoria(categoriaTexto);
        adocoes.setContato(contato.getText().toString());
        adocoes.setData_post(dataAtual);

        String tituloKey = titulo.getText().toString().replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_");

        DatabaseReference usuarios = referencia.child("banco-dados");
        DatabaseReference doacaoRef = usuarios.child("adocoesDoacoes").child(tituloKey);

        doacaoRef.setValue(adocoes).addOnSuccessListener(aVoid -> {
            doacaoRef.child("imagem").setValue(idImagem);
            doacaoRef.child("usuarioID").setValue(usuario.getUid());
            doacaoRef.child("estado").setValue(estadoDetectado);
            doacaoRef.child("cidade").setValue(cidadeDigitada);
            usuarios.child("usuario").child(usuario.getUid()).child("adocoesDoacoes").child(tituloKey).setValue(true);

            salvarImagemStorage(idImagem);
        }).addOnFailureListener(e -> {
            salvar.setEnabled(true);
            cancelar.setEnabled(true);
            salvar.setText("Doar");
            Toast.makeText(getContext(), "Erro de conexão.", Toast.LENGTH_SHORT).show();
        });
    }

    private void salvarImagemStorage(String nomeImagem) {
        StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                .child("BancoImagens").child("DoacoesAdocoes").child(nomeImagem + ".png");

        if (imagemUriSelecionada != null) {
            imagemRef.putFile(imagemUriSelecionada)
                    .addOnSuccessListener(taskSnapshot -> {
                        if (isAdded()) requireActivity().onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                if (isAdded()) {
                    salvar.setEnabled(true);
                    cancelar.setEnabled(true);
                    salvar.setText("Doar");
                    Toast.makeText(getContext(), "Erro ao enviar imagem", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (imagemBitmapCapturada != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imagemBitmapCapturada.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] dadosImagem = baos.toByteArray();

            imagemRef.putBytes(dadosImagem)
                    .addOnSuccessListener(taskSnapshot -> {
                        if (isAdded()) requireActivity().onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) Toast.makeText(getContext(), "Erro ao enviar da câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            if (isAdded()) requireActivity().onBackPressed();
        }
    }
}