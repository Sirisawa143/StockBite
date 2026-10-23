package stockbite.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Format {
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    private Format() {}

    public static String money(BigDecimal v) { return String.format("%,.2f", v); }
    public static String peso(BigDecimal v) { return "\u20B1" + money(v); }
    public static String date(LocalDateTime d) { return d == null ? "" : d.format(DATE_TIME); }

    /** Parses user input such as "1,250.50"; throws NumberFormatException if invalid. */
    public static BigDecimal parseMoney(String s) {
        return new BigDecimal(s.trim().replace(",", ""));
    }
}
