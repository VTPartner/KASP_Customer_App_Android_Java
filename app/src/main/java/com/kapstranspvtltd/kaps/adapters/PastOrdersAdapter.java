package com.kapstranspvtltd.kaps.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.activities.models.AllGoodsOrders;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ItemPastOrderBinding;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastOrdersAdapter extends RecyclerView.Adapter<PastOrdersAdapter.OrderViewHolder> {
    private Context context;
    private List<AllGoodsOrders> orders = new ArrayList<>();
    private OnOrderClickListener listener;


    public interface OnOrderClickListener {
        void onOrderClick(AllGoodsOrders order, int position);
    }

    public PastOrdersAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrders(List<AllGoodsOrders> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPastOrderBinding binding = ItemPastOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(orders.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ItemPastOrderBinding binding;

        OrderViewHolder(ItemPastOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AllGoodsOrders order) {
            // Set basic order details
            String bookingTiming = order.getBooking_timing();
            bookingTiming = formatDateTime(bookingTiming);
            binding.txtDate.setText(bookingTiming);
            binding.txtType.setText(order.getVehicle_name());
            binding.txtTotle.setText(formatCurrency(order.getTotal_price()));
            binding.txtPickaddress.setText(order.getPickup_address());
            binding.txtDropaddress.setText(order.getDrop_address());

            // Set vehicle image using Glide
            Glide.with(context)
                    .load(order.getVehicle_image())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(binding.imgIcon);

            // Set sender and receiver details
            String orderDetails = "Sender: " + order.getSender_name() +
                    " | Receiver: " + order.getReceiver_name();
            binding.txtType.setText(orderDetails);
binding.orderId.setText("# CRN "+order.getOrder_id());
            // Handle drop location
//            binding.lvlDrop.removeAllViews();
//            View dropView = LayoutInflater.from(context)
//                    .inflate(R.layout.custome_droplocation, binding.lvlDrop, false);
//            TextView txtDropAddress = dropView.findViewById(R.id.txt_dropaddress);
//            txtDropAddress.setText(order.getDrop_address());
//            binding.lvlDrop.addView(dropView);

            // Set payment method and time
            String timeAndPayment = order.getTotal_time() + " | " +
                    order.getPayment_method();
//            binding.txtStatus.setText(timeAndPayment);

            // Set click listener
            binding.lvlClick.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order, getAdapterPosition());
                }
            });

            // Optional: Add contact buttons
            binding.btnCallSender.setOnClickListener(v -> {
                dialPhoneNumber(order.getSender_number());
            });

            binding.btnCallReceiver.setOnClickListener(v -> {
                dialPhoneNumber(order.getReceiver_number());
            });

            binding.txtPaymentMethod.setText(order.getPayment_method());
        }

        private void dialPhoneNumber(String phoneNumber) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Cannot make phone call", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to format date/time if needed
    private String formatDateTime(String timestamp) {
        try {
            // Parse the epoch timestamp (removing any decimal points and taking first 10 digits)
            long epochTime;
            if (timestamp.contains(".")) {
                epochTime = Long.parseLong(timestamp.substring(0, timestamp.indexOf(".")));
            } else {
                epochTime = Long.parseLong(timestamp);
            }

            // Convert epoch to Date
            Date date = new Date(epochTime * 1000L); // Multiply by 1000 to convert seconds to milliseconds

            // Format the date
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM dd, hh:mm a", Locale.getDefault());
            return outputFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            return timestamp; // Return original string if parsing fails
        }
    }

    // Helper method to format currency
    private String formatCurrency(String amount) {
        try {
            double value = Double.parseDouble(amount);
            DecimalFormat formatter = new DecimalFormat("₹#,##,##0.00");
            return formatter.format(value);
        } catch (NumberFormatException e) {
            return "₹" + amount;
        }
    }
}