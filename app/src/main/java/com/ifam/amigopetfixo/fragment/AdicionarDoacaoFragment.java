package com.ifam.amigopetfixo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.storage.UploadTask;
import com.ifam.amigopetfixo.Model.bean.Adocoes;
import com.ifam.amigopetfixo.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AdicionarDoacaoFragment extends Fragment {

    private FirebaseAuth usuario = FirebaseAuth.getInstance();
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private ImageView foto;
    private TextInputEditText titulo, dscricao, endereco, bairro, contato;
    private ChipGroup chipGroupCategoria;
    private MaterialButton cancelar, salvar;
    private Random random = new Random();

    private FusedLocationProviderClient fusedLocationClient;
    private String estadoDetectado = "Amazonas";

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    foto.setImageBitmap(imageBitmap);
                }
            });

    private final ActivityResultLauncher<String> galeriaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        foto.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show();
                    }
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
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(takePictureIntent);
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
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        } catch (SecurityException e) {
        }
    }

    private void configurarBotoes() {
        cancelar.setOnClickListener(v -> requireActivity().onBackPressed());
        salvar.setOnClickListener(v -> salvarDoacao());
    }

    private void salvarDoacao() {
        int chipSelecionadoId = chipGroupCategoria.getCheckedChipId();

        if (titulo.getText().toString().trim().isEmpty() ||
                dscricao.getText().toString().trim().isEmpty() ||
                contato.getText().toString().trim().isEmpty() ||
                bairro.getText().toString().trim().isEmpty() ||
                endereco.getText().toString().trim().isEmpty() ||
                chipSelecionadoId == View.NO_ID) {

            Toast.makeText(getContext(), "Preencha todos os campos e selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return;
        }

        Chip chipSelecionado = getView().findViewById(chipSelecionadoId);
        String categoriaTexto = chipSelecionado.getText().toString();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        String dataAtual = dateFormat.format(new Date());
        String idImagem = String.valueOf(Long.toHexString(random.nextLong()));

        Adocoes adocoes = new Adocoes();
        adocoes.setTitulo(titulo.getText().toString());
        adocoes.setDescricao(dscricao.getText().toString());
        adocoes.setEndereco(endereco.getText().toString());
        adocoes.setBairro(bairro.getText().toString());
        adocoes.setCategoria(categoriaTexto);
        adocoes.setContato(contato.getText().toString());
        adocoes.setData_post(dataAtual);

        String tituloKey = titulo.getText().toString().replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_");

        DatabaseReference usuarios = referencia.child("banco-dados");
        DatabaseReference doacaoRef = usuarios.child("adocoesDoacoes").child(tituloKey);

        doacaoRef.setValue(adocoes);
        doacaoRef.child("imagem").setValue(idImagem);
        doacaoRef.child("usuarioID").setValue(usuario.getUid());
        doacaoRef.child("estado").setValue(estadoDetectado);

        usuarios.child("usuario").child(usuario.getUid()).child("adocoesDoacoes").child(tituloKey).setValue(true);

        salvarImagemStorage(idImagem);

    }

    private void salvarImagemStorage(String nomeImagem) {
        foto.setDrawingCacheEnabled(true);
        foto.buildDrawingCache();
        Bitmap bitmap = foto.getDrawingCache();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 75, baos);
        byte[] dadosImagem = baos.toByteArray();

        StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                .child("BancoImagens").child("DoacoesAdocoes").child(nomeImagem + ".png");

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            if (isAdded()) {
                requireActivity().onBackPressed();
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Erro ao enviar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}