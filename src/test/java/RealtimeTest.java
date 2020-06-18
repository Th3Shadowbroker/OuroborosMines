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

import dev.th3shadowbroker.ouroboros.mines.util.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

public class RealtimeTest {

    @Test
    public void test() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR, 10);
        calendar.set(Calendar.MINUTE, 0);
        Date d1 = calendar.getTime();

        calendar.set(Calendar.HOUR, 11);
        calendar.set(Calendar.MINUTE, 0);
        Date d2 = calendar.getTime();

        Duration d = new Duration(d1, d2);

        System.out.println("Time until start: " + d.getTicksUntilStart());
        System.out.println("Time until end: " + d.getTicksUntilStart());
        System.out.println("Time in between: " + d.getTicksBetween());

        calendar.add(Calendar.SECOND, 1319980 / 20);
        System.out.println(calendar.getTime());

        Assertions.assertEquals(d.getTicksBetween(), 3600 * 20);
    }

}
