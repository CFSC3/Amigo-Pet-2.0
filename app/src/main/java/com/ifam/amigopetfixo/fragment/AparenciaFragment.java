package com.ifam.amigopetfixo.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.ifam.amigopetfixo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AparenciaFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final String PREFS_NAME = "tema_prefs";
    private static final String KEY_TEMA = "tema_selecionado";
    private static final String WALLPAPER_FILENAME = "custom_chat_wallpaper";

    private RadioGroup radioGroupTema;
    private RadioButton radioTemaSistema, radioTemaClaro, radioTemaEscuro;
    private ImageView imgPreviewFundoChat;
    private MaterialButton btnEscolherFundoChat, btnRemoverFundoChat;

    public AparenciaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aparencia, container, false);

        radioGroupTema = view.findViewById(R.id.radioGroupTema);
        radioTemaSistema = view.findViewById(R.id.radioTemaSistema);
        radioTemaClaro = view.findViewById(R.id.radioTemaClaro);
        radioTemaEscuro = view.findViewById(R.id.radioTemaEscuro);

        imgPreviewFundoChat = view.findViewById(R.id.imgPreviewFundoChat);
        btnEscolherFundoChat = view.findViewById(R.id.btnEscolherFundoChat);
        btnRemoverFundoChat = view.findViewById(R.id.btnRemoverFundoChat);

        carregarPreferenciaTema();
        carregarPreviewFundoChat();
        configurarEventos();

        return view;
    }

    private void configurarEventos() {
        radioGroupTema.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

            if (checkedId == R.id.radioTemaClaro) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putInt(KEY_TEMA, AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.radioTemaEscuro) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putInt(KEY_TEMA, AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                editor.putInt(KEY_TEMA, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            editor.apply();
        });

        btnEscolherFundoChat.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Aceita qualquer tipo de imagem (PNG, JPEG, WEBP)
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnRemoverFundoChat.setOnClickListener(v -> {
            File file = new File(requireContext().getFilesDir(), WALLPAPER_FILENAME);
            if (file.exists()) {
                file.delete();
                imgPreviewFundoChat.setImageDrawable(null);
                Toast.makeText(getContext(), "Fundo padrão restaurado.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarPreferenciaTema() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int temaSalvo = prefs.getInt(KEY_TEMA, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (temaSalvo == AppCompatDelegate.MODE_NIGHT_NO) {
            radioTemaClaro.setChecked(true);
        } else if (temaSalvo == AppCompatDelegate.MODE_NIGHT_YES) {
            radioTemaEscuro.setChecked(true);
        } else {
            radioTemaSistema.setChecked(true);
        }
    }

    private void carregarPreviewFundoChat() {
        File file = new File(requireContext().getFilesDir(), WALLPAPER_FILENAME);
        if (file.exists()) {
            imgPreviewFundoChat.setImageURI(Uri.fromFile(file));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            copiarStreamParaArmazenamentoInterno(imageUri);
        }
    }

    private void copiarStreamParaArmazenamentoInterno(Uri sourceUri) {
        try {
            File destino = new File(requireContext().getFilesDir(), WALLPAPER_FILENAME);

            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
                 OutputStream outputStream = new FileOutputStream(destino)) {

                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }

            imgPreviewFundoChat.setImageURI(Uri.fromFile(destino));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}