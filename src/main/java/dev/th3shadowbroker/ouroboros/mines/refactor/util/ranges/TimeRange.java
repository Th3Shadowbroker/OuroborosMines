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
import lombok.Getter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeRange {

    private final LocalTime a;

    private final LocalTime b;

    @Getter
    private final ZoneId zone;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("H:mm");


    public TimeRange(LocalTime a, LocalTime b) {
        this(a, b, ZoneId.systemDefault());
    }

    public TimeRange(LocalTime a, LocalTime b, ZoneId zone) {
        this.a = a;
        this.b = b;
        this.zone = zone;
    }


    public LocalTime getFrom() {
        return a.isBefore(b) ? a : b;
    }

    public LocalTime getTo() {
        return a.isBefore(b) ? b : a;
    }


    public LocalDateTime getFromToday() {
        LocalDate today = LocalDate.now(zone);
        return getFrom().atDate(today);
    }

    public LocalDateTime getToToday() {
        LocalDate today = LocalDate.now(zone);
        return getTo().atDate(today);
    }


    public Duration getUntilFrom() {
        return getUntilFrom(LocalDateTime.now(zone));
    }

    public Duration getUntilFrom(LocalDateTime pointInTime) {
        LocalDateTime from = getFromToday();

        if (pointInTime.isAfter(from)) {
            from = from.plusDays(1);
        }

        return Duration.between(pointInTime, from);
    }


    public long getTicksUntilFrom() {
        return getTicksUntilFrom(LocalDateTime.now(zone));
    }

    public long getTicksUntilFrom(LocalDateTime pointInTime) {
        return getUntilFrom(pointInTime).toSeconds() * 20;
    }


    public Duration getUntilTo() {
        return getUntilTo(LocalDateTime.now(zone));
    }

    public Duration getUntilTo(LocalDateTime pointInTime) {
        LocalDateTime to = getToToday();

        if (pointInTime.isAfter(to)) {
            to = to.plusDays(1);
        }

        return Duration.between(pointInTime, to);
    }


    public long getTicksUntilTo() {
        return getTicksUntilTo(LocalDateTime.now(zone));
    }

    public long getTicksUntilTo(LocalDateTime pointInTime) {
        return getUntilTo(pointInTime).toSeconds() * 20;
    }


    public boolean isEven() {
        return a.equals(b);
    }


    public static TimeRange fromString(String string) {
        return fromString(string, "-");
    }

    public static TimeRange fromString(String string, String separator) {
        String[] splitted = string.split(separator);

        if (splitted.length == 0) {
            throw new RangeFormatException(TimeRange.class, string, null);
        }

        try {
            LocalTime a = LocalTime.parse(splitted[0], TimeRange.FORMATTER);
            LocalTime b = LocalTime.parse(splitted[splitted.length > 1 ? 1 : 0], TimeRange.FORMATTER);
            return new TimeRange(a, b);
        } catch (DateTimeParseException ex) {
            throw new RangeFormatException(TimeRange.class, string, ex);
        }
    }

}
