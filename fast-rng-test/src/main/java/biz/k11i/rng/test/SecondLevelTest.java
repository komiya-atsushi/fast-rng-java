package biz.k11i.rng.test;

import biz.k11i.rng.test.gof.GoodnessOfFitTest;
import biz.k11i.rng.test.util.inference.AndersonDarlingTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class SecondLevelTest {
    public static class Builder {
        private int numIterations;

        public Builder numIterations(int numIterations) {
            this.numIterations = numIterations;
            return this;
        }

        public SecondLevelTest build() {
            return new SecondLevelTest(numIterations);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SecondLevelTest.class);

    private final int numIterations;

    private SecondLevelTest(int numIterations) {
        this.numIterations = numIterations;
    }

    public void testAndVerify(GoodnessOfFitTest gofTest) {
        boolean parallel = Runtime.getRuntime().availableProcessors() > 1;

        Map<String, Double> results = test(gofTest, parallel);
        assertThat(results).isNotEmpty();

        results.forEach((t, p) ->
                assertThat(p)
                        .describedAs("At a significance level of %e, the second level Goodness-of-Fit test of [%s] should fail to reject null hypothesis.\n" +
                                "This means that the random sequence generated by the random number generator [%s] fit the theoretical probability distribution.",
                                gofTest.significanceLevel, t, gofTest.rngName)
                        .isGreaterThanOrEqualTo(0.001));
    }

    private Map<String, Double> test(GoodnessOfFitTest gofTest, boolean parallel) {
        ForkJoinPool pool = parallel ? new ForkJoinPool() : null;

        try {
            Map<String, Double> result = IntStream.range(0, numIterations)
                    .peek(i -> LOGGER.info("First level #{}", i + 1))
                    .mapToObj(ignore -> parallel ? gofTest.testInParallel(pool) : gofTest.test())
                    .flatMap(r -> r.entrySet().stream())
                    .collect(groupingBy(
                            Map.Entry::getKey,
                            LinkedHashMap::new,
                            mapping(Map.Entry::getValue, computeAndersonDarlingP())));

            result.forEach((key, value) -> LOGGER.info("[{}] Second level p-value: {}", key, value));

            return result;

        } finally {
            if (pool != null) {
                pool.shutdown();
                try {
                    pool.awaitTermination(2, TimeUnit.SECONDS);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    private static Collector<Double, ?, double[]> toDoubleArray() {
        return collectingAndThen(toList(), l -> l.stream().mapToDouble(v -> v).sorted().toArray());
    }

    private static Collector<Double, ?, Double> computeAndersonDarlingP() {
        return collectingAndThen(
                toDoubleArray(),
                AndersonDarlingTest::andersonDarlingTest);
    }

    public static Builder builder() {
        return new Builder();
    }
}
