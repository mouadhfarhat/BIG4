package Services;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIChatService {

    private static final String API_KEY = "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";
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private final HttpClient client = HttpClient.newHttpClient();

    public String askAI(String systemPrompt, String userMessage) {
        try {
            String jsonBody = """
            {
              "model": "gpt-4o-mini",
              "messages": [
                {"role": "system", "content": "%s"},
                {"role": "user", "content": "%s"}
              ],
              "temperature": 0.7
            }
            """.formatted(escape(systemPrompt), escape(userMessage));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();

            // ✅ SUPER IMPORTANT DEBUG
            System.out.println("OPENAI STATUS = " + code);
            System.out.println("OPENAI BODY = " + body);

            // If API returned an error JSON (no "choices")
            if (code != 200) {
                String msg = extract(body, "\"message\"\\s*:\\s*\"(.*?)\"");
                return "API error " + code + (msg.isEmpty() ? "" : (": " + msg));
            }

            // Normal chat completions response: choices[0].message.content :contentReference[oaicite:1]{index=1}
            String content = extract(body, "\"content\"\\s*:\\s*\"(.*?)\"");
            if (content.isEmpty()) return "AI error: empty content";
            return unescape(content);

        } catch (Exception e) {
            e.printStackTrace();
            return "AI service error: " + e.getMessage();
        }
    }

    private String extract(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String unescape(String s) {
        return s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
