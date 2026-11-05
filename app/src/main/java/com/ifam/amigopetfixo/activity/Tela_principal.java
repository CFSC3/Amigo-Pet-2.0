package com.ifam.amigopetfixo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

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
                    // Permissões concedidas
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
        configurarNotificacoesFCM();
        verificarRedirecionamentoNotificacao();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        verificarRedirecionamentoNotificacao();
    }

    private void verificarRedirecionamentoNotificacao() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();

            String acao = null;
            if (extras.containsKey("Acao")) {
                acao = String.valueOf(extras.get("Acao"));
            } else if (extras.containsKey("acao")) {
                acao = String.valueOf(extras.get("acao"));
            }

            if ("AbrirConversa".equals(acao)) {
                String uidDestino = extras.containsKey("uidDestino") ? String.valueOf(extras.get("uidDestino")) : null;

                intent.removeExtra("Acao");
                intent.removeExtra("acao");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intentChat = new Intent(this, com.ifam.amigopetfixo.activity.ChatHubActivity.class);
                    intentChat.putExtra("Acao", "AbrirConversa");
                    intentChat.putExtra("uidDestino", uidDestino);
                    startActivity(intentChat);
                }, 300);
            }
            else if ("AbrirComentario".equals(acao)) {
                String idPostagem = extras.containsKey("idPostagem") ? String.valueOf(extras.get("idPostagem")) : null;

                intent.removeExtra("Acao");
                intent.removeExtra("acao");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intentThread = new Intent(this, com.ifam.amigopetfixo.activity.UsuarioHubActivity.class);
                    intentThread.putExtra("Acao", "ThreadPostagem");
                    intentThread.putExtra("ID_ITEM", idPostagem);
                    startActivity(intentThread);
                }, 300);
            }
        }
    }

    private void solicitarPermissoesGlobais() {
        List<String> permissoesParaPedir = new ArrayList<>();

        permissoesParaPedir.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissoesParaPedir.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissoesParaPedir.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissoesParaPedir.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permissoesParaPedir.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        requestPermissionsLauncher.launch(permissoesParaPedir.toArray(new String[0]));
    }

    private void configurarNotificacoesFCM() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String idUsuarioAtual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) return;

            String tokenFCM = task.getResult();
            FirebaseDatabase.getInstance().getReference()
                    .child("banco-dados")
                    .child("usuario")
                    .child(idUsuarioAtual)
                    .child("tokenFCM")
                    .setValue(tokenFCM);
        });
    }
}