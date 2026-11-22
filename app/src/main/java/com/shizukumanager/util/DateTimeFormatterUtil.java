package com.shizukumanager.app.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

// Tien ich dinh dang ngay gio theo ngon ngu hien tai cua ung dung
public final class DateTimeFormatterUtil {
    private DateTimeFormatterUtil() {
    }

    public static String formatDateTime(long millis) {
        if (millis <= 0L) {
            return "-";
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        return df.format(new Date(millis));
    }

    public static String formatShortTime(long millis) {
        if (millis <= 0L) {
            return "-";
        }
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        return df.format(new Date(millis));
    }
}
