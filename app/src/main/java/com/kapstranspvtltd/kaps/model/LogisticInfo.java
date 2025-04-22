package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LogisticInfo{

	@SerializedName("transaction_id")
	private String transactionId;

	@SerializedName("p_method_name")
	private String pMethodName;

	@SerializedName("orderid")
	private String orderid;

	@SerializedName("productdata")
	private List<LogisticProduct> productdata;

	@SerializedName("drop_address")
	private String dropAddress;

	@SerializedName("drop_floor_no")
	private String dropFloorNo;

	@SerializedName("drop_long")
	private double dropLong;

	@SerializedName("package_number")
	private String packageNumber;

	@SerializedName("total")
	private String total;

	@SerializedName("pick_lat")
	private double pickLat;

	@SerializedName("drop_lat")
	private double dropLat;

	@SerializedName("logistic_date")
	private String logisticDate;

	@SerializedName("pick_lng")
	private double pickLng;

	@SerializedName("pick_has_lift")
	private String pickHasLift;

	@SerializedName("p_method_img")
	private String pMethodImg;

	@SerializedName("pick_address")
	private String pickAddress;

	@SerializedName("drop_has_lift")
	private String dropHasLift;

	@SerializedName("pick_floor_no")
	private String pickFloorNo;

	@SerializedName("status")
	private String status;

	@SerializedName("rider_name")
	private String riderName;

	@SerializedName("rider_img")
	private String riderImg;

	@SerializedName("parcel_weight")
	private String parcelWeight;


	@SerializedName("parcel_dimension")
	@Expose
	private List<String> parcelDimension;

	public String getTransactionId(){
		return transactionId;
	}

	public String getPMethodName(){
		return pMethodName;
	}

	public String getOrderid(){
		return orderid;
	}

	public List<LogisticProduct> getProductdata(){
		return productdata;
	}

	public String getDropAddress(){
		return dropAddress;
	}

	public String getDropFloorNo(){
		return dropFloorNo;
	}

	public double getDropLong(){
		return dropLong;
	}

	public String getPackageNumber(){
		return packageNumber;
	}

	public String getTotal(){
		return total;
	}

	public double getPickLat(){
		return pickLat;
	}

	public double getDropLat(){
		return dropLat;
	}

	public String getLogisticDate(){
		return logisticDate;
	}

	public double getPickLng(){
		return pickLng;
	}

	public String getPickHasLift(){
		return pickHasLift;
	}

	public String getPMethodImg(){
		return pMethodImg;
	}

	public String getPickAddress(){
		return pickAddress;
	}

	public String getDropHasLift(){
		return dropHasLift;
	}

	public String getPickFloorNo(){
		return pickFloorNo;
	}

	public String getStatus(){
		return status;
	}

	public String getRiderName() {
		return riderName;
	}

	public String getRiderImg() {
		return riderImg;
	}

	public String getParcelWeight() {
		return parcelWeight;
	}

	public List<String> getParcelDimension() {
		return parcelDimension;
	}
}