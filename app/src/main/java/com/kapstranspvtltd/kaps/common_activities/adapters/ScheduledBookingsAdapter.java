package com.kapstranspvtltd.kaps.common_activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.common_activities.models.ScheduledBooking;
import com.kapstranspvtltd.kaps.databinding.ItemScheduledBookingBinding;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduledBookingsAdapter extends RecyclerView.Adapter<ScheduledBookingsAdapter.BookingViewHolder> {
    private Context context;
    private List<ScheduledBooking> bookings = new ArrayList<>();
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(ScheduledBooking booking, int position);
    }

    public ScheduledBookingsAdapter(Context context, OnBookingClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setBookings(List<ScheduledBooking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduledBookingBinding binding = ItemScheduledBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private ItemScheduledBookingBinding binding;

        BookingViewHolder(ItemScheduledBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScheduledBooking booking) {
            // Format and display scheduled date and time
            String scheduledDateTime = formatDateTime(booking.getScheduled_time());
            System.out.println("scheduledDateTime::"+scheduledDateTime);
            binding.txtDate.setText(scheduledDateTime);

            // Set service type and name
            String serviceType = booking.getService_name().isEmpty() ? 
                               booking.getSub_cat_name() : 
                               booking.getSub_cat_name() + " / " + booking.getService_name();
            binding.txtType.setText(serviceType);

            // Set price
            binding.txtTotal.setText(formatCurrency(booking.getTotal_price()));

            // Set addresses
            binding.txtPickaddress.setText(booking.getPickup_address());
            
            // Show/hide drop address based on service type
            if (booking.getCategory_id().equals("1") || booking.getCategory_id().equals("2")) {
                binding.txtDropaddress.setVisibility(View.VISIBLE);
                binding.txtDropaddress.setText(booking.getDrop_address());
            } else {
                binding.txtDropaddress.setVisibility(View.GONE);
            }

            // Set booking status and ID
            binding.txtStatus.setText(booking.getBooking_status());
            binding.bookingId.setText("# CRN " + booking.getBooking_id());

            // Load category image
            Glide.with(context)
                    .load(booking.getCategory_image())
                    .placeholder(R.drawable.placeholder)
                    .into(binding.imgIcon);

            // Set click listener
            binding.lvlClick.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking, getAdapterPosition());
                }
            });
        }

        private String formatDateTime(String timestamp) {
            try {
                long epochTime;
                if (timestamp.contains(".")) {
                    epochTime = Long.parseLong(timestamp.substring(0, timestamp.indexOf(".")));
                } else {
                    epochTime = Long.parseLong(timestamp);
                }

                Date date = new Date(epochTime * 1000L);
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM dd, hh:mm a", Locale.getDefault());
                return outputFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
                return timestamp;
            }
        }

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
}