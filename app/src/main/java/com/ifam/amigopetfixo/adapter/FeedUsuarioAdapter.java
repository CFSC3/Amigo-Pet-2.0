package com.ifam.amigopetfixo.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ifam.amigopetfixo.Model.bean.Postagem;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.UsuarioHubActivity;
import com.ifam.amigopetfixo.activity.VisualizacaoHubActivity;

import java.util.List;

public class FeedUsuarioAdapter extends RecyclerView.Adapter<FeedUsuarioAdapter.FeedViewHolder> {

    private List<Postagem> listaPostagensFeed;
    private boolean isProprietario;

    public FeedUsuarioAdapter(List<Postagem> listaPostagensFeed, boolean isProprietario) {
        this.listaPostagensFeed = listaPostagensFeed;
        this.isProprietario = isProprietario;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_postagem, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Postagem postagem = listaPostagensFeed.get(position);
        String urlMidia = postagem.getUrlMidia();

        if (urlMidia != null && !urlMidia.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(urlMidia)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgPostagem);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (isProprietario) {
                intent = new Intent(v.getContext(), UsuarioHubActivity.class);
                intent.putExtra("Acao", "ThreadPostagem");
            } else {
                intent = new Intent(v.getContext(), VisualizacaoHubActivity.class);
                intent.putExtra("TIPO_VISUALIZACAO", "THREAD_POSTAGEM");
            }
            intent.putExtra("ID_ITEM", postagem.getIdPostagem());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaPostagensFeed.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPostagem;
        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostagem = itemView.findViewById(R.id.imgPostagemFeed);
        }
    }
}