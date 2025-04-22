package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Wheele{

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("Vehiclelist")
	private List<Wheeleritem> vehiclelist;

	@SerializedName("Paymentdata")
	private List<PaymentItem> paymentItems;

	@SerializedName("Categorylist")
	private List<WCategory> wCategories;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("Result")
	private String result;

	public String getResponseCode(){
		return responseCode;
	}

	public List<Wheeleritem> getVehiclelist(){
		return vehiclelist;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public String getResult(){
		return result;
	}

	public List<PaymentItem> getPaymentItems() {
		return paymentItems;
	}

	public void setPaymentItems(List<PaymentItem> paymentItems) {
		this.paymentItems = paymentItems;
	}

	public List<WCategory> getCategories() {
		return wCategories;
	}

	public void setCategories(List<WCategory> wCategories) {
		this.wCategories = wCategories;
	}
}