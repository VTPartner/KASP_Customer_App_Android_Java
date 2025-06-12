package com.kapstranspvtltd.kaps.common_activities.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.common_activities.models.RecentSearch;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {
    private List<RecentSearch> recentSearches;
    private OnRecentSearchClickListener listener;

    public interface OnRecentSearchClickListener {
        void onRecentSearchClick(RecentSearch search);
    }

    public RecentSearchAdapter(List<RecentSearch> recentSearches, OnRecentSearchClickListener listener) {
        this.recentSearches = recentSearches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentSearch search = recentSearches.get(position);
        holder.bind(search);
    }

    @Override
    public int getItemCount() {
        return recentSearches.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView addressText,placeNameText;
        TextView distanceText;
        ImageView iconView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeNameText = itemView.findViewById(R.id.placeNameText);
            addressText = itemView.findViewById(R.id.addressText);
            iconView = itemView.findViewById(R.id.iconHistory);
            distanceText = itemView.findViewById(R.id.distanceText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRecentSearchClick(recentSearches.get(position));
                }
            });
        }

        void bind(RecentSearch search) {
            // Show both place name and address if place name exists
            if (!TextUtils.isEmpty(search.getCompanyName())) {
                placeNameText.setVisibility(View.VISIBLE);
                placeNameText.setText(search.getCompanyName());
                addressText.setText(search.getAddress());
                distanceText.setText(String.format("%.1f km away", search.getDistance()));
            } else {
                placeNameText.setVisibility(View.GONE);
                addressText.setText(search.getAddress());
            }
//            addressText.setText(search.getAddress());
        }
    }
}