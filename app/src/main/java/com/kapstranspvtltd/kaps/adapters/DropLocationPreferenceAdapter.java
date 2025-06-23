package com.kapstranspvtltd.kaps.adapters;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.models.DropLocation;
import com.kapstranspvtltd.kaps.common_activities.models.RecentSearch;

import java.util.ArrayList;
import java.util.List;

public class DropLocationPreferenceAdapter extends RecyclerView.Adapter<DropLocationPreferenceAdapter.DropViewHolder> {

    private List<DropLocation> dropLocations;
    private boolean isMainList;
    private OnItemClickListener listener;
    private boolean isRecent;

    public interface OnItemClickListener {
        void onItemClick(DropLocation location);
    }

    public DropLocationPreferenceAdapter(List<DropLocation> dropLocations, boolean isMainList, boolean isRecent) {
        this.dropLocations = dropLocations != null ? dropLocations : new ArrayList<>();
        this.isMainList = isMainList;
        this.isRecent = isRecent;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }



    public void updateListFromRecent(List<RecentSearch> recentSearches) {
        dropLocations.clear();
        for (RecentSearch rs : recentSearches) {
            DropLocation location = new DropLocation();
            location.setAddress(rs.getAddress());
            location.setLatitude(rs.getLatitude());
            location.setLongitude(rs.getLongitude());
            location.setBrandName(rs.getCompanyName());    // Add brand name

            dropLocations.add(location);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drop_location, parent, false);
        return new DropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DropViewHolder holder, int position) {
        DropLocation location = dropLocations.get(position);
        // Set the brand name if available
        if (!TextUtils.isEmpty(location.getBrandName())) {
            holder.brandName.setVisibility(View.VISIBLE);
            holder.brandName.setText(location.getBrandName());
        } else {
            holder.brandName.setVisibility(View.GONE);
        }

        holder.address.setText(location.getAddress());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(location);
            }
        });

        holder.icon.setImageResource(isRecent ? R.drawable.ic_recent : R.drawable.ic_location);

        ImageViewCompat.setImageTintList(holder.icon,
                ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.grey)));
    }

    @Override
    public int getItemCount() {
        return dropLocations.size();
    }

    static class DropViewHolder extends RecyclerView.ViewHolder {
        TextView address,brandName;
        ImageView icon;

        DropViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.txtAddress);
            icon = itemView.findViewById(R.id.ivLocationIcon);
            brandName = itemView.findViewById(R.id.brandName);
        }
    }
}
