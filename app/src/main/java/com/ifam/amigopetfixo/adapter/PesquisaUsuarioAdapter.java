package com.ifam.amigopetfixo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ifam.amigopetfixo.R;

import java.util.List;
import java.util.Map;

public class PesquisaUsuarioAdapter extends RecyclerView.Adapter<PesquisaUsuarioAdapter.MyViewHolder> {

    private List<Map<String, String>> usuarios;
    private Context context;

    public PesquisaUsuarioAdapter(List<Map<String, String>> usuarios, Context context) {
        this.usuarios = usuarios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pesquisa_usuario, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Map<String, String> usuario = usuarios.get(position);

        String nome = usuario.get("nome");
        String username = usuario.get("username");
        String fotoUrl = usuario.get("fotoUrl");

        holder.textNomeBusca.setText(nome != null ? nome : "");
        holder.textUsernameBusca.setText(username != null ? "@" + username : "");

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(context)
                    .load(fotoUrl)
                    .circleCrop()
                    .into(holder.imageFotoPerfilBusca);
        } else {

        }

        holder.itemView.setOnClickListener(v -> {

            String uidUsuarioClicado = usuario.get("uid");

            android.content.Intent intent = new android.content.Intent(context, com.ifam.amigopetfixo.activity.VisualizacaoHubActivity.class);

            intent.putExtra("TIPO_VISUALIZACAO", "PERFIL_PUBLICO");
            intent.putExtra("ID_ITEM", uidUsuarioClicado);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageFotoPerfilBusca;
        TextView textNomeBusca;
        TextView textUsernameBusca;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageFotoPerfilBusca = itemView.findViewById(R.id.imageFotoPerfilBusca);
            textNomeBusca = itemView.findViewById(R.id.textNomeBusca);
            textUsernameBusca = itemView.findViewById(R.id.textUsernameBusca);
        }
    }
}