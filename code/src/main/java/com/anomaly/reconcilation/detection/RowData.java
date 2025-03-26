package com.anomaly.reconcilation.detection;

/**
 * Data class to store row information for processing.
 */
public class RowData {
    double balanceDifference;
    double glBalance;
    double iHubBalance;
    String account;
    String comments;

    RowData(double balanceDifference, double glBalance, double iHubBalance, String account, String comments) {
        this.balanceDifference = balanceDifference;
        this.glBalance = glBalance;
        this.iHubBalance = iHubBalance;
        this.account = account;
        this.comments = comments;
    }
}
