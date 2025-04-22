package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Logistic{

	@SerializedName("LogisticHistory")
	private List<LogisticHistory> logisticHistory;

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("Result")
	private String result;

	public List<LogisticHistory> getLogisticHistory(){
		return logisticHistory;
	}

	public String getResponseCode(){
		return responseCode;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public String getResult(){
		return result;
	}
}