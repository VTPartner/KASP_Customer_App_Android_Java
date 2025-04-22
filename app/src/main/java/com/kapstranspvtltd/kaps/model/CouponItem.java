package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

public class CouponItem {

	@SerializedName("coupon_title")
	private String couponTitle;

	@SerializedName("cdate")
	private String cdate;

	@SerializedName("coupon_code")
	private String couponCode;

	@SerializedName("c_desc")
	private String cDesc;

	@SerializedName("c_value")
	private String cValue;

	@SerializedName("min_amt")
	private int minAmt;

	@SerializedName("c_img")
	private String cImg;

	@SerializedName("id")
	private String id;

	@SerializedName("subtitle")
	private String subtitle;

	public String getCouponTitle(){
		return couponTitle;
	}

	public String getCdate(){
		return cdate;
	}

	public String getCouponCode(){
		return couponCode;
	}

	public String getCDesc(){
		return cDesc;
	}

	public String getCValue(){
		return cValue;
	}

	public int getMinAmt(){
		return minAmt;
	}

	public String getCImg(){
		return cImg;
	}

	public String getId(){
		return id;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
}