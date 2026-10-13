package report;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class ReportGenerator {

    private static final int MIN_PREDICTIONS_PER_ASSET = 30;

    public static void generate(JSONObject allData) throws Exception {
        String tableRows = "";

        for (String symbol : allData.keySet()) {
            JSONObject stockData = allData.getJSONObject(symbol);
            String price = String.format(java.util.Locale.US, "%.2f", Double.parseDouble(stockData.getString("price")));
            String percentChange = stockData.getString("percent_change");
            String trend = stockData.optString("trend", "n/a");
            String analysis = stockData.optString("analysis", "No analysis returned");

            double changeValue = Double.parseDouble(percentChange);
            String changeColor = changeValue >= 0 ? "green" : "red";

            tableRows += "<tr><td>" + symbol + "</td><td>" + price + "</td>"
                    + "<td style=\"color:" + changeColor + "; font-weight:bold;\">" + String.format(java.util.Locale.US, "%.2f", changeValue) + "%</td>"
                    + "<td>" + trend + "</td><td>" + analysis + "</td></tr>\n";
        }

        int totalPredictions = 0;
        int correctPredictions = 0;
        String incorrectRows = "";
        Map<String, Integer> predictionsPerSymbol = new HashMap<>();

        File logFile = new File("data/accuracy_log.jsonl");
        if (logFile.exists()) {
            List<String> lines = Files.readAllLines(logFile.toPath());

            for (String line : lines) {
                if (line.isBlank()) continue;
                JSONObject entry = new JSONObject(line);
                totalPredictions++;
                predictionsPerSymbol.merge(entry.getString("symbol"), 1, Integer::sum);

                if (entry.getBoolean("was_correct")) {
                    correctPredictions++;
                } else {
                    incorrectRows += "<tr><td>" + entry.getString("symbol") + "</td><td>" + entry.getString("date")
                            + "</td><td>" + entry.getString("predicted_trend") + "</td><td>"
                            + entry.getString("actual_percent_change") + "%</td></tr>\n";
                }
            }
        }

        boolean enoughData = true;
        String progress = "";
        for (String symbol : allData.keySet()) {
            int count = predictionsPerSymbol.getOrDefault(symbol, 0);
            if (count < MIN_PREDICTIONS_PER_ASSET) {
                enoughData = false;
            }
            progress += symbol + ": " + count + "/" + MIN_PREDICTIONS_PER_ASSET + "<br>";
        }

        String accuracySection;
        String chartScript = "";

        if (enoughData) {
            double accuracyPercent = (double) correctPredictions / totalPredictions * 100;
            accuracySection = "<h2>Prediction Accuracy: " + String.format("%.1f", accuracyPercent)
                    + "% (" + correctPredictions + "/" + totalPredictions + ")</h2>"
                    + "<div style=\"max-width: 400px;\"><canvas id=\"accuracyChart\"></canvas></div>";
            chartScript = "<script>"
                    + "new Chart(document.getElementById('accuracyChart'), {"
                    + "type: 'doughnut',"
                    + "data: { labels: ['Correct', 'Incorrect'], datasets: [{ data: [" + correctPredictions + ", "
                    + (totalPredictions - correctPredictions) + "], backgroundColor: ['#4CAF50', '#f44336'] }] }"
                    + "});"
                    + "</script>";
        } else {
            accuracySection = "<h2>Prediction Accuracy</h2>"
                    + "<p>Collecting data. Accuracy is shown once every asset has at least "
                    + MIN_PREDICTIONS_PER_ASSET + " scored predictions.</p>"
                    + "<p>" + progress + "</p>";
        }

        String html = "<html><head><title>AI Market Analyst Report</title>"
                + "<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; margin: 40px; color: #222; }"
                + "h1 { color: #1a1a2e; }"
                + "table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }"
                + "td, th { border: 1px solid #ccc; padding: 8px; text-align: left; }"
                + "th { background-color: #f0f0f0; }"
                + "</style>"
                + "</head><body>"
                + "<h1>AI Market Analyst - Daily Report</h1>"
                + "<h2>Today's Data</h2>"
                + "<table><tr><th>Symbol</th><th>Price</th><th>Change %</th><th>Trend</th><th>Analysis</th></tr>"
                + tableRows
                + "</table>"
                + accuracySection
                + "<h2>Incorrect Predictions</h2>"
                + (incorrectRows.isEmpty()
                ? "<p>No incorrect predictions yet.</p>"
                : "<table><tr><th>Symbol</th><th>Date</th><th>Predicted</th><th>Actual Change</th></tr>" + incorrectRows + "</table>")
                + chartScript
                + "</body></html>";

        FileWriter reportFile = new FileWriter("report.html");
        reportFile.write(html);
        reportFile.close();
    }
}