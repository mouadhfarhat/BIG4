package Services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MealDbService {

    private final HttpClient client = HttpClient.newHttpClient();

    public String fetchImageUrl(String dishName) {
        try {
            String encoded = URLEncoder.encode(dishName, StandardCharsets.UTF_8);
            String url = "https://www.themealdb.com/api/json/v1/1/search.php?s=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            // Extract first image URL
            Pattern pattern = Pattern.compile("\"strMealThumb\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(response.body());

            if (matcher.find()) {
                return matcher.group(1).replace("\\/", "/");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
