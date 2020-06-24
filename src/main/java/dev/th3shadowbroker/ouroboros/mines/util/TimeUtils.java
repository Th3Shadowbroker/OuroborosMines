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

import java.util.*;

public class TimeUtils {

    public static final long DAY_START = 0;

    public final static long DAY_END = 24000;

    public static long getDifference(long aimedTime, long worldTime) {
        //System.out.println(String.format("Inputs. Aimed: %s | World: %s", aimedTime, worldTime));
        long difference = aimedTime - worldTime;

        // Aimed time is at the same in-game day or time is now
        if (difference > 0 || difference == 0) {
            return difference;
        }

        // Aimed time is behind current in-game time
        else {
            long diffToNextCycle = Math.abs(DAY_END - worldTime);
            //System.out.println(String.format("Diffs. Start: %s | World: %s", diffToNextCycle, aimedTime));
            return diffToNextCycle + aimedTime;
        }
    }

    public static Calendar getCalendarWithTimezone() {
        Calendar calendar = (Calendar) Calendar.getInstance().clone();

        String timezone = OuroborosMines.INSTANCE.getConfig().getString("timezone", "auto");
        boolean useSystemTimezone = timezone.equalsIgnoreCase("auto");
        if (!useSystemTimezone) {
            calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        }

        return calendar;
    }

    public static Date now() {
        return getCalendarWithTimezone().getTime();
    }

    public static boolean minesAreOpen(Range openingHours, long time) {
        return time >= openingHours.getMin() && time <= openingHours.getMax();
    }

    public static boolean minesAreOpen(RegionConfiguration regionConfiguration) {
        if (regionConfiguration.getOpeningHours().isPresent() && regionConfiguration.getOpeningHours().get().isEnabled()) {
            List<Duration> realtimeRanges = regionConfiguration.getOpeningHours().get().getRealtimeRange();

            if (!realtimeRanges.isEmpty()) {
                for (Duration realtimeRange : realtimeRanges) {
                    if (realtimeRange.isBetween(TimeUtils.now())) {
                        return true;
                    }
                }
                return false;
            } else {
                return minesAreOpen(regionConfiguration.getOpeningHours().get().getTickRange(), regionConfiguration.getWorld().getTime());
            }
        }
        return true;
    }

}
