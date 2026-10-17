package dev.toka.i18n;

import java.util.Locale;

/**
 * Names of the languages. The files and the config always use the form zh-TW; zh_TW and ZH_TW are accepted and changed to it.
 */
public final class Locales {
    public static final String ZH_TW = "zh-TW";
    public static final String EN_US = "en-US";

    private static final java.util.regex.Pattern LANGUAGE = java.util.regex.Pattern.compile("[A-Za-z]{2,3}");
    private static final java.util.regex.Pattern REGION = java.util.regex.Pattern.compile("[A-Za-z]{2}");

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
        if (parts.length != 2 || !LANGUAGE.matcher(parts[0]).matches() || !REGION.matcher(parts[1]).matches()) {
            return fallback;
        }
        return parts[0].toLowerCase(Locale.ROOT) + "-" + parts[1].toUpperCase(Locale.ROOT);
    }
}
