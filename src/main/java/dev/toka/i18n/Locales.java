package dev.toka.i18n;

import java.util.Locale;

/**
 * Names of the languages. The files and the config always use the form zh-TW; zh_TW and ZH_TW are accepted and changed to it.
 */
public final class Locales {
    public static final String ZH_TW = "zh-TW";
    public static final String EN_US = "en-US";

    private Locales() {
    }

    /**
     * @return the language in the form language-REGION, or the fallback when the text is empty or not a language name
     */
    public static String normalize(String name, String fallback) {
        if (name == null) {
            return fallback;
        }
        String[] parts = name.strip().replace('_', '-').split("-");
        if (parts.length != 2 || !parts[0].matches("[A-Za-z]{2,3}") || !parts[1].matches("[A-Za-z]{2}")) {
            return fallback;
        }
        return parts[0].toLowerCase(Locale.ROOT) + "-" + parts[1].toUpperCase(Locale.ROOT);
    }
}
