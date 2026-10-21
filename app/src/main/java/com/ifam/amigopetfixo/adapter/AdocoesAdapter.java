package com.ifam.amigopetfixo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifam.amigopetfixo.Model.bean.Adocoes;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.helper.VisitanteHelper;

import java.util.List;

public class AdocoesAdapter extends RecyclerView.Adapter<AdocoesAdapter.MyViewHolder> {

    private List<Adocoes> listaDoacoes;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Adocoes adocao);
    }

    public AdocoesAdapter(List<Adocoes> lista, OnItemClickListener listener) {
        this.listaDoacoes = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doacao_moderno, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Adocoes adocao = listaDoacoes.get(position);

        holder.titulo.setText(adocao.getTitulo());
        holder.bairro.setText(adocao.getBairro());
        holder.data.setText(adocao.getData_post());
        holder.categoria.setText(adocao.getCategoria() != null ? adocao.getCategoria() : "Geral");

        String nomeImagem = adocao.getImagem();

        if (nomeImagem != null && !nomeImagem.equals("Não informado") && !nomeImagem.isEmpty()) {
            StorageReference imagemRef = FirebaseStorage.getInstance().getReference()
                    .child("BancoImagens").child("DoacoesAdocoes").child(nomeImagem + ".png");

            imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(holder.itemView.getContext())
                        .load(uri)
                        .centerCrop()
                        .placeholder(R.drawable.padrao)
                        .into(holder.foto);
            }).addOnFailureListener(e -> {
                holder.foto.setImageResource(R.drawable.padrao);
            });
        } else {
            holder.foto.setImageResource(R.drawable.padrao);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!VisitanteHelper.verificarAcesso(v.getContext())) return;

            listener.onItemClick(adocao);
        });
    }

    @Override
    public int getItemCount() {
        return listaDoacoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView foto;
        TextView titulo, bairro, data, categoria;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.imgItemDoacao);
            titulo = itemView.findViewById(R.id.txtItemTitulo);
            bairro = itemView.findViewById(R.id.txtItemBairro);
            data = itemView.findViewById(R.id.txtItemData);
            categoria = itemView.findViewById(R.id.txtItemCategoria);
        }
    }
}