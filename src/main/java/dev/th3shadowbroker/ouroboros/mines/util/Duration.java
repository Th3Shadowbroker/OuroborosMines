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

import java.util.Calendar;
import java.util.Date;

public class Duration {

    private final Date start;

    private final Date end;

    private final Calendar calendar;

    public static final long DAY_SECONDS = 86400;

    public Duration(Date start, Date end) {
        this.start = start;
        this.end = end;
        this.calendar = TimeUtils.getCalendarWithTimezone();
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public long getTicksUntilStart() {
        Date now = TimeUtils.now();
        Date start = (Date) this.start.clone();

        if (start.before(now)) {
            calendar.setTime(start);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            start = calendar.getTime();
        }

        return ((start.getTime() - now.getTime()) / 1000) * 20;
    }

    public long getTicksUntilEnd() {
        Date now = TimeUtils.now();
        Date end = (Date) this.end.clone();

        if (end.before(now)) {
            calendar.setTime(end);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            end = calendar.getTime();
        }

        return ((end.getTime() - now.getTime()) / 1000) * 20;
    }

    public long getTicksBetween() {
        return ((end.getTime() - start.getTime()) / 1000) * 20;
    }

    public boolean isBetween(Date date) {
        return start.before(date) && end.after(date);
    }

    public static Duration fromString(String rangeString) {
        String[] splitted = rangeString.split("-");

        String[] minString = new String[2];
        String[] maxString = new String[2];

        String[] minSplitted = splitted[0].split(":");
        String[] maxSplitted = splitted[1].split(":");

        minString[0] = minSplitted[0];
        minString[1] = minSplitted.length > 1 ? minSplitted[1] : "00";

        maxString[0] = maxSplitted[0];
        maxString[1] = maxSplitted.length > 1 ? maxSplitted[1] : "00";

        Calendar calendar = TimeUtils.getCalendarWithTimezone();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(minString[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minString[1]));
        calendar.set(Calendar.SECOND, 0);

        Date start = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(maxString[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(maxString[1]));
        calendar.set(Calendar.SECOND, 0);

        Date end = calendar.getTime();

        return new Duration(start, end);
    }

}
