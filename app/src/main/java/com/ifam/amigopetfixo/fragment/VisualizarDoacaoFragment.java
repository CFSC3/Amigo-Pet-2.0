package com.ifam.amigopetfixo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.R;

public class VisualizarDoacaoFragment extends Fragment {

    private DatabaseReference usuariosRef;
    private TextView titulo, descricao, categoria, endereco, bairro, contato, data;
    private ImageView foto;
    private FloatingActionButton fbWhatsapp;

    private String numeroContatoFormatado = "";

    public VisualizarDoacaoFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visualizar_doacao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titulo = view.findViewById(R.id.textTituloVizualizar);
        descricao = view.findViewById(R.id.textDescricaoVizualizar);
        categoria = view.findViewById(R.id.textCategoriaVizualizar);
        endereco = view.findViewById(R.id.textEndrecoVizualizar);
        bairro = view.findViewById(R.id.textBairroVizualizar);
        contato = view.findViewById(R.id.textContatoVizualizar);
        data = view.findViewById(R.id.textData);
        foto = view.findViewById(R.id.imageVizualizarAd);
        fbWhatsapp = view.findViewById(R.id.whatssap);

        if (getArguments() != null) {
            String idItem = getArguments().getString("ID_ITEM");
            usuariosRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("adocoesDoacoes").child(idItem);
            carregarDadosDoFirebase();
        } else {
            Toast.makeText(getContext(), "Erro ao carregar doação.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }

        configurarBotaoWhatsapp();
    }

    private void carregarDadosDoFirebase() {
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    String tituloStr = obterStringSegura(snapshot, "titulo");
                    String descricaoStr = obterStringSegura(snapshot, "descricao");
                    String categoriaStr = obterStringSegura(snapshot, "categoria");
                    String enderecoStr = obterStringSegura(snapshot, "endereco");
                    String bairroStr = obterStringSegura(snapshot, "bairro");
                    String contatoStr = obterStringSegura(snapshot, "contato");
                    String imagemStr = obterStringSegura(snapshot, "imagem");
                    String dataStr = obterStringSegura(snapshot, "data_post");

                    mostrarInformacoes(tituloStr, descricaoStr, categoriaStr, enderecoStr, bairroStr, contatoStr, imagemStr, dataStr);
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Esta publicação não existe mais.", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Erro de conexão com o banco de dados.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String obterStringSegura(DataSnapshot snapshot, String chave) {
        if (snapshot.child(chave).getValue() != null) {
            return snapshot.child(chave).getValue().toString();
        }
        return "Não informado";
    }

    private void mostrarInformacoes(String tituloStr, String descricaoStr, String categoriaStr, String enderecoStr,
                                    String bairroStr, String contatoStr, String imagemStr, String dataStr) {
        titulo.setText(tituloStr);
        descricao.setText(descricaoStr);
        categoria.setText(categoriaStr);
        endereco.setText("🏠 Endereço: " + enderecoStr);
        bairro.setText("📍 Bairro: " + bairroStr);
        contato.setText("📱 Contato: " + contatoStr);
        data.setText("Publicado em: " + dataStr);

        numeroContatoFormatado = contatoStr;
        baixarImagem(imagemStr);
    }

    private void baixarImagem(String nomeImagem) {
        if (nomeImagem == null || nomeImagem.equals("Não informado")) return;

        StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                .child("BancoImagens").child("DoacoesAdocoes").child(nomeImagem + ".png");

        imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (isAdded()) {
                Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .placeholder(R.drawable.padrao)
                        .into(foto);
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) foto.setImageResource(R.drawable.padrao);
        });
    }

    private void configurarBotaoWhatsapp() {
        fbWhatsapp.setOnClickListener(v -> {
            if (!numeroContatoFormatado.isEmpty() && !numeroContatoFormatado.equals("Não informado")) {
                String apenasNumeros = numeroContatoFormatado.replaceAll("[^0-9]", "");
                if (!apenasNumeros.startsWith("55")) {
                    apenasNumeros = "55" + apenasNumeros;
                }
                Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=" + apenasNumeros);
                Intent whatssapIntent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(whatssapIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "WhatsApp não instalado no celular.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Número de contato inválido.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}