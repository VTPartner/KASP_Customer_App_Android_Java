package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MainData {


    @SerializedName("currency")
    @Expose
    private String currency;


    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }



}
