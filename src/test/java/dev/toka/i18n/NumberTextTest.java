package dev.toka.i18n;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberTextTest {
    @Test
    public void groupsDigitsByThree() {
        assertEquals("0", NumberText.withCommas(0));
        assertEquals("999", NumberText.withCommas(999));
        assertEquals("1,000", NumberText.withCommas(1000));
        assertEquals("12,345", NumberText.withCommas(12345));
        assertEquals("1,234,567", NumberText.withCommas(1234567));
    }

    @Test
    public void handlesNegativesAndLimits() {
        assertEquals("-12,345", NumberText.withCommas(-12345));
        assertEquals("-999", NumberText.withCommas(-999));
        assertEquals("9,223,372,036,854,775,807", NumberText.withCommas(Long.MAX_VALUE));
        assertEquals("-9,223,372,036,854,775,808", NumberText.withCommas(Long.MIN_VALUE));
    }
}
