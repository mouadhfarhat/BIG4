package Services;

import Entities.Dish;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuChatBot {

    private final List<Dish> dishes;

    // conversation state
    private Double maxBudget = null;
    private boolean wantsVeg = false;
    private boolean wantsSpicy = false;
    private boolean wantsLight = false;

    public MenuChatBot(List<Dish> dishes) {
        this.dishes = dishes == null ? List.of() : dishes;
    }

    public String reply(String userMsg) {
        String msg = (userMsg == null ? "" : userMsg).toLowerCase().trim();

        extractBudget(msg);
        extractPrefs(msg);

        // Ask questions until we have enough info
        String next = nextQuestion();
        if (next != null) return next;

        List<Dish> top = top3();
        return format(top);
    }

    private void extractBudget(String msg) {
        Pattern p = Pattern.compile("(max|budget|under|<=|less than)\\s*(\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(msg);
        if (m.find()) {
            maxBudget = Double.parseDouble(m.group(2));
        }
    }

    private void extractPrefs(String msg) {
        // user intent
        if (hasAny(msg, "vegetarian", "veggie", "sans viande", "no meat")) wantsVeg = true;
        if (hasAny(msg, "spicy", "harissa", "piment", "chili")) wantsSpicy = true;
        if (hasAny(msg, "light", "healthy", "léger", "salad", "grilled")) wantsLight = true;
    }

    private String nextQuestion() {
        if (maxBudget == null) return "What’s your max budget? (example: max 25)";
        // optional extra Q only if user didn’t mention anything
        if (!wantsVeg && !wantsSpicy && !wantsLight)
            return "Any preference? (spicy / vegetarian / light) — or say 'no preference'.";
        return null;
    }

    private List<Dish> top3() {
        List<Dish> copy = new ArrayList<>(dishes);
        copy.sort((a, b) -> Integer.compare(score(b), score(a)));
        return copy.subList(0, Math.min(3, copy.size()));
    }

    private int score(Dish d) {
        int s = 0;

        // Budget scoring
        if (maxBudget != null) {
            double price = d.getBase_price();
            if (price <= maxBudget) s += 40;
            else s -= 100;
        }

        // Description-based tags
        String desc = (d.getDescription() == null ? "" : d.getDescription()).toLowerCase();

        boolean dishVeg = hasAny(desc, "vegetarian", "veggie", "sans viande", "no meat", "légumes");
        boolean dishSpicy = hasAny(desc, "spicy", "harissa", "piment", "chili", "hot");
        boolean dishLight = hasAny(desc, "salad", "grilled", "healthy", "light", "léger");

        if (wantsVeg)   s += dishVeg ? 30 : -30;
        if (wantsSpicy) s += dishSpicy ? 25 : -15;
        if (wantsLight) s += dishLight ? 20 : -10;

        // small bonus: has description (looks better)
        if (desc.length() >= 15) s += 5;

        return s;
    }

    private String format(List<Dish> top) {
        if (top.isEmpty()) return "Sorry, I can’t find available dishes right now.";

        StringBuilder sb = new StringBuilder();
        sb.append("Based on your chat, here are my top picks:\n\n");

        for (int i = 0; i < top.size(); i++) {
            Dish d = top.get(i);
            sb.append(i + 1).append(") ")
                    .append(d.getName())
                    .append(" — ").append(String.format("%.2f", (double) d.getBase_price())).append(" DT\n");

            String desc = d.getDescription();
            if (desc != null && !desc.isBlank()) {
                sb.append("   • ").append(shorten(desc, 80)).append("\n");
            }
        }

        sb.append("\nWant me to recommend a drink/dessert too?");
        return sb.toString();
    }

    private boolean hasAny(String text, String... words) {
        for (String w : words) if (text.contains(w)) return true;
        return false;
    }

    private String shorten(String s, int max) {
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
