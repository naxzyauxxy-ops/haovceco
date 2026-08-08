package club.havocsmp.eco.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles color translation. Supports legacy '&' codes and hex '&#RRGGBB' codes,
 * matching the format used across your existing config files.
 */
public final class Text {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private Text() {}

    public static String color(String input) {
        if (input == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(ChatColor.of("#" + hex).toString()));
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<String> color(List<String> input) {
        List<String> out = new ArrayList<>();
        if (input == null) return out;
        for (String s : input) out.add(color(s));
        return out;
    }

    /** Replace a placeholder like %price% with a value. */
    public static String replace(String input, String key, Object value) {
        if (input == null) return "";
        return input.replace("%" + key + "%", String.valueOf(value));
    }
}
