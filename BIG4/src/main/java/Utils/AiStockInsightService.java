package Utils;

import Controllers.AdminDashboardController;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiStockInsightService {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String OPENROUTER_MODELS_URL = "https://openrouter.ai/api/v1/models";
    private static final long MODELS_CACHE_TTL_MS = 10 * 60 * 1000;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();
    private static volatile List<String> cachedFreeModels = List.of();
    private static volatile long cachedFreeModelsAt = 0L;
        private static final List<String> DEFAULT_OPENROUTER_MODELS = Arrays.asList(
                "stepfun/step-3.5-flash:free",
            "google/gemma-3-27b-it:free",
            "google/gemini-2.0-flash-exp:free",
            "meta-llama/llama-3.1-8b-instruct:free"
        );

    public static class AiResult {
        private final String provider;
        private final String summary;

        public AiResult(String provider, String summary) {
            this.provider = provider;
            this.summary = summary;
        }

        public String getProvider() {
            return provider;
        }

        public String getSummary() {
            return summary;
        }
    }

    public static class ApiHealth {
        private final boolean ok;
        private final String provider;
        private final String message;

        public ApiHealth(boolean ok, String provider, String message) {
            this.ok = ok;
            this.provider = provider;
            this.message = message;
        }

        public boolean isOk() {
            return ok;
        }

        public String getProvider() {
            return provider;
        }

        public String getMessage() {
            return message;
        }
    }

    public AiResult buildInsights(List<AdminDashboardController.IngredientInsight> insights,
                                  double predictedWaste7d,
                                  int potentialStockouts,
                                  double suggestedBudget,
                                  double weatherMultiplier) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return new AiResult("Local fallback", localSummary(insights, predictedWaste7d, potentialStockouts, suggestedBudget, weatherMultiplier));
        }

        String prompt = buildPrompt(insights, predictedWaste7d, potentialStockouts, suggestedBudget, weatherMultiplier);
        List<String> modelsToTry = buildModelsToTry(apiKey);

        String lastError = null;
        for (String model : modelsToTry) {
            try {
                String jsonBody = "{" +
                        "\"model\":\"" + model + "\"," +
                        "\"messages\":[{" +
                        "\"role\":\"system\",\"content\":\"You are a restaurant inventory optimization assistant. Give concise, practical actions.\"},{" +
                        "\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}]," +
                        "\"temperature\":0.3," +
                        "\"max_tokens\":500" +
                        "}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(OPENROUTER_URL))
                    .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("HTTP-Referer", "http://localhost")
                        .header("X-Title", "BIG4 Smart Insights")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    String content = extractContent(response.body());
                    if (content != null && !content.isBlank()) {
                        return new AiResult("OpenRouter (" + model + ")", content.trim());
                    }
                    lastError = "empty content";
                    continue;
                }

                String errorMessage = extractErrorMessage(response.body());
                if (errorMessage == null || errorMessage.isBlank()) {
                    lastError = "HTTP " + response.statusCode();
                } else {
                    lastError = "HTTP " + response.statusCode() + " - " + errorMessage;
                }
            } catch (IOException | InterruptedException e) {
                lastError = e.getMessage();
            }
        }

        String fallbackSummary = localSummary(insights, predictedWaste7d, potentialStockouts, suggestedBudget, weatherMultiplier);
        if (lastError != null && !lastError.isBlank()) {
            fallbackSummary = fallbackSummary + "\n\nAI API note: " + lastError;
        }
        return new AiResult("Local fallback", fallbackSummary);
    }

    public ApiHealth checkApiHealth() {
        AiResult result = askChat(
                "You are a system health checker.",
                "Reply with exactly: OPENROUTER_OK",
                30,
                0.0
        );

        if (result.getProvider().startsWith("OpenRouter")) {
            return new ApiHealth(true, result.getProvider(), result.getSummary());
        }
        return new ApiHealth(false, result.getProvider(), result.getSummary());
    }

    public AiResult askInventoryAssistant(String userMessage) {
        return askChat(
                "You are a restaurant inventory assistant. Keep answers short, practical, and friendly.",
                userMessage,
                350,
                0.3
        );
    }

    private AiResult askChat(String systemPrompt, String userMessage, int maxTokens, double temperature) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return new AiResult("Local fallback", "OPENROUTER_API_KEY is missing.");
        }

        List<String> modelsToTry = buildModelsToTry(apiKey);

        String lastError = null;
        for (String model : modelsToTry) {
            try {
                String jsonBody = "{" +
                        "\"model\":\"" + model + "\"," +
                        "\"messages\":[{" +
                        "\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},{" +
                        "\"role\":\"user\",\"content\":\"" + escapeJson(userMessage) + "\"}]," +
                        "\"temperature\":" + temperature + "," +
                        "\"max_tokens\":" + maxTokens +
                        "}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(OPENROUTER_URL))
                    .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("HTTP-Referer", "http://localhost")
                        .header("X-Title", "BIG4 Smart Insights")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    String content = extractContent(response.body());
                    if (content != null && !content.isBlank()) {
                        return new AiResult("OpenRouter (" + model + ")", content.trim());
                    }
                    lastError = "empty content";
                    continue;
                }

                String errorMessage = extractErrorMessage(response.body());
                if (errorMessage == null || errorMessage.isBlank()) {
                    lastError = "HTTP " + response.statusCode();
                } else {
                    lastError = "HTTP " + response.statusCode() + " - " + errorMessage;
                }
            } catch (IOException | InterruptedException e) {
                lastError = e.getMessage();
            }
        }

        return new AiResult("Local fallback", "API unavailable: " + (lastError == null ? "unknown error" : lastError));
    }

    private List<String> buildModelsToTry(String apiKey) {
        Set<String> ordered = new LinkedHashSet<>();

        String preferredModel = System.getenv("OPENROUTER_MODEL");
        if (preferredModel != null && !preferredModel.isBlank()) {
            ordered.add(preferredModel.trim());
        }

        ordered.addAll(fetchDynamicFreeModels(apiKey));
        ordered.addAll(DEFAULT_OPENROUTER_MODELS);
        return new ArrayList<>(ordered);
    }

    private List<String> fetchDynamicFreeModels(String apiKey) {
        long now = System.currentTimeMillis();
        if (!cachedFreeModels.isEmpty() && (now - cachedFreeModelsAt) < MODELS_CACHE_TTL_MS) {
            return cachedFreeModels;
        }

        List<String> ids = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENROUTER_MODELS_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return ids;
            }

            Pattern idPattern = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+?:free)\\\"");
            Matcher matcher = idPattern.matcher(response.body());
            while (matcher.find() && ids.size() < 12) {
                ids.add(matcher.group(1));
            }
            if (!ids.isEmpty()) {
                cachedFreeModels = List.copyOf(ids);
                cachedFreeModelsAt = now;
            }
        } catch (IOException | InterruptedException ignored) {
        }
        return ids;
    }

    private String buildPrompt(List<AdminDashboardController.IngredientInsight> insights,
                               double predictedWaste7d,
                               int potentialStockouts,
                               double suggestedBudget,
                               double weatherMultiplier) {
        StringBuilder builder = new StringBuilder();
        builder.append("Restaurant inventory dataset:\n");
        builder.append(String.format("- Predicted waste next 7d: %.2f\n", predictedWaste7d));
        builder.append(String.format("- Potential stockouts next 7d: %d\n", potentialStockouts));
        builder.append(String.format("- Suggested purchase budget: $%.2f\n", suggestedBudget));
        builder.append(String.format("- Weather demand multiplier: %.2f\n", weatherMultiplier));
        builder.append("Top ingredient lines (name,current,min,avgWaste,recommended,risk):\n");

        int max = Math.min(10, insights.size());
        for (int index = 0; index < max; index++) {
            AdminDashboardController.IngredientInsight insight = insights.get(index);
            builder.append(String.format("%s,%.2f,%.2f,%.2f,%.2f,%s\n",
                    insight.getIngredientName(),
                    insight.getCurrentStock(),
                    insight.getMinStock(),
                    insight.getAvgDailyWaste(),
                    insight.getRecommendedOrder(),
                    insight.getRiskLevel()));
        }

        builder.append("Respond with:\n");
        builder.append("1) 3 urgent actions\n");
        builder.append("2) 3 medium-term optimization actions\n");
        builder.append("3) one concise procurement strategy paragraph\n");
        return builder.toString();
    }

    private String localSummary(List<AdminDashboardController.IngredientInsight> insights,
                                double predictedWaste7d,
                                int potentialStockouts,
                                double suggestedBudget,
                                double weatherMultiplier) {
        StringBuilder summary = new StringBuilder();
        summary.append("Urgent actions:\n");
        int shown = 0;
        for (AdminDashboardController.IngredientInsight insight : insights) {
            if (shown >= 3) break;
            if ("HIGH".equals(insight.getRiskLevel()) || insight.getRecommendedOrder() > 0) {
                summary.append("- Order ")
                        .append(String.format("%.2f", insight.getRecommendedOrder()))
                        .append(" units of ")
                        .append(insight.getIngredientName())
                        .append(" (risk ")
                        .append(insight.getRiskLevel())
                        .append(").\n");
                shown++;
            }
        }

        summary.append("Medium-term optimization:\n");
        summary.append("- Align purchase cycles with 7-day predicted waste (")
                .append(String.format("%.2f", predictedWaste7d))
                .append(").\n");
        summary.append("- Reduce order batch size for fast-expiring items to cut spoilage.\n");
        summary.append("- Track top wasted ingredients weekly and cap overstock to 1.5x minimum stock.\n\n");

        summary.append("Procurement strategy: Prioritize high-risk ingredients first, keep a lean buffer for medium-risk items, and reserve a budget of ")
                .append(String.format("$%.2f", suggestedBudget))
                .append(" while adjusting demand by weather multiplier ")
                .append(String.format("%.2f", weatherMultiplier))
                .append(". Potential stockouts currently estimated: ")
                .append(potentialStockouts)
                .append(".");

        return summary.toString();
    }

    private String extractContent(String json) {
        Pattern pattern = Pattern.compile("\\\"content\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescapeJsonText(matcher.group(1));
        }

        Pattern textPattern = Pattern.compile("\\\"text\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"", Pattern.DOTALL);
        Matcher textMatcher = textPattern.matcher(json);
        if (textMatcher.find()) {
            return unescapeJsonText(textMatcher.group(1));
        }

        return null;
    }

    private String extractErrorMessage(String json) {
        Pattern pattern = Pattern.compile("\\\"message\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescapeJsonText(matcher.group(1));
        }
        return null;
    }

    private String unescapeJsonText(String value) {
        return value
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
