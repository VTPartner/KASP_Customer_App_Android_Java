package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.model.Coupon;

import java.util.List;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    private List<Coupon> couponList;
    private OnCouponClickListener listener;
    private boolean isCouponApplied = false;

    public interface OnCouponClickListener {
        void onCouponClick(Coupon coupon);
    }

    public CouponAdapter(List<Coupon> couponList, OnCouponClickListener listener) {
        this.couponList = couponList;
        this.listener = listener;
    }

    public void setCouponApplied(boolean isCouponApplied) {
        this.isCouponApplied = isCouponApplied;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = couponList.get(position);
        holder.txtCoupon.setText(coupon.getCouponCode());
        holder.txtAmount.setText(String.format("₹ %.2f", coupon.getDiscountValue()));
        holder.txtTitle.setText(coupon.getCouponTitle());
        holder.txtDesc.setText(coupon.getCouponDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCouponApplied) {
                    listener.onCouponClick(coupon);
                } else {
                    Toast.makeText(v.getContext(), "Only one coupon can be applied at a time.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    public static class CouponViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtCoupon, txtAmount, txtApply, txtTitle, txtDesc;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            txtCoupon = itemView.findViewById(R.id.txt_coupon);
            txtAmount = itemView.findViewById(R.id.txt_amount);
            txtApply = itemView.findViewById(R.id.txt_apply);
            txtTitle = itemView.findViewById(R.id.txt_titel);
            txtDesc = itemView.findViewById(R.id.txt_desc);
        }
    }
}