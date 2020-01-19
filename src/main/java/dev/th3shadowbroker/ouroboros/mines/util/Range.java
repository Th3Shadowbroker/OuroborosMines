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

import java.util.Random;
import java.util.function.Consumer;

public class Range {

    private final int min;

    private final int max;

    private final Random rnd;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
        this.rnd = new Random();
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean isRange() {
        return min != max;
    }

    public boolean isZero() {
        return min == 0 && max == 0;
    }

    public void ifNotZero(Consumer<Range> consumer) {
        if (!isZero()) {
            consumer.accept(this);
        }
    }

    public int getRandomWithin() {
        return min + rnd.nextInt(max - min);
    }

    @Override
    public String toString() {
        return String.format("%s-%s", min, max);
    }

    public static Range zero() {
        return new Range(0, 0);
    }

    public static Range fromString(String s) {
        String[] splitted = s.split("-");
        if (splitted.length == 0) throw new RuntimeException("Unable to parse range: " + s);
        int min = Integer.parseInt(splitted[0]);
        int max = splitted.length > 1 ? Integer.parseInt(splitted[1]) : min;
        return new Range(min, max);
    }

}
