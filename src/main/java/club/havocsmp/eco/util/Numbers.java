package club.havocsmp.eco.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.NavigableMap;
import java.util.TreeMap;

/** Formats currency values: 1,234.56 as well as abbreviated 1.23K / 4.56M etc. */
public final class Numbers {

    private static final DecimalFormat COMMA = new DecimalFormat("#,##0.00");
    private static final NavigableMap<Long, String> SUFFIXES = new TreeMap<>();

    static {
        SUFFIXES.put(1_000L, "K");
        SUFFIXES.put(1_000_000L, "M");
        SUFFIXES.put(1_000_000_000L, "B");
        SUFFIXES.put(1_000_000_000_000L, "T");
        SUFFIXES.put(1_000_000_000_000_000L, "Q");
    }

    private Numbers() {}

    /** 1234.5 -> "1,234.50" */
    public static String comma(double value) {
        return COMMA.format(value);
    }

    /** 1234567 -> "1.23M" (the %voyager_nicestMoney% style). */
    public static String nicest(double value) {
        if (value < 1000) return trimZeros(value);
        long longVal = (long) value;
        var entry = SUFFIXES.floorEntry(longVal);
        if (entry == null) return trimZeros(value);
        long divideBy = entry.getKey();
        BigDecimal scaled = BigDecimal.valueOf(value / divideBy).setScale(2, RoundingMode.HALF_UP);
        return trimZeros(scaled.doubleValue()) + entry.getValue();
    }

    private static String trimZeros(double v) {
        if (v == Math.floor(v)) return String.valueOf((long) v);
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static boolean isPositiveNumber(String s) {
        try {
            return Double.parseDouble(s) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
