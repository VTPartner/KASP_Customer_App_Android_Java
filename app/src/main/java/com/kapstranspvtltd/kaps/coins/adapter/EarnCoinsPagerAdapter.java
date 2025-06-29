package com.kapstranspvtltd.kaps.coins.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.coins.model.EarnCoinPage;
import java.util.List;

public class EarnCoinsPagerAdapter extends RecyclerView.Adapter<EarnCoinsPagerAdapter.PageViewHolder> {

    public interface OnActionListener {
        void onNextClick(int position);
        void onDone();
    }

    private List<EarnCoinPage> pageList;
    private OnActionListener onActionListener;

    public EarnCoinsPagerAdapter(List<EarnCoinPage> pageList, OnActionListener onActionListener) {
        this.pageList = pageList;
        this.onActionListener = onActionListener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PageViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_how_to_earn_coins_page, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        EarnCoinPage page = pageList.get(position);
        holder.ivEarnCoins.setImageResource(page.imageRes);
        holder.tvMain.setText(page.mainText);
        holder.tvDesc.setText(page.desc);

        if (page.isLast) {
            holder.btnUnderstood.setVisibility(View.VISIBLE);
            holder.tvNext.setVisibility(View.GONE);
        } else {
            holder.btnUnderstood.setVisibility(View.GONE);
            holder.tvNext.setVisibility(View.VISIBLE);
        }

        holder.btnUnderstood.setOnClickListener(v -> {
            if (onActionListener != null) onActionListener.onDone();
        });
        holder.tvNext.setOnClickListener(v -> {
            if (onActionListener != null) onActionListener.onNextClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEarnCoins;
        TextView tvMain, tvDesc, tvNext;
        Button btnUnderstood;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEarnCoins = itemView.findViewById(R.id.iv_earn_coins);
            tvMain = itemView.findViewById(R.id.tv_main);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvNext = itemView.findViewById(R.id.tv_next);
            btnUnderstood = itemView.findViewById(R.id.btn_understood);
        }
    }
}