package com.kapstranspvtltd.kaps.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VehicleData implements Serializable {

    @SerializedName("ukms")
    @Expose
    private Integer ukms;
    @SerializedName("uprice")
    @Expose
    private Integer uprice;
    @SerializedName("aprice")
    @Expose
    private Integer aprice;

    public Integer getUkms() {
        return ukms;
    }

    public void setUkms(Integer ukms) {
        this.ukms = ukms;
    }

    public Integer getUprice() {
        return uprice;
    }

    public void setUprice(Integer uprice) {
        this.uprice = uprice;
    }

    public Integer getAprice() {
        return aprice;
    }

    public void setAprice(Integer aprice) {
        this.aprice = aprice;
    }

}
