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

package dev.th3shadowbroker.ouroboros.mines.next.util;

import dev.th3shadowbroker.ouroboros.mines.next.exceptions.IntRangeParsingException;
import lombok.Getter;

import java.util.Random;

public final class IntRange {

    @Getter
    private final int min;

    @Getter
    private final int max;

    private final Random random;

    public IntRange(int a, int b) {
        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
        this.random = new Random();
    }

    public int getRandomWithin() {
        return min + random.nextInt(max - min);
    }

    public boolean isActualRange() {
        return min != max;
    }

    public boolean isZero() {
        return min == 0 && max == 0;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", min, max);
    }

    public static IntRange parse(String string) throws IntRangeParsingException {
        String[] fragments = string.split("-");
        try {
            if (fragments.length > 0) {
                int a = Integer.parseInt(fragments[0]);
                int b = Integer.parseInt(fragments[fragments.length > 1 ? 1 : 0]);
                return new IntRange(a, b);
            } else {
                throw new IntRangeParsingException(string);
            }
        } catch (NumberFormatException ex) {
            throw new IntRangeParsingException(string, ex);
        }
    }

    public static IntRange zero() {
        return new IntRange(0, 0);
    }

}
