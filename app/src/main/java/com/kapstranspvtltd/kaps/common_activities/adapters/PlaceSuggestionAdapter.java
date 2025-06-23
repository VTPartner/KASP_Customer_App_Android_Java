package com.kapstranspvtltd.kaps.common_activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.kapstranspvtltd.kaps.R;

import java.util.ArrayList;
import java.util.List;

public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.ViewHolder> {
    private List<AutocompletePrediction> predictions;

    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(AutocompletePrediction prediction);
    }

    public void updatePredictions(List<AutocompletePrediction> newPredictions) {
        this.predictions.clear();
        if (newPredictions != null) {
            this.predictions.addAll(newPredictions);
        }
        notifyDataSetChanged();
    }

    public PlaceSuggestionAdapter(OnSuggestionClickListener listener) {
        this.predictions = new ArrayList<>();
        this.listener = listener;
    }

    public void setPredictions(List<AutocompletePrediction> predictions) {
        this.predictions = predictions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AutocompletePrediction prediction = predictions.get(position);
        holder.bind(prediction);
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView primaryText;
        TextView secondaryText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            primaryText = itemView.findViewById(R.id.tvPrimaryText);
            secondaryText = itemView.findViewById(R.id.tvSecondaryText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSuggestionClick(predictions.get(position));
                }
            });
        }

        void bind(AutocompletePrediction prediction) {
            primaryText.setText(prediction.getPrimaryText(null));
            secondaryText.setText(prediction.getSecondaryText(null));
        }
    }
}