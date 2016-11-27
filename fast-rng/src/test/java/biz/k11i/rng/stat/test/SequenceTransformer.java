package biz.k11i.rng.stat.test;

import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;

import static biz.k11i.util.MathFunctions.pow;

public enum SequenceTransformer {
    NONE() {
        @Override
        public double[] transform(double[] values) {
            return values;
        }
    },

    SPACING() {
        @Override
        public double[] transform(double[] values) {
            int n = values.length;
            double[] s = new double[n + 1];

            // Initialize S
            double prevValue = 0.0;
            for (int i = 0; i < n; i++) {
                int si = i;
                int ui = i - 1;
                s[si] = values[ui + 1] - prevValue;
                prevValue = values[ui + 1];
            }
            s[s.length - 1] = 1.0 - values[n - 1];
            Arrays.sort(s);

            // Compute S
            for (int i = s.length - 1; i > 0; i--) {
                s[i] = (n - i + 1) * (s[i] - s[i - 1]);
            }
            s[0] = (n + 1) * s[0];

            // Compute V
            double[] v = new double[n];
            double t = 0;

            for (int i = 0; i < n; i++) {
                v[i] = t + s[i];
                t = v[i];
            }
            Arrays.sort(v);

            return v;
        }
    },

    POWER_RATIO() {
        @Override
        public double[] transform(double[] values) {
            double[] result = new double[values.length];
            int n = values.length;

            for (int i = 0; i < n - 1; i++) {
                if (values[i + 1] == 0) {
                    result[i] = 1.0;
                } else {
                    result[i] = pow(values[i] / values[i + 1], i + 1);
                }
            }
            result[n - 1] = FastMath.pow(values[n - 1], n);

            Arrays.sort(result);

            return result;
        }
    };

    public abstract double[] transform(double[] values);
}
