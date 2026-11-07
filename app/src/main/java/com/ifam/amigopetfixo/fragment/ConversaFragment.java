package com.ifam.amigopetfixo.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.Model.bean.Mensagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.MensagemAdapter;
import com.ifam.amigopetfixo.repository.ChatRepository;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import com.ifam.amigopetfixo.helper.AppStatus;

public class ConversaFragment extends Fragment {

    private RecyclerView recyclerMensagens;
    private EditText editMensagem;
    private ImageButton btnEnviarMensagem, btnAnexarMidia;
    private ImageView imageFundoChat;

    // Componentes de Resposta (Citacao)
    private LinearLayout layoutCitacao;
    private TextView textCitacaoAutor, textCitacaoConteudo;
    private ImageButton btnFecharCitacao;
    private Mensagem mensagemCitadaEmEspera = null;

    private String idUsuarioAtual;
    private String idDestinatario;
    private String idSalaChat;

    private Uri uriCameraSelecionada;
    private String tipoCamera;
    private ActivityResultLauncher<android.content.Intent> cameraLauncher;

    private MensagemAdapter adapter;
    private List<Mensagem> listaMensagens = new ArrayList<>();
    private ActivityResultLauncher<PickVisualMediaRequest> seletorMidiaLauncher;

    private ChatRepository repository;

    public ConversaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversa, container, false);
        repository = new ChatRepository();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            idUsuarioAtual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (getArguments() != null) {
            idDestinatario = getArguments().getString("uidDestino");
        }

        idSalaChat = gerarIdSala(idUsuarioAtual, idDestinatario);

        seletorMidiaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null && getContext() != null) {
                String mimeType = getContext().getContentResolver().getType(uri);
                String tipo = (mimeType != null && mimeType.startsWith("video")) ? "video" : "imagem";
                repository.enviarMidia(idSalaChat, idUsuarioAtual, idDestinatario, uri, tipo, mensagemCitadaEmEspera);
                cancelarCitacao(); // Limpa a barra após enviar
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && uriCameraSelecionada != null) {
                repository.enviarMidia(idSalaChat, idUsuarioAtual, idDestinatario, uriCameraSelecionada, tipoCamera, mensagemCitadaEmEspera);
                cancelarCitacao();
            }
        });

        inicializarUI(view);
        configurarListeners();
        iniciarObservadorMensagens();

        return view;
    }

    private void inicializarUI(View view) {
        recyclerMensagens = view.findViewById(R.id.recyclerMensagens);
        editMensagem = view.findViewById(R.id.editMensagem);
        btnEnviarMensagem = view.findViewById(R.id.btnEnviarMensagem);
        btnAnexarMidia = view.findViewById(R.id.btnAnexarMidia);
        imageFundoChat = view.findViewById(R.id.imageFundoChat);

        layoutCitacao = view.findViewById(R.id.layoutCitacao);
        textCitacaoAutor = view.findViewById(R.id.textCitacaoAutor);
        textCitacaoConteudo = view.findViewById(R.id.textCitacaoConteudo);
        btnFecharCitacao = view.findViewById(R.id.btnFecharCitacao);

        // Carrega o Wallpaper do arquivo local salvo no armazenamento interno do app
        if (getContext() != null) {
            File arquivoWallpaper = new File(getContext().getFilesDir(), "custom_chat_wallpaper");
            if (arquivoWallpaper.exists()) {
                Glide.with(this)
                        .load(arquivoWallpaper)
                        .centerCrop()
                        .into(imageFundoChat);
            } else {
                imageFundoChat.setImageDrawable(null); // Limpa caso tenha sido removido
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerMensagens.setLayoutManager(layoutManager);

        adapter = new MensagemAdapter(listaMensagens, getContext(), idUsuarioAtual, mensagem -> prepararCitacao(mensagem));
        recyclerMensagens.setAdapter(adapter);
    }

    private void prepararCitacao(Mensagem mensagem) {
        mensagemCitadaEmEspera = mensagem;
        layoutCitacao.setVisibility(View.VISIBLE);
        textCitacaoAutor.setText(mensagem.getIdRemetente().equals(idUsuarioAtual) ? "Você" : "Outro");
        textCitacaoConteudo.setText(mensagem.getTexto() != null && !mensagem.getTexto().isEmpty() ? mensagem.getTexto() : "Mídia");
    }

    private void cancelarCitacao() {
        mensagemCitadaEmEspera = null;
        layoutCitacao.setVisibility(View.GONE);
    }

    private void configurarListeners() {
        btnEnviarMensagem.setOnClickListener(v -> {
            String texto = editMensagem.getText().toString().trim();
            if (!texto.isEmpty() && idUsuarioAtual != null && idDestinatario != null) {
                repository.enviarMensagemTexto(idSalaChat, idUsuarioAtual, idDestinatario, texto, mensagemCitadaEmEspera);
                editMensagem.setText("");
                cancelarCitacao();
            }
        });

        btnAnexarMidia.setOnClickListener(v -> mostrarOpcoesMidia());
        btnFecharCitacao.setOnClickListener(v -> cancelarCitacao());
    }

    private void abrirSeletorDeMidia() {
        seletorMidiaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void mostrarOpcoesMidia() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());

        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_anexos, null);
        bottomSheetDialog.setContentView(view);

        view.findViewById(R.id.btnOpcaoFoto).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            abrirCamera("imagem");
        });

        view.findViewById(R.id.btnOpcaoVideo).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            abrirCamera("video");
        });

        view.findViewById(R.id.btnOpcaoGaleria).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            abrirSeletorDeMidia();
        });

        bottomSheetDialog.show();
    }

    private void abrirCamera(String tipo) {
        tipoCamera = tipo;
        android.content.ContentValues values = new android.content.ContentValues();
        android.content.Intent intent;

        if (tipo.equals("imagem")) {
            values.put(android.provider.MediaStore.Images.Media.TITLE, "Nova Foto Chat");
            uriCameraSelecionada = requireContext().getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        } else {
            values.put(android.provider.MediaStore.Video.Media.TITLE, "Novo Video Chat");
            uriCameraSelecionada = requireContext().getContentResolver().insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            intent = new android.content.Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

            intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
        }

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uriCameraSelecionada);

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "Nenhum aplicativo de câmera encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarObservadorMensagens() {
        if (idSalaChat.isEmpty()) return;

        repository.ouvirMensagens(idSalaChat, idUsuarioAtual, new ChatRepository.MensagemCallback() {
            @Override
            public void onMensagemRecebida(Mensagem mensagem) {
                listaMensagens.add(mensagem);
                adapter.notifyItemInserted(listaMensagens.size() - 1);
                recyclerMensagens.scrollToPosition(listaMensagens.size() - 1);
            }

            @Override
            public void onErro(String erro) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Erro: " + erro, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_conversa, menu);
            }
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_excluir_chat) {
                    confirmarExclusaoInterna();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void confirmarExclusaoInterna() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir Chat")
                .setMessage("Você perderá o acesso a esta conversa. Deseja continuar?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    repository.excluirChatLogicamente(idSalaChat, idUsuarioAtual, idDestinatario, () -> {
                        if (getActivity() != null) getActivity().onBackPressed();
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String gerarIdSala(String uid1, String uid2) {
        if (uid1 == null || uid2 == null) return "";
        return (uid1.compareTo(uid2) < 0) ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        repository.removerListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppStatus.idChatAberto = idDestinatario;

        if (getContext() != null && idDestinatario != null) {
            NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(idDestinatario.hashCode());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AppStatus.idChatAberto = null;
    }
}