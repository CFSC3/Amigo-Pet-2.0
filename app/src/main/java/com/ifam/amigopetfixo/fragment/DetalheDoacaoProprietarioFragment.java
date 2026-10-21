package com.ifam.amigopetfixo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.DoacaoRepository;

public class DetalheDoacaoProprietarioFragment extends Fragment {

    private String idDoacaoSelecionada;
    private String nomeImagemAtual;
    private String currentUserId;
    private DoacaoRepository repository;

    private TextView titulo, descricao, categoria, data, bairro, endereco, contato;
    private ImageView foto;
    private ImageButton btnVoltar, btnEditar, btnExcluir;

    public DetalheDoacaoProprietarioFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalhe_doacao_proprietario, container, false);

        if (getArguments() != null) {
            idDoacaoSelecionada = getArguments().getString("ID_ITEM");
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = new DoacaoRepository();

        bairro = view.findViewById(R.id.textBairroVizualizarProprietario);
        endereco = view.findViewById(R.id.textEndrecoVizualizarProprietario);
        contato = view.findViewById(R.id.textContatoVizualizarProprietario);

        inicializarViews(view);
        carregarDados();
        configurarBotoesAcao();

        return view;
    }

    private void inicializarViews(View view) {
        titulo = view.findViewById(R.id.textTituloVizualizarProprietario);
        descricao = view.findViewById(R.id.textDescricaoVizualizarProprietario);
        categoria = view.findViewById(R.id.textCategoriaVizualizarProprietario);
        data = view.findViewById(R.id.textDataProprietario);
        foto = view.findViewById(R.id.imageVizualizarAdProprietario);

        btnVoltar = view.findViewById(R.id.btnVoltarDoacao);
        btnEditar = view.findViewById(R.id.btnEditarDoacao);
        btnExcluir = view.findViewById(R.id.btnExcluirDoacao);
    }

    private void carregarDados() {
        DatabaseReference doacaoRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("adocoesDoacoes").child(idDoacaoSelecionada);
        doacaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    titulo.setText(snapshot.child("titulo").getValue(String.class));
                    descricao.setText(snapshot.child("descricao").getValue(String.class));
                    categoria.setText(snapshot.child("categoria").getValue(String.class));
                    data.setText("Publicado em: " + snapshot.child("data_post").getValue(String.class));
                    nomeImagemAtual = snapshot.child("imagem").getValue(String.class);
                    bairro.setText("📍 Bairro: " + snapshot.child("bairro").getValue(String.class));
                    endereco.setText("🏠 Endereço: " + snapshot.child("endereco").getValue(String.class));
                    contato.setText("📱 Contato: " + snapshot.child("contato").getValue(String.class));

                    baixarImagem(nomeImagemAtual);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void baixarImagem(String nomeImagem) {
        if (nomeImagem == null || nomeImagem.equals("Não informado")) return;
        com.google.firebase.storage.StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                .child("BancoImagens").child("DoacoesAdocoes").child(nomeImagem + ".png");

        imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (isAdded()) Glide.with(this).load(uri).centerCrop().into(foto);
        });
    }

    private void configurarBotoesAcao() {
        btnVoltar.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().finish();
        });

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.ifam.amigopetfixo.activity.UsuarioHubActivity.class);
            intent.putExtra("Acao", "EditarDoacao");
            intent.putExtra("ID_ITEM", idDoacaoSelecionada);
            startActivity(intent);
        });

        btnExcluir.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Excluir Doação/Adoção")
                .setMessage("Tem certeza que deseja excluir esta publicação? Esta ação apagará a foto e os dados permanentemente.")
                .setPositiveButton("Excluir", (dialog, which) -> processarExclusao())
                .setNegativeButton("Cancelar", null)
                .show());
    }

    private void processarExclusao() {
        repository.excluirDoacaoEmCascata(idDoacaoSelecionada, currentUserId, nomeImagemAtual, new DoacaoRepository.OnDoacaoCompleteListener() {
            @Override
            public void onSuccess() {
                if(getContext() != null) Toast.makeText(getContext(), "Excluído com sucesso.", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().finish();
            }

            @Override
            public void onFailure(String erro) {
                if(getContext() != null) Toast.makeText(getContext(), "Erro: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }
}