package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.models.SliderModel;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private List<SliderModel> offers = new ArrayList<>();

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        SliderModel offer = offers.get(position);
        
        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(offer.getImageUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.imageView);

        // Set title and description if needed
        holder.titleView.setText(offer.getTitle());
        holder.descriptionView.setText(offer.getDescription());
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    public void setOffers(List<SliderModel> offers) {
        this.offers = offers;
        notifyDataSetChanged();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView descriptionView;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
//            imageView = itemView.findViewById(R.id.bannerImage);
//            titleView = itemView.findViewById(R.id.bannerTitle);
//            descriptionView = itemView.findViewById(R.id.bannerDescription);
        }
    }
}