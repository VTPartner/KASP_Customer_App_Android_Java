package com.kapstranspvtltd.kaps.coins.model;

public class EarnCoinPage {
    public int imageRes;
    public String mainText;
    public String desc;
    public boolean isLast;

    public EarnCoinPage(int imageRes, String mainText, String desc, boolean isLast) {
        this.imageRes = imageRes;
        this.mainText = mainText;
        this.desc = desc;
        this.isLast = isLast;
    }
}