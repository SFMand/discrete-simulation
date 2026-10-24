package com.sfmand.des.engine;

import java.util.Locale;

/** Number formatting shared by the trace and the report. */
public final class Numbers {

    private Numbers() {
    }

    public static String format(double value) {
        return format(value, 2);
    }

    public static String format(double value, int decimals) {
        if (Double.isFinite(value) && value == Math.rint(value) && Math.abs(value) < 1e15) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}
