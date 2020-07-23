/*
 * Copyright 2020 Jens Fischer
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

package dev.th3shadowbroker.ouroboros.mines.util;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class OMDuration {

    private final LocalTime start;

    private final LocalTime end;

    public OMDuration(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public long getTicksUntilStart() {
        ZoneId zoneId = InstantUtilities.getTimeZone();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime zStart = getZonedDateTimeStart(zoneId);

        if (zStart.isBefore(now)) {
            zStart = zStart.plusDays(1);
            //System.out.println("Adding day to start");
        }

        Duration duration = Duration.between(now, zStart);
        //long l = (duration.toMillis() / 1000) * 20;
        //System.out.println("Ticks (start " + zStart.toLocalTime().atOffset(zStart.getOffset()) + "): " + l);
        //return (duration.toMillis() / 1000) * 20;
        //long l = (now.until(zStart, ChronoUnit.MILLIS) / 1000) * 20;
        /*
        System.out.println("Start: " + l + " | " + (l / 20 / 60 / 60));
        System.out.println("Start-Time: " + zStart);
        System.out.println("Now: " + now);
        */
        return (now.until(zStart, ChronoUnit.MILLIS) / 1000) * 20;
    }
/*
    public long getTicksUntilStart(ZonedDateTime zdt) {
        ZoneId zoneId = InstantUtilities.getTimeZone();
        ZonedDateTime now = zdt;
        ZonedDateTime zStart = getZonedDateTimeStart(zoneId);

        Duration duration = Duration.between(zStart, now);
        return duration.isNegative() ? (duration.plusDays(1).toMillis() / 1000) * 20 : (duration.toMillis() / 1000) * 20;
    }
*/
    public long getTicksUntilEnd() {
        ZoneId zoneId = InstantUtilities.getTimeZone();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime zEnd = getZonedDateTimeEnd(zoneId);

        if (zEnd.isBefore(now)) {
            zEnd = zEnd.plusDays(1);
        }

        //long l = (now.until(zEnd, ChronoUnit.MILLIS) / 1000) * 20;
        /*System.out.println("End: " + l + " | " + (l / 20 / 60 / 60));
        System.out.println("End-Time: " + zEnd);
        System.out.println("Now: " + now);*/
        return (now.until(zEnd, ChronoUnit.MILLIS) / 1000) * 20;
    }
/*
    public long getTicksUntilEnd(ZonedDateTime zdt) {
        ZoneId zoneId = InstantUtilities.getTimeZone();
        ZonedDateTime now = zdt;
        ZonedDateTime zEnd = getZonedDateTimeEnd(zoneId);

        Duration duration = Duration.between(zEnd, now);
        return duration.isNegative() ? (duration.plusDays(1).toMillis() / 1000) * 20 : (duration.toMillis() / 1000) * 20;
    }
*/
    public boolean isInBetween(LocalDateTime ldt) {
        ZoneId zoneId = InstantUtilities.getTimeZone();

        //@TODO Zdt doesn't apply timezones properly!!!!
        /*
        [13:44:18 INFO]: zedt2020-07-22T08:00-04:00[America/New_York]zsdt2020-07-22T07:19-04:00[America/New_York]
        [13:44:18 INFO]: zdt 2020-07-22T13:44:18.632-04:00[America/New_York] <--- @FIXME
        [13:44:18 INFO]: zedt2020-07-22T08:00-04:00[America/New_York]
         */
        ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
        ZonedDateTime zsdt = getZonedDateTimeStart(zoneId);
        ZonedDateTime zedt = getZonedDateTimeEnd(zoneId);

        if (end.isBefore(start)) {
            zedt = zedt.plusDays(1);
            //System.out.println("Adding day to end");
        }

        /*
        System.out.println(String.format("C1: %s | C2: %s", b1, b2));
        System.out.println("zsdt" + zsdt);
        System.out.println("zdt " + zdt);
        System.out.println("zedt" + zedt);
        */

        return zsdt.isBefore(zdt) && zedt.isAfter(zdt);
        //@TODO Fix this shit!
    }

    public ZonedDateTime getZonedDateTimeStart(ZoneId zoneId) {
        return ZonedDateTime.of(LocalDate.now(zoneId), start, zoneId);
    }

    public ZonedDateTime getZonedDateTimeEnd(ZoneId zoneId) {
        return ZonedDateTime.of(LocalDate.now(zoneId), end, zoneId);
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public static OMDuration fromZonedDateTime(ZonedDateTime start, ZonedDateTime end) {
        return new OMDuration(start.toLocalTime(), end.toLocalTime());
    }

}
