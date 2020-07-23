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

import dev.th3shadowbroker.ouroboros.mines.util.OMDuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

public class InstantTimeTest {

    /*@Test
    public void test1() {
        LocalTime startLt = LocalTime.of(10, 0);
        LocalTime endLt = LocalTime.of(22, 0);
        LocalDateTime compDt = LocalDateTime.of(LocalDate.of(2020, 7, 21), LocalTime.of(9, 0));

        OMDuration omd = new OMDuration(startLt, endLt);

        Assertions.assertEquals(72000, omd.getTicksUntilStart(ZonedDateTime.of(2020, 7, 21, 9, 0, 0, 0, ZoneId.of("+2"))));
        Assertions.assertEquals(936000, omd.getTicksUntilEnd(ZonedDateTime.of(2020, 7, 21, 9, 0, 0, 0, ZoneId.of("+2"))));
        Assertions.assertFalse(omd.isInBetween(compDt));
    }

    @Test
    public void test2() {
        LocalTime startLt = LocalTime.of(1, 0);
        LocalTime endLt = LocalTime.of(20, 0);
        LocalDateTime compDt = LocalDateTime.of(LocalDate.of(2020, 7, 21), LocalTime.of(9, 0));

        OMDuration omd = new OMDuration(startLt, endLt);

        //Assertions.assertEquals(0, omd.getTicksUntilStart(ZonedDateTime.of(2020, 7, 20, 9, 0, 0, 0, ZoneId.of("+2"))));
        Assertions.assertEquals(792000, omd.getTicksUntilEnd(ZonedDateTime.of(2020, 7, 21, 9, 0, 0, 0, ZoneId.of("+2"))));
        Assertions.assertTrue(omd.isInBetween(compDt));
    }*/

    @Test
    public void test3() {
        ZoneId zoneId = ZoneId.of("America/New_York");

        LocalTime zst = LocalTime.of(6,0);
        ZonedDateTime zsdt = ZonedDateTime.of(LocalDate.now(zoneId), zst, zoneId);

        LocalTime zet = LocalTime.of(7, 0);
        ZonedDateTime zedt = ZonedDateTime.of(LocalDate.now(zoneId), zet, zoneId);

        LocalTime tb = LocalTime.of(6, 30);
        ZonedDateTime tbdt = ZonedDateTime.of(LocalDate.now(zoneId), tb, zoneId);

        Assertions.assertTrue(zsdt.isBefore(tbdt));
        Assertions.assertTrue(zedt.isAfter(tbdt));
    }

}
