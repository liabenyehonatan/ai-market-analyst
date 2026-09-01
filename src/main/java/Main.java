import java.io.FileInputStream;
import java.util.Properties;
import java.net.http.HttpClient;
import org.json.JSONObject;
import org.json.JSONArray;
import clients.TwelveDataClient;
import clients.GeminiClient;
import storage.DataStore;

public class Main {
    public static void main(String[] args) throws Exception {

        Properties env = new Properties();
        env.load(new FileInputStream(".env"));
        String apiKey = env.getProperty("TWELVE_DATA_API_KEY");
        String geminiApiKey = env.getProperty("GEMINI_API_KEY");

        String[] symbols = {"SOL/USD", "TSLA", "SPY", "AAPL", "NVDA"};
        JSONObject allData = new JSONObject();

        JSONObject oldData = DataStore.loadOldData();
        boolean shouldRun = DataStore.shouldRunToday(oldData, symbols);

        if (shouldRun) {
            try (HttpClient client = HttpClient.newHttpClient()) {

                for (String symbol : symbols) {
                    JSONObject data = TwelveDataClient.fetchQuote(client, symbol, apiKey);

                    String price = data.getString("close");
                    String change = data.getString("percent_change");
                    boolean isMarketOpen = data.getBoolean("is_market_open");
                    String dataTime = data.getString("datetime");

                    if (oldData != null) {
                        JSONObject oldStockData = oldData.getJSONObject(symbol);
                        String predictedTrend = oldStockData.getString("trend");

                        double changeNumber = Double.parseDouble(change);
                        boolean wasCorrect;

                        if (predictedTrend.equals("Bullish") && changeNumber >= 0) {
                            wasCorrect = true;
                        } else if (predictedTrend.equals("Bearish") && changeNumber < 0) {
                            wasCorrect = true;
                        } else if (predictedTrend.equals("Neutral")) {
                            wasCorrect = true;
                        } else {
                            wasCorrect = false;
                        }

                        JSONObject logEntry = new JSONObject();
                        logEntry.put("date", dataTime);
                        logEntry.put("symbol", symbol);
                        logEntry.put("predicted_trend", predictedTrend);
                        logEntry.put("actual_percent_change", change);
                        logEntry.put("was_correct", wasCorrect);

                        DataStore.appendToLog(logEntry);
                    }

                    JSONObject stockData = new JSONObject();
                    stockData.put("price", price);
                    stockData.put("percent_change", change);
                    stockData.put("is_market_open", isMarketOpen);
                    stockData.put("datetime", dataTime);
                    stockData.put("checked_at", System.currentTimeMillis());

                    allData.put(symbol, stockData);

                    System.out.println("---");
                    System.out.println("stock: " + symbol);
                    System.out.println("Price: " + price);
                    System.out.println("Percent Change: " + change + "%");
                    System.out.println("Is market open? " + isMarketOpen);
                    System.out.println("Date of the data: " + dataTime);
                }

                JSONArray analysisArray = GeminiClient.fetchAnalysis(client, geminiApiKey, allData);

                for (int i = 0; i < analysisArray.length(); i++) {
                    JSONObject oneAnalysis = analysisArray.getJSONObject(i);
                    String analyzedSymbol = oneAnalysis.getString("symbol");
                    String trend = oneAnalysis.getString("trend");
                    String analysis = oneAnalysis.getString("analysis");

                    JSONObject existingStockData = allData.getJSONObject(analyzedSymbol);
                    existingStockData.put("trend", trend);
                    existingStockData.put("analysis", analysis);
                }
            }

            DataStore.saveDailyData(allData);
         } else {
        allData = oldData;
    }
        report.ReportGenerator.generate(allData);
    }}