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

import dev.th3shadowbroker.ouroboros.mines.OuroborosMines;

import java.time.LocalTime;
import java.time.ZoneId;

public class InstantUtilities {

    public static OMDuration durationFromString(String rawDuration) {
        String[] splitted = rawDuration.split("-");

        String[] minString = new String[2];
        String[] maxString = new String[2];

        String[] minSplitted = splitted[0].split(":");
        String[] maxSplitted = splitted[1].split(":");

        minString[0] = minSplitted[0];
        minString[1] = minSplitted.length > 1 ? minSplitted[1] : "00";

        maxString[0] = maxSplitted[0];
        maxString[1] = maxSplitted.length > 1 ? maxSplitted[1] : "00";

        LocalTime start = LocalTime.of(Integer.parseInt(minString[0]), Integer.parseInt(minString[1]), 0);
        LocalTime end = LocalTime.of(Integer.parseInt(maxString[0]), Integer.parseInt(maxString[1]), 0);

        return new OMDuration(start, end);
    }

    public static ZoneId getTimeZone() {
        String timezone = OuroborosMines.INSTANCE != null ? OuroborosMines.INSTANCE.getConfig().getString("timezone", "auto") : "auto";
        boolean useSystemTimezone = timezone.equalsIgnoreCase("auto");
        return useSystemTimezone ? ZoneId.systemDefault() : ZoneId.of(timezone);
    }

}
