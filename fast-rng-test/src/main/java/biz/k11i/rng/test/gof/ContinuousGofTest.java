package biz.k11i.rng.test.gof;

import biz.k11i.rng.test.util.ComputationAndSorting;
import biz.k11i.rng.test.util.SplittableRandomWrapper;
import biz.k11i.rng.test.util.distribution.ContinuousDistribution;
import biz.k11i.rng.test.util.distribution.ProbabilityDistributions;
import biz.k11i.rng.test.util.inference.AndersonDarlingTest;
import net.jafama.FastMath;
import org.apache.commons.math3.distribution.RealDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * Provides Goodness-of-Fit test for discrete random number generator.
 */
class ContinuousGofTest extends GoodnessOfFitTest {
    /**
     * Builds {@link ContinuousGofTest} object.
     */
    public static class Builder extends BuilderBase<Builder> {
        private ContinuousDistribution distribution;
        private String name;
        private RandomNumberGenerator generator;

        @SuppressWarnings("unused")
        public Builder probabilityDistribution(RealDistribution distribution) {
            this.distribution = ProbabilityDistributions.wrap(distribution);
            return this;
        }

        public Builder probabilityDistribution(ContinuousDistribution distribution) {
            this.distribution = Objects.requireNonNull(distribution);
            return this;
        }

        public Builder randomNumberGenerator(String name, RandomNumberGenerator generator) {
            this.name = name;
            this.generator = Objects.requireNonNull(generator);
            return this;
        }

        public GoodnessOfFitTest build() {
            return new ContinuousGofTest(
                    name,
                    significanceLevel,
                    distribution,
                    generator,
                    numRandomValues);
        }
    }

    /**
     * Generates continuous random variates.
     */
    @FunctionalInterface
    public interface RandomNumberGenerator {
        /**
         * Generates a continuous random variate.
         *
         * @param random uses to sample uniform random variates.
         * @return continuous random variate.
         */
        double generate(Random random);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ContinuousGofTest.class);

    private final ContinuousDistribution distribution;
    private final RandomNumberGenerator generator;
    private final int numRandomValues;
    private final double[] x;
    private final double[] work;
    private long seed;
    private final BufferRecycler bufferRecycler = new BufferRecycler();
    private ComputationAndSorting<SplittableRandomWrapper> computationAndSorting;

    private ContinuousGofTest(
            String name,
            double significanceLevel,
            ContinuousDistribution distribution,
            RandomNumberGenerator generator,
            int numRandomValues) {
        super(name, significanceLevel);
        this.distribution = distribution;
        this.generator = generator;
        this.numRandomValues = numRandomValues;
        this.x = new double[numRandomValues];
        this.work = new double[numRandomValues];
    }

    @Override
    public Map<String, Double> test() {
        return PerformanceMeasure.run(LOGGER, m -> {
            m.measure("Generate {} random numbers", numRandomValues, ignore -> {
                Random random = new SplittableRandomWrapper(seed++);
                for (int i = 0; i < numRandomValues; i++) {
                    double rv = generator.generate(random);
                    x[i] = distribution.cdf(rv);
                }
            });

            m.measure("Sort {} random numbers", x.length, ignore -> Arrays.sort(x));

            Map<String, Double> result = new LinkedHashMap<>();

            m.measure("Transform sorted random numbers", ignore -> {
                for (Transformation t : Transformation.values()) {
                    m.measure("Transform by [{}]: p-value = {}", r -> {
                        double[] transformed = t.transform(x, bufferRecycler);
                        double p = AndersonDarlingTest.andersonDarlingTest(transformed);
                        result.put(t.toString(), p);

                        r.updateArgs(t, p);
                    });
                }
            });

            return result;
        });
    }

    @Override
    public Map<String, Double> testInParallel(ForkJoinPool pool) {
        return PerformanceMeasure.run(LOGGER, m -> {
            if (computationAndSorting == null) {
                computationAndSorting = new ComputationAndSorting<>(
                        numRandomValues,
                        pool.getParallelism(),
                        (r, index) -> distribution.cdf(generator.generate(r)));
            }

            m.measure("Generate & sort {} random numbers", numRandomValues, ignore -> {
                SplittableRandomWrapper random = new SplittableRandomWrapper(seed++);
                pool.invoke(computationAndSorting.newForkJoinTask(random, x, work));
            });

            Map<String, Double> result = new LinkedHashMap<>();

            m.measure("Transform sorted random numbers", ignore -> {
                for (Transformation t : Transformation.values()) {
                    m.measure("Transform by [{}]: p-value = {}", r -> {
                        double[] transformed = t.transformInParallel(pool, x, bufferRecycler);
                        double p = AndersonDarlingTest.andersonDarlingTest(transformed);
                        result.put(t.toString(), p);

                        r.updateArgs(t, p);
                    });
                }
            });

            return result;
        });
    }
}

enum Transformation {
    RAW {
        @Override
        public double[] transform(double[] x, BufferRecycler ignore) {
            return x;
        }
    },

    SPACING {
        @Override
        public double[] transform(double[] x, BufferRecycler bufferRecycler) {
            int n = x.length;

            double[] s = bufferRecycler.allocate("SPACING_S", n + 1);
            initializeS(x, s);
            Arrays.sort(s);

            // Compute S
            for (int i = s.length - 1; i > 0; i--) {
                s[i] = (n - i + 1) * (s[i] - s[i - 1]);
            }
            s[0] = (n + 1) * s[0];

            // Compute V
            double[] v = bufferRecycler.allocate("SPACING_V", n);
            double t = 0;

            for (int i = 0; i < n; i++) {
                v[i] = t + s[i];
                t = v[i];
            }

            return v;
        }

        private void initializeS(double[] values, double[] s) {
            // Initialize S
            double prevValue = 0.0;
            for (int i = 0; i < values.length; i++) {
                int si = i;
                int ui = i - 1;
                s[si] = values[ui + 1] - prevValue;
                prevValue = values[ui + 1];
            }
            s[s.length - 1] = 1.0 - values[values.length - 1];
        }

        @Override
        public double[] transformInParallel(ForkJoinPool pool, double[] x, BufferRecycler bufferRecycler) {
            int n = x.length;

            ComputationAndSorting<ComputationAndSorting.NullSplittable> spacingInitS = new ComputationAndSorting<>(n, pool.getParallelism(), (ignore, index) -> {
                if (index == 0) {
                    return x[0];
                }
                if (index < n) {
                    return x[index] - x[index - 1];
                }
                return 1.0 - x[n - 1];
            });

            double[] s = bufferRecycler.allocate("SPACING_S", n + 1);
            double[] work = bufferRecycler.allocate("SPACING_S_WORK", n + 1);
            pool.invoke(spacingInitS.newForkJoinTask(ComputationAndSorting.NullSplittable.INSTANCE, s, work));

            // Compute S and V
            double[] v = bufferRecycler.allocate("SPACING_V", n);
            v[0] = (n + 1) * s[0];

            for (int i = 1; i < n; i++) {
                v[i] = v[i - 1] + (n - i + 1) * (s[i] - s[i - 1]);
            }

            return v;
        }
    },

    POWER_RATIO {
        @Override
        public double[] transform(double[] x, BufferRecycler bufferRecycler) {
            int n = x.length;
            double[] result = bufferRecycler.allocate("POWER_RATIO_RESULT", n);

            for (int i = 0; i < n - 1; i++) {
                if (x[i + 1] == 0) {
                    result[i] = 1.0;
                } else {
                    result[i] = FastMath.pow(x[i] / x[i + 1], i + 1);
                }
            }
            result[n - 1] = FastMath.pow(x[n - 1], n);

            Arrays.sort(result);

            return result;
        }

        @Override
        public double[] transformInParallel(ForkJoinPool pool, double[] x, BufferRecycler bufferRecycler) {
            int n = x.length;

            ComputationAndSorting<ComputationAndSorting.NullSplittable> powerRatio = new ComputationAndSorting<>(n, pool.getParallelism(),
                    (ignore, index) -> {
                        if (index < n - 1) {
                            if (x[index + 1] == 0) {
                                return 1.0;
                            }
                            return FastMath.pow(x[index] / x[index + 1], index + 1);
                        }
                        return FastMath.pow(x[n - 1], n);
                    });

            double[] result = bufferRecycler.allocate("POWER_RATIO_RESULT", n);
            double[] work = bufferRecycler.allocate("POWER_RATIO_WORK", n);
            pool.invoke(powerRatio.newForkJoinTask(ComputationAndSorting.NullSplittable.INSTANCE, result, work));

            return result;
        }
    };

    public abstract double[] transform(double[] x, BufferRecycler bufferRecycler);

    public double[] transformInParallel(ForkJoinPool pool, double[] x, BufferRecycler bufferRecycler) {
        return transform(x, bufferRecycler);
    }
}

class BufferRecycler {
    private Map<String, double[]> buffers = new HashMap<>(10);

    double[] allocate(String name, int n) {
        return buffers.computeIfAbsent(name, ignore -> new double[n]);
    }
}
