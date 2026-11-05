package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.ArrayList;
import java.util.List;

public class ConversaFragment extends Fragment {

    private RecyclerView recyclerMensagens;
    private EditText editMensagem;
    private ImageButton btnEnviarMensagem, btnAnexarMidia;

    private String idUsuarioAtual;
    private String idDestinatario;
    private String idSalaChat;

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
                repository.enviarMidia(idSalaChat, idUsuarioAtual, idDestinatario, uri, tipo);
            }
        });

        // Marca o chat como lido ao abrir a tela
        if (idUsuarioAtual != null && idSalaChat != null && !idSalaChat.isEmpty()) {
            FirebaseDatabase.getInstance().getReference()
                    .child("banco-dados").child("Conversas")
                    .child(idUsuarioAtual).child(idSalaChat)
                    .child("lida").setValue(true);
        }

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerMensagens.setLayoutManager(layoutManager);

        adapter = new MensagemAdapter(listaMensagens, getContext(), idUsuarioAtual);
        recyclerMensagens.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnEnviarMensagem.setOnClickListener(v -> {
            String texto = editMensagem.getText().toString().trim();
            if (!texto.isEmpty() && idUsuarioAtual != null && idDestinatario != null) {
                repository.enviarMensagemTexto(idSalaChat, idUsuarioAtual, idDestinatario, texto);
                editMensagem.setText("");
            }
        });

        btnAnexarMidia.setOnClickListener(v -> abrirSeletorDeMidia());
    }

    private void abrirSeletorDeMidia() {
        seletorMidiaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void iniciarObservadorMensagens() {
        if (idSalaChat.isEmpty()) return;

        repository.ouvirMensagens(idSalaChat, new ChatRepository.MensagemCallback() {
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
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
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
}