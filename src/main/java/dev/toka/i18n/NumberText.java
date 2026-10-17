package dev.toka.i18n;

import java.util.Locale;

/** Number formatting that does not depend on the server locale. */
public final class NumberText {
    private NumberText() {
    }

    /**
     * @return the number with a comma between every three digits, such as 12,345 or -1,000
     */
    public static String withCommas(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
