package com.kapstranspvtltd.kaps.coins.model;

public class CoinsSummary {
    private int totalCoins;
    private int availableCoins;
    private int expiringCoins;

    public CoinsSummary(int totalCoins, int availableCoins, int expiringCoins) {
        this.totalCoins = totalCoins;
        this.availableCoins = availableCoins;
        this.expiringCoins = expiringCoins;
    }

    // Getters
    public int getTotalCoins() { return totalCoins; }
    public int getAvailableCoins() { return availableCoins; }
    public int getExpiringCoins() { return expiringCoins; }
}