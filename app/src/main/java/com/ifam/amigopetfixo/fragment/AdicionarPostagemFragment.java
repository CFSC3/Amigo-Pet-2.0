package com.ifam.amigopetfixo.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.PickVisualMediaRequest;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.PostagemRepository;
import com.ifam.amigopetfixo.repository.UsuarioRepository;

public class AdicionarPostagemFragment extends Fragment {

    private ShapeableImageView imgAvatarAutor;
    private TextView txtUsernameAutor;
    private MaterialCardView cardEscolherMidia;
    private ImageView imgMidiaSelecionada;
    private LinearLayout layoutPlaceholderAddMidia;
    private ImageView iconePlayIndicador;
    private TextInputEditText editLegendaPostagem;
    private MaterialButton btnCancelar, btnPublicar;

    private Uri midiaUriSelecionada;
    private String tipoMidiaSelecionada;
    private final ActivityResultLauncher<PickVisualMediaRequest> abrirGaleria = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    midiaUriSelecionada = uri;
                    identificarEExibirMidia(uri);
                }
            }
    );

    public AdicionarPostagemFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adicionar_postagem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializarComponentes(view);
        configurarDadosDoUsuario();
        configurarCliques();
    }

    private void inicializarComponentes(View view) {
        imgAvatarAutor = view.findViewById(R.id.imgAvatarAutorPost);
        txtUsernameAutor = view.findViewById(R.id.txtUsernameAutorPost);
        cardEscolherMidia = view.findViewById(R.id.cardEscolherMidia);
        imgMidiaSelecionada = view.findViewById(R.id.imgMidiaSelecionada);
        layoutPlaceholderAddMidia = view.findViewById(R.id.layoutPlaceholderAddMidia);
        iconePlayIndicador = view.findViewById(R.id.iconePlayIndicador);
        editLegendaPostagem = view.findViewById(R.id.editLegendaPostagem);
        btnCancelar = view.findViewById(R.id.btnCancelarPostagem);
        btnPublicar = view.findViewById(R.id.btnPublicarPostagem);
    }

    private void configurarDadosDoUsuario() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        UsuarioRepository usuarioRepository = new UsuarioRepository();

        usuarioRepository.buscarPerfilParaCabecalho(currentUserId, new UsuarioRepository.OnPerfilLoadedListener() {
            @Override
            public void onSuccess(String username, String fotoUrl) {
                if (username != null) {
                    txtUsernameAutor.setText("@" + username);
                }

                if (fotoUrl != null && !fotoUrl.equals("default") && isAdded()) {
                    Glide.with(requireContext())
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_avatar_pet)
                            .into(imgAvatarAutor);
                }
            }

            @Override
            public void onError(String erro) {
                // Falha silenciosa ou log de erro
            }
        });
    }

    private void configurarCliques() {
        cardEscolherMidia.setOnClickListener(v -> {
            abrirGaleria.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        });

        btnCancelar.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        btnPublicar.setOnClickListener(v -> {
            validarEIniciarUpload();
        });
    }

    private void identificarEExibirMidia(Uri uri) {

        String mimeType = requireContext().getContentResolver().getType(uri);

        if (mimeType != null && mimeType.startsWith("video/")) {
            tipoMidiaSelecionada = "video";
            iconePlayIndicador.setVisibility(View.VISIBLE); // Mostra o Play!
        } else {
            tipoMidiaSelecionada = "foto";
            iconePlayIndicador.setVisibility(View.GONE); // Esconde o Play
        }

        layoutPlaceholderAddMidia.setVisibility(View.GONE);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(imgMidiaSelecionada);
    }

    private void validarEIniciarUpload() {
        String legenda = editLegendaPostagem.getText().toString().trim();

        if (midiaUriSelecionada == null && legenda.isEmpty()) {
            return;
        }

        btnPublicar.setEnabled(false);
        btnPublicar.setText("Publicando...");
        btnCancelar.setEnabled(false);

        Postagem novaPostagem = new Postagem();
        novaPostagem.setTextoPostagem(legenda);
        novaPostagem.setUsername("teste");

        PostagemRepository repository = new PostagemRepository();

        repository.fazerUploadECriarPostagem(novaPostagem, midiaUriSelecionada, tipoMidiaSelecionada, new PostagemRepository.OnPostagemCompleteListener() {
            @Override
            public void onSuccess() {
                requireActivity().finish();
            }

            @Override
            public void onFailure(String erro) {
                Toast.makeText(getContext(), erro, Toast.LENGTH_LONG).show();
                btnPublicar.setEnabled(true);
                btnPublicar.setText("Publicar");
                btnCancelar.setEnabled(true);
            }
        });
    }

}
