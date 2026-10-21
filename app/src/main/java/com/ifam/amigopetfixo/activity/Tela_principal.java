package com.ifam.amigopetfixo.activity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.helper.VisitanteHelper;

import java.util.ArrayList;
import java.util.List;

public class Tela_principal extends AppCompatActivity {

    private NavController navController;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

                Boolean locationGranted = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    locationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                }
                Boolean cameraGranted = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                }

                Boolean storageGranted = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    storageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false) ||
                            result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                }

                if (locationGranted && cameraGranted && storageGranted) {

                } else {
                    Toast.makeText(this, "Algumas permissões foram negadas. O app pode ter funções limitadas.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.botao_navegacao);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            // TRAVA DE VISITANTE PARA MAPA E PERFIL
            int id = item.getItemId();
            if (id == R.id.nav_mapa || id == R.id.nav_perfil_usuario) {
                if (!VisitanteHelper.verificarAcesso(this)) {
                    return false;
                }
            }

            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                    .setEnterAnim(android.R.anim.fade_in)
                    .setExitAnim(android.R.anim.fade_out)
                    .setPopEnterAnim(android.R.anim.fade_in)
                    .setPopExitAnim(android.R.anim.fade_out)
                    .build();

            try {
                navController.navigate(item.getItemId(), null, navOptions);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (bottomNavigationView.getSelectedItemId() != destination.getId()) {
                bottomNavigationView.getMenu().findItem(destination.getId()).setChecked(true);
            }
        });

        solicitarPermissoesGlobais();
    }

    private void solicitarPermissoesGlobais() {
        List<String> permissoesParaPedir = new ArrayList<>();

        permissoesParaPedir.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissoesParaPedir.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissoesParaPedir.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissoesParaPedir.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        requestPermissionsLauncher.launch(permissoesParaPedir.toArray(new String[0]));
    }
}