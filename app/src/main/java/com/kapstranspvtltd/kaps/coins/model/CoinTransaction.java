package com.kapstranspvtltd.kaps.coins.model;

public class CoinTransaction {
    private long coinId;
    private int coinsEarned;
    private String earnedAt;
    private String expiresAt;
    private Long orderId;
    private String remarks;
    private boolean isUsed;

    public CoinTransaction(long coinId, int coinsEarned, String earnedAt, String expiresAt, 
                         Long orderId, String remarks, boolean isUsed) {
        this.coinId = coinId;
        this.coinsEarned = coinsEarned;
        this.earnedAt = earnedAt;
        this.expiresAt = expiresAt;
        this.orderId = orderId;
        this.remarks = remarks;
        this.isUsed = isUsed;
    }

    // Getters
    public long getCoinId() { return coinId; }
    public int getCoinsEarned() { return coinsEarned; }
    public String getEarnedAt() { return earnedAt; }
    public String getExpiresAt() { return expiresAt; }
    public Long getOrderId() { return orderId; }
    public String getRemarks() { return remarks; }
    public boolean isUsed() { return isUsed; }
}