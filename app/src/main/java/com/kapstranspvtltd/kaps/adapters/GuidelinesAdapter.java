package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.activities.models.GuidelineModel;
import com.kapstranspvtltd.kaps.R;

public class GuidelinesAdapter extends ListAdapter<GuidelineModel, GuidelinesAdapter.ViewHolder> {
    
    public GuidelinesAdapter() {
        super(new DiffUtil.ItemCallback<GuidelineModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull GuidelineModel oldItem, @NonNull GuidelineModel newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areContentsTheSame(@NonNull GuidelineModel oldItem, @NonNull GuidelineModel newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) 
                    && oldItem.getDescription().equals(newItem.getDescription());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guideline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuidelineModel guideline = getItem(position);
        holder.bind(guideline);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView descriptionText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
        }

        void bind(GuidelineModel guideline) {
            titleText.setText(guideline.getTitle());
            descriptionText.setText(guideline.getDescription());
        }
    }
}