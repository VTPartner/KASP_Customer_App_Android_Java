package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PackersMovers implements Serializable {

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("CategoryData")
	private List<CategoryDataItem> categoryData;

	@SerializedName("ResponseMsg")
	private String responseMsg;


	@SerializedName("vehicle_Data")
	private VehicleData vehicledata;

	@SerializedName("Result")
	private String result;

	public String getResponseCode(){
		return responseCode;
	}

	public List<CategoryDataItem> getCategoryData(){
		return categoryData;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public String getResult(){
		return result;
	}

	public VehicleData getVehicledata() {
		return vehicledata;
	}

	public void setVehicledata(VehicleData vehicledata) {
		this.vehicledata = vehicledata;
	}
}