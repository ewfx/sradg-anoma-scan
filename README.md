# 🚀 Project Name

## 📌 Table of Contents
- [Introduction](#introduction)
  The Anomaly Detection for Financial Reconciliation project leverages deep learning to identify discrepancies in financial data between two systems, General Ledger (GL) and iHub, by analyzing balance differences. Using DeepLearning4J (DL4J), we implemented an Autoencoder to detect anomalies in real-time data by learning patterns from historical data. This tool helps financial teams flag inconsistencies, such as significant balance differences, and provides actionable insights for reconciliation. The project includes a Java-based backend for processing data, an Autoencoder for anomaly detection, and a proposed UI dashboard for user interaction, enabling file uploads, result visualization, and email notifications.
- [Demo](#demo)
-Here’s a step-by-step walkthrough of how the system works:

Upload Historical Data:
The user uploads input_data.csv, which contains historical financial data (e.g., 6 months of GL and iHub balances).
Example: Historical data includes columns like Date, Company, Account, GL Balance, iHub Balance, Balance Difference, etc.
Train the Autoencoder:
The system uses DeepLearning4J to train an Autoencoder on the historical data, learning the "normal" patterns of GL Balance, iHub Balance, and Balance Difference.
Process Real-Time Data:
The system processes real-time data (currently hardcoded, but can be extended to upload a file).
Example: Real-time data includes 5 rows with significant balance differences (e.g., -15,000, -10,000).
Detect Anomalies:
The trained Autoencoder computes the reconstruction error for each real-time row. Rows with high reconstruction errors (above a threshold) are flagged as anomalies.
Results are written to output_data.csv with columns is_anomaly and anomaly_reason.
View Results on Dashboard (Proposed UI):
The dashboard displays:
Total rows processed: 5
Number of issues (anomalies): 5
A "View Details" link to see the full output in a table.
A "Send Email Notification" button triggers an email with the results.
- [Inspiration](#inspiration)
- The inspiration for this project came from the need to automate financial reconciliation processes in organizations. Manual reconciliation of GL and iHub balances is time-consuming and error-prone, especially when dealing with large datasets. We noticed that significant balance differences often indicate data entry errors, system mismatches, or fraudulent activities. By using deep learning with DeepLearning4J, we aimed to create a more robust and scalable anomaly detection system that can learn complex patterns in financial data, improving accuracy over traditional statistical methods.
- [What It Does](#what-it-does)
- Anomaly Detection with Deep Learning: Uses an Autoencoder built with DeepLearning4J to identify discrepancies in financial data by comparing real-time balance differences against learned patterns from historical data.
  Output Generation: Produces a detailed CSV file (output_data.csv) with columns indicating whether each row is an anomaly (is_anomaly) and the reason for the anomaly (anomaly_reason).
  Notification: Sends an email notification with the results, ensuring stakeholders are informed.
  Proposed UI Dashboard: Allows users to upload historical data, view the number of anomalies, and send email notifications with a single click.
- [How We Built It](#how-we-built-it)
- Data Processing
  Used Java to read historical data from input_data.csv and process real-time data (hardcoded for now).
  Extracted features (GL Balance, iHub Balance, Balance Difference) for training the Autoencoder.
  Autoencoder with DeepLearning4J:
  Built a simple Autoencoder with 3 input nodes (for the features), 2 hidden nodes, and 3 output nodes.
  Normalized the features using mean and standard deviation to improve training performance.
  Trained the Autoencoder on historical data for 100 epochs using the Adam optimizer.
  Used the reconstruction error to detect anomalies: rows with errors above a threshold (0.1) are flagged as anomalies.
  Output Generation:
  Added is_anomaly and anomaly_reason columns to the output CSV, aligning with the input comments (e.g., "Huge spike in outstanding balances").
  Email Notification:
  Integrated JavaMail API to send email notifications with the output file path.
  Proposed UI Dashboard:
  Designed a wireframe for a dashboard with sections for file upload, summary (total rows, number of anomalies), and an email notification button.
  Code Structure:
  Followed Java best practices with small,focused methods, detailed JavaDocs, and constants for thresholds and configurations.
- [Challenges We Faced](#challenges-we-faced)
- Small Dataset: With only 12 historical rows, complex machine learning models like Autoencoders were prone to overfitting. We switched to a statistical Z-score method combined with a rule-based check for better accuracy.
  IndexOutOfBoundsException: Encountered an issue when adding new columns (is_anomaly, anomaly_reason) to the output rows. Fixed by correctly appending values to the list instead of using an invalid index.
  Anomaly Detection Sensitivity: Initially, the system marked all rows as non-anomalous. Adjusted the Z-score threshold to 1 (from 1.5) and added a rule-based check (balance difference > 5000) to align with the expected anomalies (yellow highlights in the image).
  UI Integration: The UI dashboard is currently a wireframe. Integrating it with the Java backend would require a REST API and a frontend framework, which we plan to implement in the future.
- [How to Run](#how-to-run)
- Compile and run the Java code
- [Tech Stack](#tech-stack)
- Backend:
  - Dl4J: Deeplearning for anomaly detection.
    Java: Core language for data processing.
    OpenCSV: Library for reading and writing CSV files.
    JavaMail API: For sending email notifications.
    Proposed Frontend (Wireframe):
- Frontend:
  - HTML/CSS/JavaScript: For the UI dashboard
  - React framework: For building an interactive dashboard.
- Tools:
  Maven: Dependency management.
  SMTP Server: For email notifications (e.g., Gmail SMTP).
- [Team](#team)
- Santoshi Sunanda Gaddam
- Venkat Jillella

---

## 🎯 Introduction
A brief overview of your project and its purpose. Mention which problem statement are your attempting to solve. Keep it concise and engaging.

## 🎥 Demo
🔗 [Live Demo](#) (if applicable)  
📹 [Video Demo](#) (if applicable)  
🖼️ Screenshots:

![Screenshot 1](link-to-image)

## 💡 Inspiration
What inspired you to create this project? Describe the problem you're solving.

## ⚙️ What It Does
Explain the key features and functionalities of your project.

## 🛠️ How We Built It
Briefly outline the technologies, frameworks, and tools used in development.

## 🚧 Challenges We Faced
Describe the major technical or non-technical challenges your team encountered.

## 🏃 How to Run
1. Clone the repository  
   ```sh
   git clone https://github.com/your-repo.git
   ```
2. Install dependencies  
   ```sh
   npm install  # or pip install -r requirements.txt (for Python)
   ```
3. Run the project  
   ```sh
   npm start  # or python app.py
   ```

## 🏗️ Tech Stack
- 🔹 Frontend: React / Vue / Angular
- 🔹 Backend: Node.js / FastAPI / Django
- 🔹 Database: PostgreSQL / Firebase
- 🔹 Other: OpenAI API / Twilio / Stripe

## 👥 Team
- **Your Name** - [GitHub](#) | [LinkedIn](#)
- **Teammate 2** - [GitHub](#) | [LinkedIn](#)
