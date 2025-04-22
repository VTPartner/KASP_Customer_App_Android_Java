package com.kapstranspvtltd.kaps.utility;

import android.os.Parcel;
import android.os.Parcelable;

public class Drop implements Parcelable {

    double lat;
    double log;
    String address;
    String rname;
    String rmobile;

    public Drop(Parcel in) {
        lat = in.readDouble();
        log = in.readDouble();
        address = in.readString();
        rname = in.readString();
        rmobile = in.readString();
    }

    public static final Creator<Drop> CREATOR = new Creator<Drop>() {
        @Override
        public Drop createFromParcel(Parcel in) {
            return new Drop(in);
        }

        @Override
        public Drop[] newArray(int size) {
            return new Drop[size];
        }
    };

    public Drop() {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRname() {
        return rname;
    }

    public void setRname(String rname) {
        this.rname = rname;
    }

    public String getRmobile() {
        return rmobile;
    }

    public void setRmobile(String rmobile) {
        this.rmobile = rmobile;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(log);
        parcel.writeString(address);
        parcel.writeString(rname);
        parcel.writeString(rmobile);
    }
}
