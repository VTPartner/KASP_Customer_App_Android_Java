package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Refercode{

	@SerializedName("pagelist")
	@Expose
	private List<Pages> pagelist = null;

	@SerializedName("refercredit")
	private String refercredit;

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("code")
	private String code;

	@SerializedName("signupcredit")
	private String signupcredit;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("mobile")
	private String mobile;

	@SerializedName("email")
	private String email;

	@SerializedName("Result")
	private String result;

	public String getRefercredit(){
		return refercredit;
	}

	public String getResponseCode(){
		return responseCode;
	}

	public String getCode(){
		return code;
	}

	public String getSignupcredit(){
		return signupcredit;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public String getMobile(){
		return mobile;
	}

	public String getEmail(){
		return email;
	}

	public String getResult(){
		return result;
	}

	public List<Pages> getPagelist() {
		return pagelist;
	}

	public void setPagelist(List<Pages> pagelist) {
		this.pagelist = pagelist;
	}
}