package com.ifam.amigopetfixo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.ifam.amigopetfixo.R;

public class InformacoesAppFragment extends Fragment {

    public InformacoesAppFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_informacoes_app, container, false);

        WebView webView = view.findViewById(R.id.webViewInfo);

        if (getArguments() != null) {
            String tipoTela = getArguments().getString("tipoTela");

            // Carrega o arquivo HTML local correspondente
            if ("sobre".equals(tipoTela)) {
                webView.loadUrl("file:///android_asset/sobre.html");
            } else if ("termos".equals(tipoTela)) {
                webView.loadUrl("file:///android_asset/termos.html");
            } else if ("privacidade".equals(tipoTela)) {
                webView.loadUrl("file:///android_asset/privacidade.html");
            }
        }

        return view;
    }
}