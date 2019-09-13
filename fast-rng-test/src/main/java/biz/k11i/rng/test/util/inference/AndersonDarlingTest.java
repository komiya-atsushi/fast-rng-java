package biz.k11i.rng.test.util.inference;

import static org.apache.commons.math3.util.FastMath.*;

/**
 * Implements <a href="https://en.wikipedia.org/wiki/Anderson%E2%80%93Darling_test">Anderson–Darling test</a>
 * for uniformity.
 * <p>
 * Marsaglia, George, and John Marsaglia.
 * <i>"Evaluating the anderson-darling distribution."</i>
 * Journal of Statistical Software 9.2 (2004): 1-5.
 * </p>
 */
public class AndersonDarlingTest {
    private static final double EPSILON = Math.ulp(1.0) / 2.0;

    /**
     * Tests uniformity of double values.
     *
     * @param x array of double values that must be sorted in ascending order.
     * @return p-value
     */
    public static double andersonDarlingTest(double[] x) {
        final int n = x.length;
        double z = 0;
        double prev = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < n; i++) {
            if (prev > x[i]) {
                throw new IllegalArgumentException("Array of double values 'x' must be sorted in ascending order");
            }

            double u = x[i], v = x[n - 1 - i];
            if (u <= EPSILON) {
                u = EPSILON;
            } else if (v >= 1.0 - EPSILON) {
                v = 1.0 - EPSILON;
            }

            double t = u * (1.0 - v);
            z = z - (i + i + 1) * log(t);

            prev = x[i];
        }

        return 1 - Math.max(AD(n, -n + z / n), 0.0);
    }

    private static double AD(int n, double z) {
        double c, v, x;
        x = adinf(z);
        /* now x=adinf(z). Next, get v=errfix(n,x) and return x+v; */
        if (x > .8) {
            v = (-130.2137 + (745.2337 - (1705.091 - (1950.646 - (1116.360 - 255.7844 * x) * x) * x) * x) * x) / n;
            return x + v;
        }
        c = .01265 + .1757 / n;
        if (x < c) {
            v = x / c;
            v = sqrt(v) * (1. - v) * (49 * v - 102);
            return x + v * (.0037 / (n * n) + .00078 / n + .00006) / n;
        }
        v = (x - c) / (.8 - c);
        v = -.00022633 + (6.54034 - (14.6538 - (14.458 - (8.259 - 1.91864 * v) * v) * v) * v) * v;
        return x + v * (.04213 + .01365 / n) / n;
    }

    private static double adinf(double z) {
        if (z < 2.)
            return exp(-1.2337141 / z) / sqrt(z) * (2.00012 + (.247105 - (.0649821 - (.0347962 - (.011672 - .00168691 * z) * z) * z) * z) * z);
        /* max |error| < .000002 for z<2, (p=.90816...) */
        return exp(-exp(1.0776 - (2.30695 - (.43424 - (.082433 - (.008056 - .0003146 * z) * z) * z) * z) * z));
        /* max |error|<.0000008 for 4<z<infinity */
    }
}
