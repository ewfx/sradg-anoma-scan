package com.anomaly.reconcilation.detection;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class to detect anomalies in financial reconciliation data using DeepLearning4J.
 */
public class ReconciliationAnomalyDetector {

    private MultiLayerNetwork autoencoder;
    private double[] featureMeans;
    private double[] featureStds;

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

        trainAutoencoder(historicalRows);
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
     * Trains an Autoencoder on historical data to learn normal patterns.
     *
     * @param historicalRows List of historical data rows.
     */
    private void trainAutoencoder(List<String[]> historicalRows) {
        List<double[]> featuresList = new ArrayList<>();
        for (int i = 1; i < historicalRows.size(); i++) { // Skip header
            String[] row = historicalRows.get(i);
            double glBalance = Double.parseDouble(row[6]);
            double iHubBalance = Double.parseDouble(row[7]);
            double balanceDiff = Double.parseDouble(row[8]);
            featuresList.add(new double[]{glBalance, iHubBalance, balanceDiff});
        }

        // Convert to INDArray
        INDArray features = Nd4j.create(featuresList);

        // Normalize the features
        featureMeans = new double[Constants.INPUT_SIZE];
        featureStds = new double[Constants.INPUT_SIZE];
        for (int i = 0; i < Constants.INPUT_SIZE; i++) {
            featureMeans[i] = features.getColumn(i).meanNumber().doubleValue();
            featureStds[i] = features.getColumn(i).stdNumber().doubleValue();
            if (featureStds[i] == 0) featureStds[i] = 1.0; // Avoid division by zero
            features.putColumn(i, features.getColumn(i).subi(featureMeans[i]).divi(featureStds[i]));
        }

        // Create DataSet
        DataSet dataSet = new DataSet(features, features); // Autoencoder: input = output

        // Build the Autoencoder
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(Constants.LEARNING_RATE))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(Constants.INPUT_SIZE)
                        .nOut(Constants.HIDDEN_SIZE)
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(Constants.HIDDEN_SIZE)
                        .nOut(Constants.INPUT_SIZE)
                        .activation(Activation.IDENTITY)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .build();

        autoencoder = new MultiLayerNetwork(conf);
        autoencoder.init();

        // Train the Autoencoder
        for (int i = 0; i < Constants.EPOCHS; i++) {
            autoencoder.fit(dataSet);
        }
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
     * Detects anomalies in the real-time data using the trained Autoencoder.
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
            double glBalance = Double.parseDouble(row[6]);
            double iHubBalance = Double.parseDouble(row[7]);
            double balanceDiff = Double.parseDouble(row[8]);
            String comments = row[10];

            // Normalize the input
            double[] input = new double[]{glBalance, iHubBalance, balanceDiff};
            for (int j = 0; j < input.length; j++) {
                input[j] = (input[j] - featureMeans[j]) / featureStds[j];
            }

            // Compute reconstruction error
            INDArray inputArray = Nd4j.create(input);
            INDArray outputArray = autoencoder.output(inputArray);
            double reconstructionError = inputArray.distance2(outputArray);

            // Flag as anomaly if reconstruction error exceeds threshold
            String isAnomaly = reconstructionError > Constants.RECONSTRUCTION_ERROR_THRESHOLD ? "Yes" : "No";
            String anomalyReason = comments.isEmpty() ? "No anomaly detected" : comments;

            if (isAnomaly.equals("Yes") && comments.isEmpty()) {
                double absBalanceDiff = Math.abs(balanceDiff);
                if (absBalanceDiff > 10000) {
                    anomalyReason = "Huge spike in outstanding balances";
                } else if (absBalanceDiff >= 1000) {
                    anomalyReason = "Inconsistent variations in outstanding balances";
                }
            } else if (isAnomaly.equals("No") && comments.isEmpty()) {
                anomalyReason = "Difference is within tolerance";
            }

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