import java.io.FileInputStream;
import java.util.Properties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import java.io.FileWriter;
import org.json.JSONArray;

public class Main {
    public static void main(String[] args) throws Exception {

        Properties env = new Properties();
        env.load(new FileInputStream(".env"));
        String apiKey = env.getProperty("TWELVE_DATA_API_KEY");
        String geminiApiKey = env.getProperty("GEMINI_API_KEY");

        String[] symbols= {"SOL/USD", "TSLA", "SPY", "AAPL", "NVDA"};
        JSONObject allData = new JSONObject();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String symbol : symbols){
                String url= "https://api.twelvedata.com/quote?symbol=" + symbol +"&apikey=" + apiKey;

                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject data = new JSONObject(response.body());

                String price = data.getString("close");
                String change = data.getString("percent_change");
                boolean isMarketOpen = data.getBoolean("is_market_open");
                String dataTime = data.getString("datetime");

                JSONObject stockData = new JSONObject();
                stockData.put("price", price);
                stockData.put("percent_change", change);
                stockData.put("is_market_open", isMarketOpen);
                stockData.put("datetime", dataTime);

                allData.put(symbol, stockData);

                System.out.println("---");
                System.out.println("stock: " + symbol);
                System.out.println("Price: " + price);
                System.out.println("Percent Change: " + change + "%");
                System.out.println("Is market open? " + isMarketOpen);
                System.out.println("Date of the data: " + dataTime);

                }
            String geminiUrl= "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
            String prompt = "You are an expert financial analyst. I will provide you with a JSON containing stock market data. Analyze all available fields. For each stock, determine the current market trend. Return your response STRICTLY as a valid JSON array of objects, with no additional text or Markdown blocks. Each object must follow this structure: {\"symbol\": \"...\", \"trend\": \"Bullish/Bearish/Neutral\", \"analysis\": \"2-3 sentence explanation\"}. Here is the data: " + allData.toString();

            JSONObject part = new JSONObject();
            part.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);

            HttpRequest geminiRequest = HttpRequest.newBuilder()
                    .uri(URI.create(geminiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> geminiResponse = client.send(geminiRequest, HttpResponse.BodyHandlers.ofString());

            JSONObject geminiData = new JSONObject(geminiResponse.body());
            JSONArray candidates = geminiData.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject candidateContent = firstCandidate.getJSONObject("content");
            JSONArray candidateParts = candidateContent.getJSONArray("parts");
            JSONObject firstPart = candidateParts.getJSONObject(0);
            String analysisText = firstPart.getString("text");
            analysisText = analysisText.replace("```json", "").replace("```", "").trim();

            System.out.println("Gemini analysis:");
            System.out.println(analysisText);

            JSONArray analysisArray = new JSONArray(analysisText);

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

        FileWriter file = new FileWriter("daily_data.json");
        file.write(allData.toString());
        file.close();




    }
}