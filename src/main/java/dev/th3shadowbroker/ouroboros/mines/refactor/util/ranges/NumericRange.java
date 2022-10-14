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

import java.util.Random;

public class NumericRange {

    private final double a;

    private final double b;

    private final Random random;

    public NumericRange(double a, double b) {
        this.a = a;
        this.b = b;
        this.random = new Random();
    }

    public double getMinDouble() {
        return Math.min(a, b);
    }

    public double getMaxDouble() {
        return Math.max(a, b);
    }

    public int getMinInt() {
        return (int) getMinDouble();
    }

    public int getMaxInt() {
        return (int) getMaxDouble();
    }

    public double getRandomDouble() {
        if (a == b) return a;
        return getMinDouble() + (getMaxDouble() - getMinDouble()) * random.nextDouble();
    }

    public int getRandomInt() {
        if (getMinInt() == getMaxInt()) return getMinInt();
        return getMinInt() + random.nextInt(getMaxInt());
    }

    public boolean isEvenDouble() {
        return a == b;
    }

    public boolean isEvenInt() {
        return getMinInt() == getMaxInt();
    }

    public static NumericRange fromString(String string) {
        return fromString(string, "-");
    }

    public static NumericRange fromString(String string, String separator) {
        String[] splitted = string.split(separator);
         if (splitted.length == 0) {
            throw new RangeFormatException(NumericRange.class, string, null);
        } else {
            try {
                double a = Double.parseDouble(splitted[0]);
                double b = Double.parseDouble(splitted[splitted.length > 1 ? 1 : 0]);
                return new NumericRange(a, b);
            } catch (NumberFormatException ex) {
                throw new RangeFormatException(NumericRange.class, string, ex);
            }
        }
    }

}
