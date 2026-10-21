package com.ifam.amigopetfixo.fragment;

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
import com.ifam.amigopetfixo.Model.bean.Evento;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.EventoRepository;

public class VisualizarEventoFragment extends Fragment {

    private String idEventoSelecionado;
    private EventoRepository repository;

    private ImageView imgDetalheEvento;
    private TextView txtTituloDetalheEvento, txtDataHoraDetalhe, txtEnderecoDetalhe, txtDescricaoDetalheEvento;

    public VisualizarEventoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            idEventoSelecionado = getArguments().getString("ID_ITEM");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visualizar_evento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new EventoRepository();
        inicializarComponentes(view);

        if (idEventoSelecionado != null) {
            carregarDetalhesDoEvento();
        } else {
            Toast.makeText(getContext(), "Erro: Evento não identificado.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
        }
    }

    private void inicializarComponentes(View view) {
        imgDetalheEvento = view.findViewById(R.id.imgDetalheEvento);
        txtTituloDetalheEvento = view.findViewById(R.id.txtTituloDetalheEvento);
        txtDataHoraDetalhe = view.findViewById(R.id.txtDataHoraDetalhe);
        txtEnderecoDetalhe = view.findViewById(R.id.txtEnderecoDetalhe);
        txtDescricaoDetalheEvento = view.findViewById(R.id.txtDescricaoDetalheEvento);
    }

    private void carregarDetalhesDoEvento() {
        repository.buscarEventoPorId(idEventoSelecionado, new EventoRepository.OnEventoUnicoLoadedListener() {
            @Override
            public void onSuccess(Evento evento) {
                if (isAdded()) {
                    preencherTela(evento);
                }
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preencherTela(Evento evento) {
        txtTituloDetalheEvento.setText(evento.getTitulo());
        txtDescricaoDetalheEvento.setText(evento.getDescricao());

        // Lógica Inteligente para Data
        String dataExibicao;
        if (evento.getDataEvento().equals(evento.getDataEventoFim())) {
            dataExibicao = evento.getDataEvento();
        } else {
            dataExibicao = "De " + evento.getDataEvento() + " até " + evento.getDataEventoFim();
        }

        String horaExibicao;
        if (evento.getHorarioEvento().equals(evento.getHorarioEventoFim())) {
            horaExibicao = evento.getHorarioEvento();
        } else {
            horaExibicao = evento.getHorarioEvento() + " às " + evento.getHorarioEventoFim();
        }

        txtDataHoraDetalhe.setText(dataExibicao + " - " + horaExibicao);

        if (evento.getEndereco() != null && !evento.getEndereco().isEmpty()) {
            txtEnderecoDetalhe.setText(evento.getEndereco());
        } else {
            txtEnderecoDetalhe.setText("Local não informado.");
        }

        if (evento.getUrlImagem() != null) {
            Glide.with(requireContext())
                    .load(evento.getUrlImagem())
                    .centerCrop()
                    .placeholder(R.drawable.padrao)
                    .into(imgDetalheEvento);
        }
    }
}