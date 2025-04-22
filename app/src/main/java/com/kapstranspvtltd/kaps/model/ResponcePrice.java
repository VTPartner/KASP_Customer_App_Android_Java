package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

public class ResponcePrice{

	@SerializedName("ResponseCode")
	private int responseCode;

	@SerializedName("vehicle_Data")
	private VehicleData vehicleData;

	@SerializedName("KGPRICE")
	private int kGPRICE;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("MAXLIMIT")
	private int mAXLIMIT;

	@SerializedName("Result")
	private boolean result;

	public int getResponseCode(){
		return responseCode;
	}

	public VehicleData getVehicleData(){
		return vehicleData;
	}

	public int getKGPRICE(){
		return kGPRICE;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public int getMAXLIMIT(){
		return mAXLIMIT;
	}

	public boolean isResult(){
		return result;
	}
}