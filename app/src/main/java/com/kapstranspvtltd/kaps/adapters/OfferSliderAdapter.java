package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.activities.models.SliderModel;
import com.kapstranspvtltd.kaps.R;

import java.util.ArrayList;
import java.util.List;

public class OfferSliderAdapter extends RecyclerView.Adapter<OfferSliderAdapter.SliderViewHolder> {
    private List<SliderModel> offers = new ArrayList<>();

    public void setOffers(List<SliderModel> offers) {
        this.offers = offers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.bind(offers.get(position));
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView offerImage;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            offerImage = itemView.findViewById(R.id.offerImage);
        }

        void bind(SliderModel offer) {
            Glide.with(itemView.getContext())
                    .load(offer.getImageUrl())
                    .into(offerImage);
        }
    }
}