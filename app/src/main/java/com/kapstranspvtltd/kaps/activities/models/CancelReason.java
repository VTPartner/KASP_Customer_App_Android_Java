package com.kapstranspvtltd.kaps.activities.models;

public class CancelReason {
    private int reasonId;
    private String reason;

    public CancelReason(int reasonId, String reason) {
        this.reasonId = reasonId;
        this.reason = reason;
    }

    public int getReasonId() { return reasonId; }
    public String getReason() { return reason; }
}