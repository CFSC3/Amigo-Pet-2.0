package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.PostagemRepository;

public class EditarPostagemFragment extends Fragment {

    private String idPostagemSelecionada;
    private EditText editTextoPostagem;
    private ImageButton btnVoltarEditar;
    private MaterialButton btnSalvarEdicao;
    private PostagemRepository postagemRepository;

    private View cardMidiaAtual;
    private ImageView imgMidiaAtual, iconePlayVideoAtual;
    private VideoView videoMidiaAtual;

    public EditarPostagemFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_postagem, container, false);

        if (getArguments() != null) {
            idPostagemSelecionada = getArguments().getString("ID_ITEM");
        }

        postagemRepository = new PostagemRepository();

        editTextoPostagem = view.findViewById(R.id.editTextoPostagem);
        btnVoltarEditar = view.findViewById(R.id.btnVoltarEditar);
        btnSalvarEdicao = view.findViewById(R.id.btnSalvarEdicao);

        cardMidiaAtual = view.findViewById(R.id.cardMidiaAtual);
        imgMidiaAtual = view.findViewById(R.id.imgMidiaAtual);
        iconePlayVideoAtual = view.findViewById(R.id.iconePlayVideoAtual);
        videoMidiaAtual = view.findViewById(R.id.videoMidiaAtual);

        if (idPostagemSelecionada != null) {
            carregarDadosAtuais();
            configurarBotoes();
        } else {
            Toast.makeText(getContext(), "Erro ao carregar edição.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
        }

        return view;
    }

    private void carregarDadosAtuais() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("banco-dados").child("Postagens").child(idPostagemSelecionada);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Postagem postagem = snapshot.getValue(Postagem.class);
                    if (postagem != null) {
                        editTextoPostagem.setText(postagem.getTextoPostagem());

                        if (postagem.getUrlMidia() != null && !postagem.getUrlMidia().isEmpty()) {
                            Glide.with(EditarPostagemFragment.this).load(postagem.getUrlMidia()).into(imgMidiaAtual);

                            if ("video".equals(postagem.getTipoMidia())) {
                                iconePlayVideoAtual.setVisibility(View.VISIBLE);
                                imgMidiaAtual.setOnClickListener(v -> {
                                    imgMidiaAtual.setVisibility(View.GONE);
                                    iconePlayVideoAtual.setVisibility(View.GONE);
                                    videoMidiaAtual.setVisibility(View.VISIBLE);

                                    android.widget.MediaController mediaController = new android.widget.MediaController(getContext());
                                    mediaController.setAnchorView(videoMidiaAtual);
                                    videoMidiaAtual.setMediaController(mediaController);
                                    videoMidiaAtual.setVideoURI(android.net.Uri.parse(postagem.getUrlMidia()));
                                    videoMidiaAtual.start();
                                });
                            } else {
                                iconePlayVideoAtual.setVisibility(View.GONE);
                                imgMidiaAtual.setOnClickListener(null);
                            }
                        } else {
                            cardMidiaAtual.setVisibility(View.GONE);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void configurarBotoes() {
        btnVoltarEditar.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().finish();
        });

        btnSalvarEdicao.setOnClickListener(v -> {
            String novoTexto = editTextoPostagem.getText().toString().trim();

            btnSalvarEdicao.setEnabled(false);
            btnSalvarEdicao.setText("Salvando...");

            postagemRepository.atualizarTextoPostagem(idPostagemSelecionada, novoTexto, new PostagemRepository.OnPostagemCompleteListener() {
                @Override
                public void onSuccess() {
                    if (getContext() != null) Toast.makeText(getContext(), "Postagem atualizada!", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) getActivity().finish();
                }

                @Override
                public void onFailure(String erro) {
                    if (getContext() != null) Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
                    btnSalvarEdicao.setEnabled(true);
                    btnSalvarEdicao.setText("Salvar");
                }
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null && ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }
}