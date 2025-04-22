package com.kapstranspvtltd.kaps.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.models.AllDriverBookingsModel;
import com.kapstranspvtltd.kaps.databinding.ItemGoodsBookingBinding;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentBookingsAdapterDrivers extends RecyclerView.Adapter<RecentBookingsAdapterDrivers.BookingViewHolder> {
    private Context context;
    private List<AllDriverBookingsModel> bookings = new ArrayList<>();
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(AllDriverBookingsModel booking, int position);
    }

    public RecentBookingsAdapterDrivers(Context context, OnBookingClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setBookings(List<AllDriverBookingsModel> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoodsBookingBinding binding = ItemGoodsBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(bookings.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private ItemGoodsBookingBinding binding;

        BookingViewHolder(ItemGoodsBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AllDriverBookingsModel booking) {


            binding.imgIcon.setVisibility(View.GONE);

            String bookingTiming = booking.getBooking_timing();
            String subCatName = booking.getSub_cat_name();

            String serviceName = booking.getService_name();

            bookingTiming = formatDateTime(bookingTiming);
            binding.txtDate.setText(bookingTiming);
            binding.txtType.setText(serviceName.isEmpty() == false ? subCatName+" / "+serviceName : subCatName);
            binding.txtTotle.setText(formatCurrency(booking.getTotal_price()));
            binding.txtPickaddress.setText(booking.getPickup_address());
            binding.txtStatus.setText(booking.getBooking_status());
            binding.txtDropaddress.setText(booking.getDrop_address());
            binding.bookingId.setText("# CRN "+booking.getBooking_id());
//            Glide.with(context)
//                    .load(booking.getVehicle_image())
//                    .placeholder(R.drawable.placeholder)
//                    .into(binding.imgIcon);

            binding.lvlClick.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking, getAdapterPosition());
                }
            });

            // Handle drop address
//            binding.lvlDrop.removeAllViews();
//            View dropView = LayoutInflater.from(context)
//                .inflate(R.layout.custome_droplocation, binding.lvlDrop, false);
//            TextView txtDropAddress = dropView.findViewById(R.id.txt_dropaddress);
//            txtDropAddress.setText(booking.getDrop_address());
//            binding.lvlDrop.addView(dropView);
        }

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