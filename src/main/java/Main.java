import java.io.FileInputStream;
import java.util.Properties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import java.io.FileWriter;

public class Main {
    public static void main(String[] args) throws Exception {

        Properties env = new Properties();
        env.load(new FileInputStream(".env"));
        String apiKey = env.getProperty("TWELVE_DATA_API_KEY");
        String[] symbols= {"SOL/USD", "TSLA", "SPY", "AAPL", "NVDA"};
        JSONObject allData = new JSONObject();

        for (String symbol : symbols){
        String url= "https://api.twelvedata.com/quote?symbol=" + symbol +"&apikey=" + apiKey;
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("The answer is" + response.body());

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
        }
        FileWriter file = new FileWriter("daily_data.json");
        file.write(allData.toString());
        file.close();

    }
}