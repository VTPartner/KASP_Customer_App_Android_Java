package com.kapstranspvtltd.kaps.common_activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.common_activities.models.RecentSearch;

import java.util.ArrayList;
import java.util.List;

public class RecentDropLocationAdapter extends RecyclerView.Adapter<RecentDropLocationAdapter.ViewHolder> {
    private List<RecentSearch> recentSearches;
    private OnRecentDropSelectedListener listener;

    public interface OnRecentDropSelectedListener {
        void onRecentDropSelected(RecentSearch recentSearch);
    }

    public RecentDropLocationAdapter(OnRecentDropSelectedListener listener) {
        this.recentSearches = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_drop_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentSearch recentSearch = recentSearches.get(position);
        holder.txtAddress.setText(recentSearch.getAddress());
        if (recentSearch.getCompanyName() != null) {
            holder.txtName.setText(recentSearch.getCompanyName());
            holder.txtName.setVisibility(View.VISIBLE);
        } else {
            holder.txtName.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecentDropSelected(recentSearch));
    }

    @Override
    public int getItemCount() {
        return recentSearches.size();
    }

    public void updateRecentSearches(List<RecentSearch> searches) {
        this.recentSearches = searches;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtAddress;

        ViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txtPlaceName);
            txtAddress = view.findViewById(R.id.txtPlaceAddress);
        }
    }
}