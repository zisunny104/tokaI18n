package dev.toka.i18n;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PagerTest {
    private static final List<String> LINES = List.of("a", "b", "c", "d", "e");

    @Test
    public void cutsIntoPagesAndKeepsThePageInRange() {
        assertEquals(List.of("a", "b"), Pager.of(LINES, 2, 1).lines());
        assertEquals(List.of("e"), Pager.of(LINES, 2, 3).lines());
        assertEquals(3, Pager.of(LINES, 2, 1).pages());
        assertEquals(3, Pager.of(LINES, 2, 99).page());
        assertEquals(1, Pager.of(LINES, 2, -4).page());
    }

    @Test
    public void anEmptyListIsOneEmptyPage() {
        Pager.Page page = Pager.of(List.of(), 5, 1);
        assertEquals(1, page.pages());
        assertEquals(List.of(), page.lines());
    }

    @Test
    public void parsesPageNumbersQuietly() {
        assertEquals(3, Pager.parse(" 3 ", 1));
        assertEquals(1, Pager.parse("abc", 1));
        assertEquals(1, Pager.parse(null, 1));
    }
}
