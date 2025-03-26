package com.anomaly.reconcilation.detection;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;

public class ReconciliationAnomalyDetector {

    private Map<String, Double> meanBalanceDiffByAccount;
    private Map<String, Double> stdDevBalanceDiffByAccount;
    private double glMean;
    private double glStdDev;
    private double iHubMean;
    private double iHubStdDev;

    /**
     * Main method to execute the anomaly detection process.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        ReconciliationAnomalyDetector detector = new ReconciliationAnomalyDetector();
        detector.process();
    }

    /**
     * Orchestrates the entire anomaly detection process.
     */
    public void process() {
        List<String[]> historicalRows = readHistoricalData(Constants.INPUT_FILE_PATH);
        if (historicalRows.isEmpty()) {
            System.err.println("Failed to read historical data. Exiting.");
            return;
        }

        computeHistoricalStats(historicalRows);
        List<String[]> realTimeRows = createRealTimeData();
        List<String[]> updatedRows = detectAnomalies(realTimeRows);
        writeOutputData(updatedRows, Constants.OUTPUT_FILE_PATH);
        sendEmailNotification(Constants.OUTPUT_FILE_PATH);
    }

    /**
     * Reads historical data from the input CSV file.
     *
     * @param filePath Path to the input CSV file.
     * @return List of rows from the CSV file, including the header.
     */
    private List<String[]> readHistoricalData(String filePath) {
        List<String[]> rows = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rows.add(row);
            }
        } catch (Exception e) {
            System.err.println("Error reading historical data: " + e.getMessage());
        }
        return rows;
    }

    /**
     * Computes historical statistics (mean and standard deviation) for balance differences,
     * GL balances, and iHub balances.
     *
     * @param historicalRows List of historical data rows.
     */
    private void computeHistoricalStats(List<String[]> historicalRows) {
        List<RowData> historicalData = new ArrayList<>();
        for (int i = 1; i < historicalRows.size(); i++) { // Skip header
            String[] row = historicalRows.get(i);
            double balanceDiff = Double.parseDouble(row[8]);
            double glBalance = Double.parseDouble(row[6]);
            double iHubBalance = Double.parseDouble(row[7]);
            String account = row[2];
            String comments = row[10];
            historicalData.add(new RowData(balanceDiff, glBalance, iHubBalance, account, comments));
        }

        // Compute mean and std dev for balance differences per account
        Map<String, List<Double>> balanceDiffByAccount = new HashMap<>();
        for (RowData data : historicalData) {
            balanceDiffByAccount.computeIfAbsent(data.account, k -> new ArrayList<>()).add(data.balanceDifference);
        }

        meanBalanceDiffByAccount = new HashMap<>();
        stdDevBalanceDiffByAccount = new HashMap<>();
        for (String account : balanceDiffByAccount.keySet()) {
            List<Double> diffs = balanceDiffByAccount.get(account);
            double mean = diffs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double stdDev = Math.sqrt(diffs.stream()
                    .mapToDouble(val -> Math.pow(val - mean, 2))
                    .average().orElse(0.0));
            meanBalanceDiffByAccount.put(account, mean);
            stdDevBalanceDiffByAccount.put(account, stdDev);
        }

        // Compute mean and std dev for GL and iHub balances
        glMean = historicalData.stream().mapToDouble(data -> data.glBalance).average().orElse(0.0);
        glStdDev = Math.sqrt(historicalData.stream()
                .mapToDouble(data -> Math.pow(data.glBalance - glMean, 2))
                .average().orElse(0.0));
        iHubMean = historicalData.stream().mapToDouble(data -> data.iHubBalance).average().orElse(0.0);
        iHubStdDev = Math.sqrt(historicalData.stream()
                .mapToDouble(data -> Math.pow(data.iHubBalance - iHubMean, 2))
                .average().orElse(0.0));
    }

    /**
     * Creates the real-time data as per the image (lines 3–7).
     *
     * @return List of real-time data rows, including the header.
     */
    private List<String[]> createRealTimeData() {
        List<String[]> realTimeRows = new ArrayList<>();
        realTimeRows.add(new String[]{"Date", "Company", "Account", "Currency", "Primary Account", "Secondary Account", "GL Balance", "iHub Balance", "Balance Difference", "Match Status", "Comments", "is_anomaly", "anomaly_reason"});
        realTimeRows.add(new String[]{"5/31/2024", "00000", "1619205", "4398 USD", "ALL OTHER LOANS", "DEFERRED COSTS", "20000", "35000", "-15000", "Break", "Inconsistent variations in outstanding balances"});
        realTimeRows.add(new String[]{"5/31/2024", "00000", "1619001", "6295 USD", "ALL OTHER LOANS", "PRINCIPAL", "60000", "65000", "-5000", "Break", "Huge spike in outstanding balances"});
        realTimeRows.add(new String[]{"5/31/2024", "00002", "1619205", "4929 USD", "ALL OTHER LOANS", "PRINCIPAL", "70000", "80000", "-10000", "Break", "Huge spike in outstanding balances"});
        realTimeRows.add(new String[]{"5/31/2024", "00000", "1619128", "5701 USD", "ALL OTHER LOANS", "DEFERRED ORIGINATION FEES", "6000", "12000", "-6000", "Break", "Inconsistent variations in outstanding balances"});
        realTimeRows.add(new String[]{"5/31/2024", "00002", "1619128", "4929 USD", "ALL OTHER LOANS", "DEFERRED ORIGINATION FEES", "4000", "10000", "-6000", "Break", "Inconsistent variations in outstanding balances"});
        return realTimeRows;
    }

    /**
     * Detects anomalies in the real-time data and adds is_anomaly and anomaly_reason columns.
     *
     * @param realTimeRows List of real-time data rows.
     * @return Updated list of rows with anomaly detection results.
     */
    private List<String[]> detectAnomalies(List<String[]> realTimeRows) {
        List<String[]> updatedRows = new ArrayList<>();
        updatedRows.add(realTimeRows.get(0)); // Add header

        for (int i = 1; i < realTimeRows.size(); i++) {
            String[] row = realTimeRows.get(i);
            List<String> newRow = new ArrayList<>(List.of(row));
            double balanceDiff = Double.parseDouble(row[8]);
            double absBalanceDiff = Math.abs(balanceDiff);
            double glBalance = Double.parseDouble(row[6]);
            double iHubBalance = Double.parseDouble(row[7]);
            String account = row[2];
            String comments = row[10];

            // Statistical check: Z-score
            double meanBalanceDiff = meanBalanceDiffByAccount.getOrDefault(account, 0.0);
            double stdDevBalanceDiff = stdDevBalanceDiffByAccount.getOrDefault(account, 0.0);
            boolean isStatisticalAnomaly = stdDevBalanceDiff > 0 && Math.abs(balanceDiff - meanBalanceDiff) > Constants.Z_SCORE_THRESHOLD * stdDevBalanceDiff;

            // Rule-based check
            boolean isRuleBasedAnomaly = absBalanceDiff > Constants.BALANCE_DIFF_THRESHOLD;

            // Combine checks
            String isAnomaly = (isStatisticalAnomaly || isRuleBasedAnomaly) ? "Yes" : "No";
            String anomalyReason = comments.isEmpty() ? "No anomaly detected" : comments;

            if (isAnomaly.equals("Yes") && comments.isEmpty()) {
                if (absBalanceDiff > 10000) {
                    anomalyReason = "Huge spike in outstanding balances";
                } else if (absBalanceDiff >= 1000) {
                    anomalyReason = "Inconsistent variations in outstanding balances";
                }
            } else if (isAnomaly.equals("No") && comments.isEmpty()) {
                anomalyReason = "Difference is within tolerance";
            }

            // Add the new columns (is_anomaly and anomaly_reason)
            newRow.add(isAnomaly);
            newRow.add(anomalyReason);
            updatedRows.add(newRow.toArray(new String[0]));
        }
        return updatedRows;
    }

    /**
     * Writes the updated rows to the output CSV file.
     *
     * @param updatedRows List of rows to write.
     * @param filePath Path to the output CSV file.
     */
    private void writeOutputData(List<String[]> updatedRows, String filePath) {
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(filePath))) {
            csvWriter.writeAll(updatedRows);
            System.out.println("Output CSV generated: " + filePath);
        } catch (Exception e) {
            System.err.println("Error writing output data: " + e.getMessage());
        }
    }

    /**
     * Sends an email notification with the output file path.
     *
     * @param outputFilePath Path to the output CSV file.
     */
    private void sendEmailNotification(String outputFilePath) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", Constants.EMAIL_HOST);
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Constants.EMAIL_USERNAME, Constants.EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Constants.EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Constants.EMAIL_TO));
            message.setSubject(Constants.EMAIL_SUBJECT);
            message.setText("The reconciliation process has completed. Please find the results in the attached CSV file: " + outputFilePath);
            Transport.send(message);
            System.out.println("Email notification sent successfully!");
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}