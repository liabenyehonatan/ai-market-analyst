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

        String apiKey = getKey("TWELVE_DATA_API_KEY");
        String geminiApiKey = getKey("GEMINI_API_KEY");
        String[] symbols = {"SOL/USD", "TSLA", "SPY", "AAPL", "NVDA"};
        JSONObject allData = new JSONObject();

        JSONObject oldData = DataStore.loadOldData();
        boolean shouldRun = DataStore.shouldRunToday(oldData, symbols);

        if (shouldRun) {
            try (HttpClient client = HttpClient.newHttpClient()) {

                for (String symbol : symbols) {
                    try {
                        JSONObject data = TwelveDataClient.fetchQuote(client, symbol, apiKey);

                        String price = data.getString("close");
                        String change = data.getString("percent_change");
                        boolean isMarketOpen = data.getBoolean("is_market_open");
                        String dataTime = data.getString("datetime");

                        if (oldData != null && oldData.has(symbol)) {
                            JSONObject oldStockData = oldData.getJSONObject(symbol);
                            String predictedTrend = oldStockData.getString("trend");

                            if (!oldStockData.getString("datetime").equals(dataTime)) {
                                double changeNumber = Double.parseDouble(change);
                                boolean wasCorrect = switch (predictedTrend) {
                                    case "Bullish" -> changeNumber > 0;
                                    case "Bearish" -> changeNumber < 0;
                                    default -> Math.abs(changeNumber) <= 0.5;
                                };

                                JSONObject logEntry = new JSONObject();
                                logEntry.put("date", dataTime);
                                logEntry.put("symbol", symbol);
                                logEntry.put("predicted_trend", predictedTrend);
                                logEntry.put("actual_percent_change", change);
                                logEntry.put("was_correct", wasCorrect);

                                DataStore.appendToLog(logEntry);
                            }
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
                    } catch (Exception e) {
                        System.err.println("Skipping " + symbol + ": " + e.getMessage());
                    }
                }

                if (allData.isEmpty()) {
                    throw new RuntimeException("All symbols failed, nothing to analyze");
                }

                JSONArray analysisArray = GeminiClient.fetchAnalysis(client, geminiApiKey, allData);

                for (int i = 0; i < analysisArray.length(); i++) {
                    JSONObject oneAnalysis = analysisArray.getJSONObject(i);
                    String analyzedSymbol = oneAnalysis.getString("symbol");
                    String trend = oneAnalysis.getString("trend");
                    String analysis = oneAnalysis.getString("analysis");
                    if (!allData.has(analyzedSymbol)) continue;
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
    }
    private static String getKey(String name) throws Exception {
        String fromEnv = System.getenv(name);
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;

        Properties env = new Properties();
        try (FileInputStream in = new FileInputStream(".env")) {
            env.load(in);
        }
        String value = env.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing key: " + name);
        }
        return value;
    }

}