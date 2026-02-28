package Services;

import java.net.URI;
import java.net.http.*;
import java.util.regex.*;

public class WeatherService {

    public static class Weather {
        public final double temperature;
        public final int weatherCode;
        public Weather(double t, int c) { temperature = t; weatherCode = c; }
    }

    private final HttpClient client = HttpClient.newHttpClient();

    // put your city coords here (example Tunis)
    public Weather getCurrent(double lat, double lon) {
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                    + "&longitude=" + lon
                    + "&current=temperature_2m,weather_code";

            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            String body = res.body();

            // extract temperature_2m and weather_code from JSON using regex (no gson)
            double temp = extractDouble(body, "\"temperature_2m\"\\s*:\\s*([0-9.\\-]+)");
            int code = (int) extractDouble(body, "\"weather_code\"\\s*:\\s*([0-9]+)");

            return new Weather(temp, code);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private double extractDouble(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.find()) return Double.parseDouble(m.group(1));
        return 0.0;
    }
}
