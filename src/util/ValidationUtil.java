package util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)\\d{9,10}$");
    private static final Pattern CCCD = Pattern.compile("^\\d{9}|\\d{12}$");

    public static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isPositiveNumber(String s) {
        try {
            double v = Double.parseDouble(s);
            return v > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPhone(String s) {
        if (s == null) return false;
        return PHONE.matcher(s.trim()).matches();
    }

    public static boolean isCCCD(String s) {
        if (s == null) return false;
        return CCCD.matcher(s.trim()).matches();
    }
}