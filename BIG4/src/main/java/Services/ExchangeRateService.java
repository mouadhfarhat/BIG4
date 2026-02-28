package Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExchangeRateService {

    private final HttpClient client = HttpClient.newHttpClient();

    public double getRate(String base, String target) {
        try {
            String url = "https://open.er-api.com/v6/latest/" + base;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            // JSON contains: "rates":{"USD":0.32,"EUR":0.29,...}
            Pattern pattern = Pattern.compile("\"" + Pattern.quote(target) + "\"\\s*:\\s*([0-9.]+)");
            Matcher matcher = pattern.matcher(response.body());

            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1.0; // fallback
    }
}
