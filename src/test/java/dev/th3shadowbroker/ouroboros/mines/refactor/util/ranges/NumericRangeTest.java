/*
 * Copyright 2022 Jens Fischer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.th3shadowbroker.ouroboros.mines.refactor.util.ranges;

import dev.th3shadowbroker.ouroboros.mines.refactor.exceptions.RangeFormatException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumericRangeTest {

    static NumericRange intRange;

    static NumericRange doubleRange;

    @BeforeAll
    static void setup() {
        intRange = new NumericRange(1, 2);
        doubleRange = new NumericRange(1, 1.5);
    }

    @Test
    void getMinDouble() {
        assertEquals(1, intRange.getMinDouble());
        assertEquals(1, doubleRange.getMinDouble());
    }

    @Test
    void getMaxDouble() {
        assertEquals(2, intRange.getMaxDouble());
        assertEquals(1.5, doubleRange.getMaxDouble());
    }

    @Test
    void getMinInt() {
        assertEquals(1, intRange.getMinInt());
        assertEquals(1, doubleRange.getMinInt());
    }

    @Test
    void getMaxInt() {
        assertEquals(2, intRange.getMaxInt());
        assertEquals(1, doubleRange.getMaxInt());
    }

    @Test
    void getRandomDouble() {
        double randomInt = intRange.getRandomDouble();
        assertTrue(randomInt >= intRange.getMinDouble() && randomInt <= intRange.getMaxDouble());

        double randomDouble = doubleRange.getRandomDouble();
        assertTrue(randomDouble >= doubleRange.getMinDouble() && randomDouble <= doubleRange.getMaxDouble());
    }

    @RepeatedTest(10)
    void getRandomInt() {
        int randomInt = intRange.getRandomInt();
        assertTrue(randomInt >= intRange.getMinInt() && randomInt <= intRange.getMaxInt());

        int randomDouble = doubleRange.getRandomInt();
        assertTrue(randomDouble >= doubleRange.getMinInt() && randomDouble <= doubleRange.getMaxInt());
    }

    @RepeatedTest(10)
    void isEvenInt() {
        assertFalse(intRange.isEvenInt());
        assertTrue(doubleRange.isEvenInt());
    }

    @Test
    void isEvenDouble() {
        assertFalse(intRange.isEvenDouble());
        assertFalse(doubleRange.isEvenDouble());
    }

    @Test
    void fromString() {
        assertThrows(RangeFormatException.class, () -> NumericRange.fromString("a-b"));
        assertDoesNotThrow(() -> NumericRange.fromString("1-2"));
        assertDoesNotThrow(() -> NumericRange.fromString("1-1.5"));
    }

}