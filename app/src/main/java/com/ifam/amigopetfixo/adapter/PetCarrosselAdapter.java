package com.ifam.amigopetfixo.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ifam.amigopetfixo.Model.bean.Perfil_pet;
import com.ifam.amigopetfixo.R;
import com.ifam.amigopetfixo.activity.UsuarioHubActivity;
import com.ifam.amigopetfixo.activity.VisualizacaoHubActivity;

import java.util.List;

public class PetCarrosselAdapter extends RecyclerView.Adapter<PetCarrosselAdapter.PetViewHolder> {

    private List<Perfil_pet> listaPets;
    private String usuarioId;

    public PetCarrosselAdapter(List<Perfil_pet> listaPets, String usuarioId) {
        this.listaPets = listaPets;
        this.usuarioId = usuarioId;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_carrossel, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Perfil_pet pet = listaPets.get(position);

        if (pet.getNomePet() != null) {
            holder.textNomePet.setText(pet.getNomePet());
        }

        String urlFoto = pet.getUrlFotoPet();

        if (urlFoto != null && !urlFoto.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(urlFoto)
                    .centerCrop()
                    .into(holder.imgPet);
        } else {
            holder.imgPet.setImageResource(R.drawable.icon_pet_profile);
        }

        holder.itemView.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

            if (usuarioId != null && usuarioId.equals(currentUserId)) {
                Intent intent = new Intent(v.getContext(), UsuarioHubActivity.class);
                intent.putExtra("Acao", "VisualizarPet");
                intent.putExtra("idPetSelecionado", pet.getIdPet());
                v.getContext().startActivity(intent);
            } else {
                Intent intent = new Intent(v.getContext(), VisualizacaoHubActivity.class);
                intent.putExtra("TIPO_VISUALIZACAO", "PET_PUBLICO");
                intent.putExtra("ID_ITEM", pet.getIdPet());
                intent.putExtra("ID_DONO", usuarioId);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPets.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView imgPet;
        TextView textNomePet;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPet = itemView.findViewById(R.id.imgItemPet);
            textNomePet = itemView.findViewById(R.id.textItemNomePet);
        }
    }
}