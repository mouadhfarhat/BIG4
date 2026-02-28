package Services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PopularityService {

    private final HttpClient client = HttpClient.newHttpClient();

    // returns total views of the last 30 days for a Wikipedia page title
    public long getMonthlyViews(String pageTitle) {
        try {
            // Example: https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/all-agents/Pizza/daily/20250101/20250131
            // We'll use "latest 30 days" in a simple way by requesting a recent fixed range isn't ideal.
            // For school project: use a static range OR you can pass dates dynamically (I can do that if you want).

            String title = URLEncoder.encode(pageTitle, StandardCharsets.UTF_8);
            String url = "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/all-agents/"
                    + title + "/daily/20250101/20250131";

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "Big4-JavaFX-App")
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();

            // sum all "views":123 values
            Pattern p = Pattern.compile("\"views\"\\s*:\\s*(\\d+)");
            Matcher m = p.matcher(body);

            long sum = 0;
            while (m.find()) sum += Long.parseLong(m.group(1));
            return sum;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // map views -> rating 1..5
    public double viewsToRating(long views) {
        if (views <= 1000) return 2.5;
        if (views <= 5000) return 3.0;
        if (views <= 20000) return 3.5;
        if (views <= 80000) return 4.0;
        if (views <= 200000) return 4.5;
        return 5.0;
    }

    public boolean isPopular(long views) {
        return views >= 80000;
    }
}
