package com.ifam.amigopetfixo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ifam.amigopetfixo.Model.bean.Evento;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.AdicionarHubActivity;
import com.ifam.amigopetfixo.adapter.AdapterEvento;
import com.ifam.amigopetfixo.helper.VisitanteHelper;
import com.ifam.amigopetfixo.repository.EventoRepository;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class EventosFragment extends Fragment implements AdapterEvento.OnEventoClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdicionarEvento;

    private AdapterEvento adapter;
    private List<Evento> listaEventos = new ArrayList<>();

    private EventoRepository repository;

    public EventosFragment() {
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    buscarLocalizacaoEFiltrar();
                } else {
                    carregarDados();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventos, container, false);

        repository = new EventoRepository();

        recyclerView = view.findViewById(R.id.reciclerView2);
        fabAdicionarEvento = view.findViewById(R.id.fabAdicionarEvento);

        configurarRecyclerView();
        configurarBotoes();
        verificarPermissaoLocalizacao();

        return view;
    }

    private void configurarRecyclerView() {
        adapter = new AdapterEvento(listaEventos, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void configurarBotoes() {
        fabAdicionarEvento.setOnClickListener(v -> {
            if (!VisitanteHelper.verificarAcesso(getContext())) return;

            Intent intent = new Intent(getActivity(), AdicionarHubActivity.class);
            intent.putExtra("TIPO_CRIACAO", "EVENTO");
            startActivity(intent);
        });
    }

    private void carregarDados() {
        repository.carregarEventos(new EventoRepository.OnEventosLoadedListener() {
            @Override
            public void onSuccess(List<Evento> eventos) {
                listaEventos.clear();
                listaEventos.addAll(eventos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String erro) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Erro: " + erro, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verificarPermissaoLocalizacao() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buscarLocalizacaoEFiltrar();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void buscarLocalizacaoEFiltrar() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                Geocoder geocoder = new Geocoder(requireContext(), new Locale("pt", "BR"));
                java.util.List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address endereco = addresses.get(0);

                    String cidade = endereco.getLocality();
                    if (cidade == null) {
                        cidade = endereco.getSubAdminArea();
                    }

                    if (cidade != null) {
                        String cidadeFiltro = cidade.toLowerCase();
                        repository.carregarEventosPorLocalizacao(cidadeFiltro, new EventoRepository.OnEventosLoadedListener() {
                            @Override
                            public void onSuccess(List<Evento> eventos) {
                                listaEventos.clear();
                                listaEventos.addAll(eventos);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(String erro) {
                                Toast.makeText(getContext(), "Erro no filtro: " + erro, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } else {
                Toast.makeText(getContext(), "Aguardando sinal de GPS...", Toast.LENGTH_SHORT).show();
                carregarDados();
            }
        } catch (SecurityException e) {
            carregarDados();
        } catch (Exception e) {
            carregarDados();
        }
    }

    @Override
    public void onEventoClick(Evento evento) {
        Intent intent = new Intent(getContext(), com.ifam.amigopetfixo.activity.VisualizacaoHubActivity.class);
        intent.putExtra("TIPO_VISUALIZACAO", "EVENTO");
        intent.putExtra("ID_ITEM", evento.getIdEvento());
        startActivity(intent);
    }
}