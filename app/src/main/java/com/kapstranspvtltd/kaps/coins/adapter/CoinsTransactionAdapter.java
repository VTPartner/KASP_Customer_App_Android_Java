package com.kapstranspvtltd.kaps.coins.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.coins.model.CoinTransaction;
import com.kapstranspvtltd.kaps.databinding.ItemCoinTransactionBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CoinsTransactionAdapter extends RecyclerView.Adapter<CoinsTransactionAdapter.ViewHolder> {
    private List<CoinTransaction> transactions = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCoinTransactionBinding binding = ItemCoinTransactionBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void submitList(List<CoinTransaction> newList) {
        transactions.clear();
        transactions.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCoinTransactionBinding binding;
        private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        public ViewHolder(ItemCoinTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CoinTransaction transaction) {
            binding.tvCoinsAmount.setText(transaction.getCoinsEarned() + " coins");
            binding.tvOrderId.setText("Order #" + transaction.getOrderId());
            binding.tvRemarks.setText(transaction.getRemarks());

            try {
                Date earnedDate = inputFormat.parse(transaction.getEarnedAt());
                if (earnedDate != null) {
                    binding.tvDate.setText(dateFormat.format(earnedDate));
                    binding.tvTime.setText(timeFormat.format(earnedDate));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Set status
            if (transaction.isUsed()) {
                binding.tvStatus.setText("Used");
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.colorerror));
            } else {
                binding.tvStatus.setText("Available");
                binding.tvStatus.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.green));
            }
        }
    }
}