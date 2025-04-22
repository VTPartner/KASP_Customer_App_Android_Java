package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Orders{

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("Historyinfo")
	private List<HistoryinfoItem> historyinfo;

	@SerializedName("Result")
	private String result;

	public void setResponseCode(String responseCode){
		this.responseCode = responseCode;
	}

	public String getResponseCode(){
		return responseCode;
	}

	public void setResponseMsg(String responseMsg){
		this.responseMsg = responseMsg;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public void setHistoryinfo(List<HistoryinfoItem> historyinfo){
		this.historyinfo = historyinfo;
	}

	public List<HistoryinfoItem> getHistoryinfo(){
		return historyinfo;
	}

	public void setResult(String result){
		this.result = result;
	}

	public String getResult(){
		return result;
	}
}