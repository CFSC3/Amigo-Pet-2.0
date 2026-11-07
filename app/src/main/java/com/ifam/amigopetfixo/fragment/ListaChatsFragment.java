package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.ChatRecente;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.ChatRecenteAdapter;
import com.ifam.amigopetfixo.repository.ChatRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class ListaChatsFragment extends Fragment {

    private RecyclerView recyclerChats;
    private ChatRecenteAdapter adapter;
    private List<ChatRecente> listaChats = new ArrayList<>();
    private DatabaseReference chatsRecentesRef;
    private String idUsuarioAtual;

    public ListaChatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_chats, container, false);
        recyclerChats = view.findViewById(R.id.recyclerChats);
        recyclerChats.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChatRecenteAdapter(listaChats, getContext(), chat -> confirmarExclusao(chat));
        recyclerChats.setAdapter(adapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            idUsuarioAtual = FirebaseAuth.getInstance().getCurrentUser().getUid();
            chatsRecentesRef = FirebaseDatabase.getInstance().getReference()
                    .child("banco-dados")
                    .child("Conversas")
                    .child(idUsuarioAtual);
            carregarChats();
        }

        return view;
    }

    private void carregarChats() {
        chatsRecentesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaChats.clear();
                for (DataSnapshot dados : snapshot.getChildren()) {
                    ChatRecente chat = dados.getValue(ChatRecente.class);
                    if (chat != null) {
                        listaChats.add(chat);
                    }
                }

                // Organiza do maior timestamp (mais novo) para o menor
                Collections.sort(listaChats, (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void confirmarExclusao(ChatRecente chat) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir este Chat?")
                .setMessage("Tem certeza que deseja excluir esse chat? As mensagens serão perdidas e você não terá mais acesso ao histórico.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    ChatRepository repository = new ChatRepository();
                    repository.excluirChatLogicamente(chat.getIdSalaChat(), idUsuarioAtual, chat.getIdDestinatario(), () -> {
                        listaChats.remove(chat);
                        adapter.notifyDataSetChanged();
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}