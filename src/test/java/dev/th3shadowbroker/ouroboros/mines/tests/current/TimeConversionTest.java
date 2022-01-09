/*
 * Copyright 2021 Jens Fischer
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

package dev.th3shadowbroker.ouroboros.mines.tests.current;import dev.th3shadowbroker.ouroboros.mines.util.TimeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeConversionTest {

    @Test
    public void test() {
        long t1 = 0;
        long t2 = 12000;
        long w = 23500;

        long diffT1W = TimeUtils.getDifference(t1, w);
        long diffT2W = TimeUtils.getDifference(t2, w);

        System.out.println(String.format("Diff between %s and %s is %s", t1, w, diffT1W));
        System.out.println(String.format("Diff between %s and %s is %s", t2, w, diffT2W));

        Assertions.assertEquals(500, diffT1W);
        Assertions.assertEquals(12500, diffT2W);

        System.out.println("Time conversion works accordingly!");
    }

}
