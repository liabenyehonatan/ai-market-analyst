package clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiClient {

    public static JSONArray fetchAnalysis(HttpClient client, String apiKey, JSONObject allData) throws Exception {
        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String prompt = "You are an expert financial analyst. I will provide you with a JSON containing stock market data. Analyze all available fields. For each stock, determine the current market trend. Base your prediction on the expected trend through the next trading day's close. Return your response STRICTLY as a valid JSON array of objects, with no additional text or Markdown blocks. Each object must follow this structure: {\"symbol\": \"...\", \"trend\": \"Bullish/Bearish/Neutral\", \"analysis\": \"2-3 sentence explanation\"}. Here is the data: " + allData.toString();

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

        return new JSONArray(analysisText);
    }
}