package clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class TwelveDataClient {

    public static JSONObject fetchQuote(HttpClient client, String symbol, String apiKey) throws Exception {
        String url = "https://api.twelvedata.com/quote?symbol=" + symbol + "&apikey=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject data = new JSONObject(response.body());

        if (!data.has("close")) {
            throw new RuntimeException("Twelve Data error for " + symbol + ": " + data.optString("message", "unknown error"));
        }

        return data;
    }
}