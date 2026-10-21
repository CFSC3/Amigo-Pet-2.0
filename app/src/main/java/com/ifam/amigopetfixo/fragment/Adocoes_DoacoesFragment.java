package com.ifam.amigopetfixo.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ifam.amigopetfixo.Model.bean.Adocoes;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.adapter.AdocoesAdapter;
import com.ifam.amigopetfixo.activity.VisualizacaoHubActivity;
import com.ifam.amigopetfixo.helper.VisitanteHelper;
import com.ifam.amigopetfixo.repository.DoacaoRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Adocoes_DoacoesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdocoesAdapter adapter;
    private List<Adocoes> listaDoacoes = new ArrayList<>();

    private ChipGroup chipGroupFiltrosDoacoes;
    private android.widget.AutoCompleteTextView autoCompleteEstado;
    private android.widget.AutoCompleteTextView autoCompleteTempo;
    private FloatingActionButton fabAdicionarDoacao;

    private DoacaoRepository repository;
    private List<Adocoes> listaMestreDoacoes = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;

    private String estadoSelecionado = "Amazonas";
    private String tempoSelecionado = "Recentes";
    private String categoriaSelecionada = "Todas as Categorias";

    public Adocoes_DoacoesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adocoes__doacoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        recyclerView = view.findViewById(R.id.recyclerView1);
        chipGroupFiltrosDoacoes = view.findViewById(R.id.chipGroupFiltrosDoacoes);
        autoCompleteEstado = view.findViewById(R.id.autoCompleteEstado);
        autoCompleteTempo = view.findViewById(R.id.autoCompleteTempo);
        fabAdicionarDoacao = view.findViewById(R.id.fabAdicionarDoacao);

        adapter = new AdocoesAdapter(listaDoacoes, adocao -> {
            Intent intent = new Intent(getContext(), VisualizacaoHubActivity.class);
            intent.putExtra("TIPO_VISUALIZACAO", "DOACAO");
            intent.putExtra("ID_ITEM", adocao.getTitulo());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        detectarEstadoAtualUsuario();
        configurarSpinners();
        configurarFiltroCategoria();
        configurarFabClique();

        repository = new DoacaoRepository();
        carregarDadosDoBanco();
    }

    private void detectarEstadoAtualUsuario() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(requireContext(), new Locale("pt", "BR"));
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String estadoDetectado = addresses.get(0).getAdminArea();
                            if (estadoDetectado != null) {
                                estadoSelecionado = estadoDetectado;
                                autoCompleteEstado.setText(estadoSelecionado, false);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SecurityException e) {
        }
    }

    private void configurarSpinners() {
        String[] estados = {"Acre", "Alagoas", "Amapá", "Amazonas", "Bahia", "Ceará", "Distrito Federal", "Espírito Santo", "Goiás", "Maranhão", "Mato Grosso", "Mato Grosso do Sul", "Minas Gerais", "Pará", "Paraíba", "Paraná", "Pernambuco", "Piauí", "Rio de Janeiro", "Rio Grande do Norte", "Rio Grande do Sul", "Rondônia", "Roraima", "Santa Catarina", "São Paulo", "Sergipe", "Tocantins", "Todos os Estados"};
        android.widget.ArrayAdapter<String> adapterEstados = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, estados);
        autoCompleteEstado.setAdapter(adapterEstados);

        String[] tempos = {"Recentes", "Última Semana", "Último Mês", "Qualquer Data"};
        android.widget.ArrayAdapter<String> adapterTempos = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, tempos);
        autoCompleteTempo.setAdapter(adapterTempos);

        autoCompleteEstado.setOnItemClickListener((parent, view, position, id) -> {
            estadoSelecionado = estados[position];
            aplicarFiltrosCombinados();
        });

        autoCompleteTempo.setOnItemClickListener((parent, view, position, id) -> {
            tempoSelecionado = tempos[position];
            aplicarFiltrosCombinados();
        });
    }

    private void configurarFiltroCategoria() {
        chipGroupFiltrosDoacoes.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int idSelecionado = checkedIds.get(0);
            if (idSelecionado == R.id.chipTodos) categoriaSelecionada = "Todas as Categorias";
            else if (idSelecionado == R.id.chipAlimentacao) categoriaSelecionada = "Alimentação";
            else if (idSelecionado == R.id.chipAnimais) categoriaSelecionada = "Animais";
            else if (idSelecionado == R.id.chipBrinquedos) categoriaSelecionada = "Brinquedos";
            else if (idSelecionado == R.id.chipHigiene) categoriaSelecionada = "Higiene";
            else if (idSelecionado == R.id.chipMoveis) categoriaSelecionada = "Móveis";
            else if (idSelecionado == R.id.chipRemedios) categoriaSelecionada = "Remédios";

            aplicarFiltrosCombinados();
        });
    }

    private void carregarDadosDoBanco() {
        repository.carregarDoacoes(new DoacaoRepository.OnDoacoesCarregadasListener() {
            @Override
            public void onDataLoaded(List<Adocoes> doacoes) {
                listaMestreDoacoes.clear();
                listaMestreDoacoes.addAll(doacoes);
                aplicarFiltrosCombinados();
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(getContext(), "Erro ao carregar: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void aplicarFiltrosCombinados() {
        listaDoacoes.clear();

        for (Adocoes adocao : listaMestreDoacoes) {
            boolean matchCategoria = categoriaSelecionada.equals("Todas as Categorias") ||
                    (adocao.getCategoria() != null && adocao.getCategoria().equals(categoriaSelecionada));

            boolean matchEstado = estadoSelecionado.equals("Todos os Estados") ||
                    (adocao.getEstado() != null && adocao.getEstado().equals(estadoSelecionado));

            if (matchCategoria && matchEstado) {
                listaDoacoes.add(adocao);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void configurarFabClique() {
        fabAdicionarDoacao.setOnClickListener(v -> {
            if (!VisitanteHelper.verificarAcesso(getContext())) return;

            String[] opcoes = {"Tirar Foto (Câmera)", "Escolher da Galeria"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Adicionar Imagem")
                    .setItems(opcoes, (dialog, which) -> {
                        int opcaoEscolhida = (which == 0) ? 2 : 1;

                        Intent intent = new Intent(getActivity(), com.ifam.amigopetfixo.activity.AdicionarHubActivity.class);
                        intent.putExtra("TIPO_CRIACAO", "DOACAO");
                        intent.putExtra("OPCAO_MIDIA", opcaoEscolhida);
                        startActivity(intent);
                    })
                    .show();
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