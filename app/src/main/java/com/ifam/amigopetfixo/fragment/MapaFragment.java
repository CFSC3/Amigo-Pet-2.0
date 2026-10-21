package com.ifam.amigopetfixo.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.google.android.gms.maps.model.BitmapDescriptor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.MapaRepository;

import java.util.List;

public class MapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ChipGroup chipGroupFiltros;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView imgDetalheLogo;

    private MapaRepository repository;
    private String cidadeAtualDetectada = "manaus";
    private MaterialCardView cardDetalheLocal;
    private TextView txtNomeLocal, txtEnderecoLocal;

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    ativarLocalizacaoUsuario();
                } else {
                    Toast.makeText(getContext(), "A permissão de localização é necessária para o mapa.", Toast.LENGTH_LONG).show();
                    moverCameraParaBrasil();
                }
            });

    public MapaFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapa_pet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        imgDetalheLogo = view.findViewById(R.id.imgDetalheLogo);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapaGoogle);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        chipGroupFiltros = view.findViewById(R.id.chipGroupFiltros);
        configurarFiltros();

        repository = new MapaRepository();
        cardDetalheLocal = view.findViewById(R.id.cardDetalheLocal);
        txtNomeLocal = view.findViewById(R.id.txtNomeLocal);
        txtEnderecoLocal = view.findViewById(R.id.txtEnderecoLocal);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> cardDetalheLocal.setVisibility(View.GONE));

        verificarPermissaoGPS();

        carregarPinosNoMapa("TODOS");
    }

    private void verificarPermissaoGPS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacaoUsuario();
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void ativarLocalizacaoUsuario() {
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng posicaoAtual = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 15f));
                        } else {
                            Toast.makeText(getContext(), "Buscando sinal de GPS...", Toast.LENGTH_SHORT).show();
                            moverCameraParaBrasil();
                        }
                    });
        }
    }

    private void moverCameraParaBrasil() {
        LatLng centroBrasil = new LatLng(-14.2350, -51.9253);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroBrasil, 4f));
    }

    private void configurarFiltros() {
        chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int idSelecionado = checkedIds.get(0);

            if (mMap != null) mMap.clear();

            if (idSelecionado == R.id.chipFiltroTodos) {
                carregarPinosNoMapa("TODOS");
            } else if (idSelecionado == R.id.chipFiltroVets) {
                carregarPinosNoMapa("VETS");
            } else if (idSelecionado == R.id.chipFiltroOngs) {
                carregarPinosNoMapa("ONGS");
            }
        });
    }

    private void carregarPinosNoMapa(String categoria) {
        if (mMap == null) return;

        mMap.clear();
        cardDetalheLocal.setVisibility(View.GONE);

        repository.buscarLocais(cidadeAtualDetectada, categoria, new MapaRepository.OnLocaisLoadedListener() {
            @Override
            public void onSuccess(List<MapaRepository.LocalMapa> locais) {
                for (MapaRepository.LocalMapa local : locais) {

                    int iconeVetor;
                    if (local.tipo.equals("VET")) {
                        iconeVetor = R.drawable.pin_laranja_patinha;
                    } else if (local.tipo.equals("ONG")) {
                        iconeVetor = R.drawable.pin_vermelho_coracao;
                    } else {
                        iconeVetor = R.drawable.pin_laranja_patinha;
                    }

                    Marker marker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(local.coordenada)
                                    .title(local.nome)
                                    .icon(bitmapDescriptorFromVector(iconeVetor))
                    );

                    if (marker != null) {
                        marker.setTag(local);
                    }
                }

                mMap.setOnMarkerClickListener(clickedMarker -> {
                    MapaRepository.LocalMapa dadosDoLocal = (MapaRepository.LocalMapa) clickedMarker.getTag();

                    if (dadosDoLocal != null) {
                        txtNomeLocal.setText(dadosDoLocal.nome);
                        txtEnderecoLocal.setText(dadosDoLocal.endereco);

                        if (dadosDoLocal.urlImagemLogo != null) {
                            Glide.with(requireContext())
                                    .load(dadosDoLocal.urlImagemLogo)
                                    .centerCrop()
                                    .placeholder(R.drawable.padrao)
                                    .into(imgDetalheLogo);
                        } else {

                            if (dadosDoLocal.tipo.equals("VET")) {
                                imgDetalheLogo.setImageResource(R.drawable.logo_png1);
                            } else {
                                imgDetalheLogo.setImageResource(R.drawable.logo);
                            }
                        }

                        cardDetalheLocal.setVisibility(View.VISIBLE);

                        mMap.animateCamera(CameraUpdateFactory.newLatLng(dadosDoLocal.coordenada));
                    }
                    return true;
                });
            }

            @Override
            public void onError(String erro) {
                Toast.makeText(getContext(), "Erro ao carregar mapa: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}