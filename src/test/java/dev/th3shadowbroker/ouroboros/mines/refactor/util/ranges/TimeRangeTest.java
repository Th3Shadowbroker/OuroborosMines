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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TimeRangeTest {

    static TimeRange range;

    @BeforeAll
    static void setup() {
        LocalTime a = LocalTime.parse("7:00", TimeRange.FORMATTER);
        LocalTime b = LocalTime.parse("8:00", TimeRange.FORMATTER);

        range = new TimeRange(a, b);
    }

    @Test
    void getFrom() {
        assertEquals("7:00", range.getFrom().format(TimeRange.FORMATTER));
    }

    @Test
    void getTo() {
        assertEquals("8:00", range.getTo().format(TimeRange.FORMATTER));
    }

    @Test
    void getFromToday() {
        LocalDate mockDate = LocalDate.of(2022, 1, 1);
        LocalDateTime mockDateTime = LocalTime.of(7, 0).atDate(mockDate);

        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(() -> LocalDate.now(any(ZoneId.class))).thenReturn(mockDate);
            assertEquals(mockDateTime, range.getFromToday());
        }
    }

    @Test
    void getToToday() {
        LocalDate mockDate = LocalDate.of(2022, 1, 1);
        LocalDateTime mockDateTime = LocalTime.of(8, 0).atDate(mockDate);

        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(() -> LocalDate.now(any(ZoneId.class))).thenReturn(mockDate);
            assertEquals(mockDateTime, range.getToToday());
        }
    }

    @Test
    void getUntilFromSameDay() {
        LocalDate mockDate = LocalDate.of(2022, 1, 1);
        LocalDateTime mockPresent = LocalTime.of(6, 0, 0).atDate(mockDate);

        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(() -> LocalDate.now(any(ZoneId.class))).thenReturn(mockDate);

            Duration timeUntilFrom = range.getUntilFrom(mockPresent);
            assertEquals(1, timeUntilFrom.toHours());

            long ticks = range.getTicksUntilFrom(mockPresent);
            assertEquals(72000, ticks);
        }
    }

    @Test
    void getUntilFromNextDay() {
        LocalDate mockDate = LocalDate.of(2022, 1, 1);
        LocalDateTime mockPresent = LocalTime.of(8, 0, 0).atDate(mockDate);

        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(() -> LocalDate.now(any(ZoneId.class))).thenReturn(mockDate);
            Duration duration = range.getUntilFrom(mockPresent);
            assertEquals(23, duration.toHours());

            long ticks = range.getTicksUntilFrom(mockPresent);
            assertEquals(1656000, ticks);
        }
    }

    @Test
    void getUntilToSameDay() {
        LocalDate mockDate = LocalDate.of(2022, 1, 1);
        LocalDateTime mockPresent = LocalTime.of(6, 0, 0).atDate(mockDate);

        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(() -> LocalDate.now(any(ZoneId.class))).thenReturn(mockDate);

            Duration duration = range.getUntilTo(mockPresent);
            assertEquals(2, duration.toHours());

            long ticks = range.getTicksUntilTo(mockPresent);
            assertEquals(144000, ticks);
        }
    }

    @Test
    void getUntilToNextDay() {
        LocalDate mockDate = LocalDate.of(2022, 1, 1);
        LocalDateTime mockPresent = LocalTime.of(9, 0, 0).atDate(mockDate);

        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(() -> LocalDate.now(any(ZoneId.class))).thenReturn(mockDate);

            Duration duration = range.getUntilTo(mockPresent);
            assertEquals(23, duration.toHours());

            long ticks = range.getTicksUntilTo(mockPresent);
            assertEquals(1656000, ticks);
        }
    }

    @Test
    void fromString() {
        assertThrows(RangeFormatException.class, () -> TimeRange.fromString("a-b"));
        assertDoesNotThrow(() -> TimeRange.fromString("7:00-8:00"));
        assertDoesNotThrow(() -> TimeRange.fromString("20:00-21:00"));
    }

}