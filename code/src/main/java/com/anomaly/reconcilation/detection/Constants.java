package com.anomaly.reconcilation.detection;

/**
 * Constants class to store all constant values used in the anomaly detection process.
 */
public class Constants {
    // File paths
    public static final String INPUT_FILE_PATH = "input_data.csv";
    public static final String OUTPUT_FILE_PATH = "output_data.csv";

    // Anomaly detection thresholds
    public static final double BALANCE_DIFF_THRESHOLD = 5000.0;
    public static final double Z_SCORE_THRESHOLD = 1.0;

    // Email configuration
    public static final String EMAIL_FROM = "your_email@example.com";
    public static final String EMAIL_TO = "recipient@example.com";
    public static final String EMAIL_HOST = "smtp.example.com";
    public static final String EMAIL_USERNAME = "your_email@example.com";
    public static final String EMAIL_PASSWORD = "your_password";
    public static final String EMAIL_SUBJECT = "Reconciliation and Anomaly Detection Results";
}

