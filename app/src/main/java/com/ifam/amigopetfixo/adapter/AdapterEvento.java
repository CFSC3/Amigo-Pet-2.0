package com.ifam.amigopetfixo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ifam.amigopetfixo.Model.bean.Evento;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.helper.VisitanteHelper;

import java.util.List;

public class AdapterEvento extends RecyclerView.Adapter<AdapterEvento.MyViewHolder> {

    private List<Evento> listaEventos;
    private OnEventoClickListener listener;

    public interface OnEventoClickListener {
        void onEventoClick(Evento evento);
    }

    public AdapterEvento(List<Evento> listaEventos, OnEventoClickListener listener) {
        this.listaEventos = listaEventos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Evento evento = listaEventos.get(position);

        holder.titulo.setText(evento.getTitulo());
        holder.resumo.setText(evento.getResumo());

        if (evento.getUrlImagem() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(evento.getUrlImagem())
                    .centerCrop()
                    .placeholder(R.drawable.padrao)
                    .into(holder.imagem);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!VisitanteHelper.verificarAcesso(v.getContext())) return;

            if (listener != null) {
                listener.onEventoClick(evento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaEventos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView titulo;
        private TextView resumo;
        private ImageView imagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textTituloE);
            imagem = itemView.findViewById(R.id.imageViewEventos);
            resumo = itemView.findViewById(R.id.textResumoE);
        }
    }
}