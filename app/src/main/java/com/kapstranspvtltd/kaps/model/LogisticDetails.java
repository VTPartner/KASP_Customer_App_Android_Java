package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

public class LogisticDetails{

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("LogisticInfo")
	private LogisticInfo logisticInfo;

	@SerializedName("Result")
	private String result;

	public String getResponseCode(){
		return responseCode;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public LogisticInfo getLogisticInfo(){
		return logisticInfo;
	}

	public String getResult(){
		return result;
	}
}