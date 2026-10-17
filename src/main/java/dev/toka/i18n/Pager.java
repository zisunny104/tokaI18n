package dev.toka.i18n;

import java.util.List;

/**
 * Cuts a long list of lines into pages, for help texts and lists. It only counts lines; the footer text comes from the
 * language file of the plugin, such as a key that has ${0} for the page, ${1} for the number of pages and ${2} for the command.
 */
public final class Pager {
    /**
     * @param lines the lines of this page
     * @param page  the page shown, from 1
     * @param pages the number of pages, at least 1
     */
    public record Page<T>(List<T> lines, int page, int pages) {
    }

    private static final java.util.regex.Pattern NUMBER = java.util.regex.Pattern.compile("[0-9]{1,6}");

    private Pager() {
    }

    /**
     * @param requested the page asked for; a number that is too small or too large is moved to the nearest page
     */
    public static <T> Page<T> of(List<T> all, int perPage, int requested) {
        int size = Math.max(1, perPage);
        int pages = Math.max(1, (all.size() + size - 1) / size);
        int page = Math.min(Math.max(1, requested), pages);
        int from = Math.min(all.size(), (page - 1) * size);
        return new Page<>(List.copyOf(all.subList(from, Math.min(all.size(), from + size))), page, pages);
    }

    /**
     * @return the number in the text, or the fallback when it is empty or not a number
     */
    public static int parse(String text, int fallback) {
        if (text == null || !NUMBER.matcher(text.strip()).matches()) {
            return fallback;
        }
        return Integer.parseInt(text.strip());
    }
}
