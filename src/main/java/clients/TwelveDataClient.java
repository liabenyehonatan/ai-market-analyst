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

        return new JSONObject(response.body());
    }
}