package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

public class LogisticHistory {

	@SerializedName("package_number")
	private String packageNumber;

	@SerializedName("p_method_name")
	private String pMethodName;

	@SerializedName("total")
	private String total;

	@SerializedName("logistic_date")
	private String logisticDate;

	@SerializedName("orderid")
	private String orderid;

	@SerializedName("p_method_img")
	private String pMethodImg;

	@SerializedName("pick_address")
	private String pickAddress;

	@SerializedName("drop_address")
	private String dropAddress;

	@SerializedName("status")
	private String status;

	public String getPackageNumber(){
		return packageNumber;
	}

	public String getPMethodName(){
		return pMethodName;
	}

	public String getTotal(){
		return total;
	}

	public String getLogisticDate(){
		return logisticDate;
	}

	public String getOrderid(){
		return orderid;
	}

	public String getPMethodImg(){
		return pMethodImg;
	}

	public String getPickAddress(){
		return pickAddress;
	}

	public String getDropAddress(){
		return dropAddress;
	}

	public String getStatus(){
		return status;
	}
}