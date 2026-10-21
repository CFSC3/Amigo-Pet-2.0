package com.ifam.amigopetfixo.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.fragment.ConfiguracoesFragment;

public class ConfiguracoesHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_hub);

        Toolbar toolbar = findViewById(R.id.toolbarConfigHub);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Carrega o menu de configurações assim que a Activity abre
        if (savedInstanceState == null) {
            carregarFragment(new ConfiguracoesFragment(), "Configurações");
        }
    }

    public void carregarFragment(Fragment fragment, String titulo) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titulo);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutConfigHub, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Configurações");
        } else {
            finish();
        }
    }
}