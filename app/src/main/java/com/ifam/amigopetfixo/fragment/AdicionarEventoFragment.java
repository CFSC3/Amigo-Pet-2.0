package com.ifam.amigopetfixo.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.ifam.amigopetfixo.Model.bean.Evento;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.repository.EventoRepository;

public class AdicionarEventoFragment extends Fragment {

    private ImageView imagePreviewEvento;
    private LinearLayout layoutAdicionarCapa;
    private TextInputEditText editTituloEvento, editResumoEvento, editDescricaoEvento;
    private MaterialButton btnPublicarEvento, btnCancelarEvento;

    private Uri imagemSelecionadaUri;
    private EventoRepository repository;
    private String currentUserId;

    private TextInputEditText editDataEvento, editDataEventoFim, editHorarioEvento, editHorarioEventoFim, editEnderecoEvento, editCidadeEvento;

    private final ActivityResultLauncher<String> abrirGaleria = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imagemSelecionadaUri = uri;
                    imagePreviewEvento.setImageURI(uri);
                    layoutAdicionarCapa.setVisibility(View.GONE);
                }
            });

    public AdicionarEventoFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adicionar_evento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new EventoRepository();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        inicializarComponentes(view);
        configurarCliques();
        aplicarMascaras();
    }

    private void inicializarComponentes(View view) {
        MaterialCardView cardImagemEvento = view.findViewById(R.id.cardImagemEvento);
        imagePreviewEvento = view.findViewById(R.id.imagePreviewEvento);
        layoutAdicionarCapa = view.findViewById(R.id.layoutAdicionarCapa);

        editTituloEvento = view.findViewById(R.id.editTituloEvento);
        editResumoEvento = view.findViewById(R.id.editResumoEvento);
        editDescricaoEvento = view.findViewById(R.id.editDescricaoEvento);

        btnPublicarEvento = view.findViewById(R.id.btnPublicarEvento);
        btnCancelarEvento = view.findViewById(R.id.btnCancelarEvento);

        editDataEvento = view.findViewById(R.id.editDataEvento);
        editDataEventoFim = view.findViewById(R.id.editDataEventoFim);
        editHorarioEvento = view.findViewById(R.id.editHorarioEvento);
        editHorarioEventoFim = view.findViewById(R.id.editHorarioEventoFim);
        editEnderecoEvento = view.findViewById(R.id.editEnderecoEvento);
        editCidadeEvento = view.findViewById(R.id.editCidadeEvento);
    }

    private void aplicarMascaras() {
        // Máscara de Data (DD/MM/YYYY)
        TextWatcher mascaraData = new TextWatcher() {
            boolean isUpdating;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) { isUpdating = false; return; }
                String str = s.toString().replaceAll("[^\\d]", "");
                StringBuilder mascara = new StringBuilder();
                int i = 0;
                for (char m : "##/##/####".toCharArray()) {
                    if (m != '#' && str.length() > i) { mascara.append(m); continue; }
                    try { mascara.append(str.charAt(i)); } catch (Exception e) { break; }
                    i++;
                }
                isUpdating = true;
                TextInputEditText campoFocado = editDataEvento.hasFocus() ? editDataEvento : editDataEventoFim;
                campoFocado.setText(mascara.toString());
                campoFocado.setSelection(mascara.length());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        editDataEvento.addTextChangedListener(mascaraData);
        editDataEventoFim.addTextChangedListener(mascaraData);

        // Máscara de Horário (HH:MM)
        TextWatcher mascaraHorario = new TextWatcher() {
            boolean isUpdating;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }
                String str = s.toString().replaceAll("[^\\d]", "");
                StringBuilder mascara = new StringBuilder();
                int i = 0;
                for (char m : "##:##".toCharArray()) {
                    if (m != '#' && str.length() > i) {
                        mascara.append(m);
                        continue;
                    }
                    try {
                        mascara.append(str.charAt(i));
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                ((TextInputEditText) (editHorarioEvento.hasFocus() ? editHorarioEvento : editHorarioEventoFim)).setText(mascara.toString());
                ((TextInputEditText) (editHorarioEvento.hasFocus() ? editHorarioEvento : editHorarioEventoFim)).setSelection(mascara.length());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        editHorarioEvento.addTextChangedListener(mascaraHorario);
        editHorarioEventoFim.addTextChangedListener(mascaraHorario);
    }

    private void configurarCliques() {
        imagePreviewEvento.setOnClickListener(v -> abrirGaleria.launch("image/*"));
        layoutAdicionarCapa.setOnClickListener(v -> abrirGaleria.launch("image/*"));

        btnCancelarEvento.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        btnPublicarEvento.setOnClickListener(v -> validarEPublicarEvento());
    }

    // Transforma 21/10/26 em 21/10/2026
    private String normalizarData(String data) {
        if (data != null && data.length() == 8) { // Tamanho de dd/MM/yy
            String[] partes = data.split("/");
            if (partes.length == 3) {
                return partes[0] + "/" + partes[1] + "/20" + partes[2];
            }
        }
        return data;
    }

    // Impede datas impossíveis (ex: 31/02/2026)
    private boolean isDataValida(String data) {
        if (data == null || data.length() < 10) return false;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            sdf.parse(data);
            return true;
        } catch (java.text.ParseException e) {
            return false;
        }
    }

    // Valida se o horário existe (00:00 até 23:59)
    private boolean isHorarioValido(String horario) {
        if (horario == null || horario.length() != 5) return false;
        // Expressão regular que checa HH:MM
        return horario.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$");
    }

    private void validarEPublicarEvento() {
        String titulo = editTituloEvento.getText().toString().trim();
        String resumo = editResumoEvento.getText().toString().trim();
        String descricao = editDescricaoEvento.getText().toString().trim();

        if (titulo.isEmpty() || resumo.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(getContext(), "Preencha todos os campos do evento!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Erro de autenticação.", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataEv = editDataEvento.getText().toString().trim();
        String dataEvFim = editDataEventoFim.getText().toString().trim();
        String horaEv = editHorarioEvento.getText().toString().trim();
        String horaEvFim = editHorarioEventoFim.getText().toString().trim();
        String endereco = editEnderecoEvento.getText().toString().trim();
        String cidade = editCidadeEvento.getText().toString().trim();

        if (dataEv.isEmpty() || dataEvFim.isEmpty() || horaEv.isEmpty() || horaEvFim.isEmpty() || endereco.isEmpty() || cidade.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, preencha as informações de local, data e horário (Início e Fim).", Toast.LENGTH_SHORT).show();
            return;
        }


        dataEv = normalizarData(dataEv);
        dataEvFim = normalizarData(dataEvFim);

        // Bloqueia datas inexistentes no calendário (ex: 30 de fevereiro) ou curtas demais
        if (!isDataValida(dataEv) || !isDataValida(dataEvFim)) {
            Toast.makeText(getContext(), "Por favor, insira datas válidas.", Toast.LENGTH_LONG).show();
            return;
        }

        // Bloqueia horários incompletos ou impossíveis (ex: 25:99)
        if (!isHorarioValido(horaEv) || !isHorarioValido(horaEvFim)) {
            Toast.makeText(getContext(), "Por favor, insira horários válidos entre 00:00 e 23:59.", Toast.LENGTH_LONG).show();
            return;
        }

        btnPublicarEvento.setEnabled(false);

        Evento novoEvento = new Evento();
        novoEvento.setTitulo(titulo);
        novoEvento.setResumo(resumo);
        novoEvento.setDescricao(descricao);
        novoEvento.setDataEvento(dataEv);
        novoEvento.setDataEventoFim(dataEvFim);
        novoEvento.setHorarioEvento(horaEv);
        novoEvento.setHorarioEventoFim(horaEvFim);
        novoEvento.setEndereco(endereco);
        novoEvento.setCidadeFiltro(cidade.toLowerCase());

        repository.criarEvento(novoEvento, imagemSelecionadaUri, currentUserId, new EventoRepository.OnEventoAcaoListener() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) getActivity().finish();
            }

            @Override
            public void onError(String erro) {
                btnPublicarEvento.setEnabled(true);
                Toast.makeText(getContext(), erro, Toast.LENGTH_SHORT).show();
            }
        });
    }
}