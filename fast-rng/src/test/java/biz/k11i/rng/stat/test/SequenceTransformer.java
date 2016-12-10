package biz.k11i.rng.stat.test;

import biz.k11i.rng.util.ParallelSorts;

import java.util.Arrays;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

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

            double[] s = initializeS(values);
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

        @Override
        public ForkJoinTask<double[]> asForkJoinTask(final double[] values) {
            return new RecursiveTask<double[]>() {
                @Override
                protected double[] compute() {
                    int n = values.length;

                    double[] s = initializeS(values);
                    invokeAll(ParallelSorts.quickSort(s));

                    computeS(s, n);

                    double[] v = computeV(s, n);
                    invokeAll(ParallelSorts.quickSort(v));

                    return v;
                }
            };
        }

        private double[] initializeS(double[] values) {
            double[] s = new double[values.length + 1];

            // Initialize S
            double prevValue = 0.0;
            for (int i = 0; i < values.length; i++) {
                int si = i;
                int ui = i - 1;
                s[si] = values[ui + 1] - prevValue;
                prevValue = values[ui + 1];
            }
            s[s.length - 1] = 1.0 - values[values.length - 1];

            return s;
        }

        private void computeS(double[] s, int n) {
            for (int i = s.length - 1; i > 0; i--) {
                s[i] = (n - i + 1) * (s[i] - s[i - 1]);
            }
            s[0] = (n + 1) * s[0];
        }

        private double[] computeV(double[] s, int n) {
            double[] v = new double[n];
            double t = 0;

            for (int i = 0; i < n; i++) {
                v[i] = t + s[i];
                t = v[i];
            }

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
            result[n - 1] = pow(values[n - 1], n);

            Arrays.sort(result);

            return result;
        }

        @Override
        public ForkJoinTask<double[]> asForkJoinTask(final double[] values) {
            return new RecursiveTask<double[]>() {
                @Override
                protected double[] compute() {
                    final double[] result = new double[values.length];
                    invokeAll(computeRecursive(values, result, 0, values.length));
                    invokeAll(ParallelSorts.quickSort(result));
                    return result;
                }
            };
        }

        private ForkJoinTask<double[]> computeRecursive(final double[] src, final double[] dest, final int left, final int right) {
            return new RecursiveTask<double[]>() {
                @Override
                protected double[] compute() {
                    int size = right - left;

                    if (size > 10000) {
                        int mid = left + size / 2;

                        invokeAll(
                                computeRecursive(src, dest, left, mid),
                                computeRecursive(src, dest, mid, right));

                        return dest;

                    } else {
                        int n = src.length;

                        int end = Math.min(right, n - 1);
                        for (int i = left; i < end; i++) {
                            if (src[i + 1] == 0) {
                                dest[i] = 1.0;
                            } else {
                                dest[i] = pow(src[i] / src[i + 1], i + 1);
                            }
                        }
                        if (right == n) {
                            dest[n - 1] = pow(src[n - 1], n);
                        }

                        return dest;
                    }
                }
            };
        }
    };

    public abstract double[] transform(double[] values);

    public ForkJoinTask<double[]> asForkJoinTask(final double[] values) {
        return new ForkJoinTask<double[]>() {
            @Override
            public double[] getRawResult() {
                return transform(values);
            }

            @Override
            protected void setRawResult(double[] value) {
                // do nothing
            }

            @Override
            protected boolean exec() {
                return true;
            }
        };
    }
}
