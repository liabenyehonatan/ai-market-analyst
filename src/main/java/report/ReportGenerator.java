package report;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import org.json.JSONObject;

public class ReportGenerator {

    public static void generate(JSONObject allData) throws Exception {
        String tableRows = "";

        for (String symbol : allData.keySet()) {
            JSONObject stockData = allData.getJSONObject(symbol);
            String price = stockData.getString("price");
            String percentChange = stockData.getString("percent_change");
            String trend = stockData.getString("trend");
            String analysis = stockData.getString("analysis");

            double changeValue = Double.parseDouble(percentChange);
            String changeColor = changeValue >= 0 ? "green" : "red";

            tableRows += "<tr><td>" + symbol + "</td><td>" + price + "</td>"
                    + "<td style=\"color:" + changeColor + "; font-weight:bold;\">" + percentChange + "%</td>"
                    + "<td>" + trend + "</td><td>" + analysis + "</td></tr>\n";
        }

        int totalPredictions = 0;
        int correctPredictions = 0;
        String incorrectRows = "";

        File logFile = new File("data/accuracy_log.jsonl");
        if (logFile.exists()) {
            List<String> lines = Files.readAllLines(logFile.toPath());

            for (String line : lines) {
                JSONObject entry = new JSONObject(line);
                totalPredictions++;
                if (entry.getBoolean("was_correct")) {
                    correctPredictions++;
                } else {
                    incorrectRows += "<tr><td>" + entry.getString("symbol") + "</td><td>" + entry.getString("date")
                            + "</td><td>" + entry.getString("predicted_trend") + "</td><td>"
                            + entry.getString("actual_percent_change") + "%</td></tr>\n";
                }
            }
        }

        double accuracyPercent = 0;
        if (totalPredictions > 0) {
            accuracyPercent = (double) correctPredictions / totalPredictions * 100;
        }

        String html = "<html><head><title>AI Market Analyst Report</title>"
                + "<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; margin: 40px; color: #222; }"
                + "h1 { color: #1a1a2e; }"
                + "table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }"
                + "td, th { border: 1px solid #ccc; padding: 8px; text-align: right; }"
                + "th { background-color: #f0f0f0; }"
                + ".disclaimer { background: #fff8e1; padding: 10px; border-radius: 4px; display: inline-block; }"
                + "</style>"
                + "</head><body>"
                + "<h1>AI Market Analyst - Daily Report</h1>"
                + "<p class=\"disclaimer\"><b>Disclaimer:</b> This tool is for educational/analytical purposes only, not investment advice.</p>"
                + "<h2>Today's Data</h2>"
                + "<table><tr><th>Symbol</th><th>Price</th><th>Change %</th><th>Trend</th><th>Analysis</th></tr>"
                + tableRows
                + "</table>"
                + "<h2>Prediction Accuracy: " + String.format("%.1f", accuracyPercent) + "% (" + correctPredictions + "/" + totalPredictions + ")</h2>"
                + "<div style=\"max-width: 400px;\">"
                + "<canvas id=\"accuracyChart\"></canvas>"
                + "<h2>Incorrect Predictions</h2>"
                + (incorrectRows.isEmpty()
                ? "<p>No incorrect predictions yet.</p>"
                : "<table><tr><th>Symbol</th><th>Date</th><th>Predicted</th><th>Actual Change</th></tr>" + incorrectRows + "</table>")
                + "</div>"
                + "<script>"
                + "new Chart(document.getElementById('accuracyChart'), {"
                + "type: 'doughnut',"
                + "data: { labels: ['Correct', 'Incorrect'], datasets: [{ data: [" + correctPredictions + ", " + (totalPredictions - correctPredictions) + "], backgroundColor: ['#4CAF50', '#f44336'] }] }"
                + "});"
                + "</script>"
                + "</body></html>";

        FileWriter reportFile = new FileWriter("report.html");
        reportFile.write(html);
        reportFile.close();
    }
}