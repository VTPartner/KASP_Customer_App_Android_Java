package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Wallet{

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("wallet")
	private String wallets;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("Walletitem")
	private List<WalletitemItem> walletitem;

	@SerializedName("Result")
	private String result;

	public String getResponseCode(){
		return responseCode;
	}

	public String getWallets(){
		return wallets;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public List<WalletitemItem> getWalletitem(){
		return walletitem;
	}

	public String getResult(){
		return result;
	}
}