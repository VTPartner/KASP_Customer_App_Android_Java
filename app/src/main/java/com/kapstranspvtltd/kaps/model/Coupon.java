
package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class Coupon implements Serializable {
    private int couponId;
    private String couponCode;
    private String couponTitle;
    private String couponDescription;
    private long categoryId;
    private String discountType;
    private double discountValue;
    private double minOrderValue;
    private double maxDiscount;
    private int usageLimit;
    private int usedCount;
    private String startDate;
    private String endDate;
    private int status;
    private double timeCreatedAt;


    // Constructor
    public Coupon(int couponId, String couponCode, String couponTitle, String couponDescription,
                  int categoryId, String discountType, double discountValue, double minOrderValue,
                  double maxDiscount, int usageLimit, int usedCount, String startDate, String endDate,
                  int status, double timeCreatedAt) {
        this.couponId = couponId;
        this.couponCode = couponCode;
        this.couponTitle = couponTitle;
        this.couponDescription = couponDescription;
        this.categoryId = categoryId;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.maxDiscount = maxDiscount;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.timeCreatedAt = timeCreatedAt;

    }

    // Getters and Setters
    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCouponTitle() {
        return couponTitle;
    }

    public void setCouponTitle(String couponTitle) {
        this.couponTitle = couponTitle;
    }

    public String getCouponDescription() {
        return couponDescription;
    }

    public void setCouponDescription(String couponDescription) {
        this.couponDescription = couponDescription;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public double getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(double maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getTimeCreatedAt() {
        return timeCreatedAt;
    }

    public void setTimeCreatedAt(double timeCreatedAt) {
        this.timeCreatedAt = timeCreatedAt;
    }


}
