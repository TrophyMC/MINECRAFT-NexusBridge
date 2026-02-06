package de.mecrytv.nexusBridge.utils;

public class TimeUtils {

    public static String formatDuration(long millis) {
        if (millis <= 0) return "Permanent";

        long seconds = millis / 1000;
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");
        if (sb.length() == 0) sb.append("< 1m");

        return sb.toString().trim();
    }
}
