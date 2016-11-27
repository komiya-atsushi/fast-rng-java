package biz.k11i.rng.stat.test;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.util.FastMath;
import umontreal.iro.lecuyer.probdist.AndersonDarlingDist;
import umontreal.iro.lecuyer.probdist.CramerVonMisesDist;

public enum TestStatistic {
    KOLMOGOROV_SMIRNOV() {
        private final KolmogorovSmirnovTest KS_DIST = new KolmogorovSmirnovTest();
        private final UniformRealDistribution UNIFORM_DIST = new UniformRealDistribution();

        @Override
        public double test(double[] values) {
            try {
                double result = KS_DIST.kolmogorovSmirnovTest(UNIFORM_DIST, values);
                if (Double.isNaN(result)) {
                    return 0.0;
                }
                return result;

            } catch (TooManyIterationsException e) {
                return 0.0;
            }
        }
    },

    ANDERSON_DARLING() {
        @Override
        public double test(double[] values) {
            int n = values.length;

            double z = 0;
            for (int i = 0; i < n; i++) {
                double t = values[i] * (1 - values[n - 1 - i]);
                z = z - (i + i + 1) * FastMath.log(t);
            }

            double a2 = -n + z / n;

            return new AndersonDarlingDist(n).barF(a2);
        }
    },

    CRAMER_VON_MISES() {
        @Override
        public double test(double[] values) {
            int n = values.length;

            double z = 0;
            for (int i = 0; i < n; i++) {
                double t = values[i] - (i + 0.5) / n;
                z += t * t;
            }

            double w2 = 1.0 / (12.0 * n) + z;

            return new CramerVonMisesDist(n).barF(w2);
        }
    };

    public abstract double test(double[] values);
}
