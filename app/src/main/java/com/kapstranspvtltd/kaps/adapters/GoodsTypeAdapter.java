package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.activities.models.AllGoodsTypesModel;
import com.kapstranspvtltd.kaps.R;

import java.util.ArrayList;
import java.util.List;

public class GoodsTypeAdapter extends RecyclerView.Adapter<GoodsTypeAdapter.ViewHolder> {
    private List<AllGoodsTypesModel> goodsTypes;
    private List<AllGoodsTypesModel> goodsTypesFiltered;
    private OnGoodsTypeSelectedListener listener;
    private int selectedPosition = 0;

    public void submitList(List<AllGoodsTypesModel> newList) {
        this.goodsTypes = newList;
        this.goodsTypesFiltered = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public interface OnGoodsTypeSelectedListener {
        void onGoodsTypeSelected(AllGoodsTypesModel goodsType);
    }

    public GoodsTypeAdapter(List<AllGoodsTypesModel> goodsTypes, OnGoodsTypeSelectedListener listener) {
        this.goodsTypes = goodsTypes;
        this.goodsTypesFiltered = new ArrayList<>(goodsTypes);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goods_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AllGoodsTypesModel goodsType = goodsTypesFiltered.get(position);
        holder.bind(goodsType, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return goodsTypesFiltered != null ? goodsTypesFiltered.size() : 0;
    }

    public void filter(String query) {
        goodsTypesFiltered.clear();
        if (query.isEmpty()) {
            goodsTypesFiltered.addAll(goodsTypes);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (AllGoodsTypesModel item : goodsTypes) {
                if (item.getGoodsTypeName().toLowerCase().contains(lowerCaseQuery)) {
                    goodsTypesFiltered.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        RadioButton radioButton;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            radioButton = itemView.findViewById(R.id.radioButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int oldPosition = selectedPosition;
                    selectedPosition = position;
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(selectedPosition);
                    listener.onGoodsTypeSelected(goodsTypesFiltered.get(position));
                }
            });
        }

        void bind(AllGoodsTypesModel goodsType, boolean isSelected) {
            nameText.setText(goodsType.getGoodsTypeName());
            radioButton.setChecked(isSelected);
        }
    }
}