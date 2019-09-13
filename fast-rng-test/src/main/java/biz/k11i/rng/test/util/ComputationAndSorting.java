package biz.k11i.rng.test.util;

import java.util.Arrays;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ComputationAndSorting<S extends ComputationAndSorting.Splittable<S>> {
    @FunctionalInterface
    public interface Computation<S extends Splittable<S>> {
        double compute(S splittable, int index);
    }

    @FunctionalInterface
    public interface Splittable<S extends Splittable<S>> {
        S split();
    }

    public static class NullSplittable implements Splittable<NullSplittable> {
        public static final NullSplittable INSTANCE = new NullSplittable();

        @Override
        public NullSplittable split() {
            return this;
        }
    }

    private final Computation<S> computation;
    private final int splitThreshold;
    private final int parallelMergeThreshold;

    public ComputationAndSorting(int n, int numParallels, Computation<S> computation) {
        this.computation = computation;
        int g = n / numParallels;
        this.splitThreshold = g;
        this.parallelMergeThreshold = g;
    }

    public ForkJoinTask<Void> newForkJoinTask(S splittable, double[] result, double[] work) {
        return new ComputationAction(0, result.length, result, work, splittable);
    }

    class ComputationAction extends RecursiveAction {
        private final int startInclusive;
        private final int endExclusive;
        private final double[] output;
        private final double[] work;
        private final S splittable;

        ComputationAction(int startInclusive, int endExclusive, double[] output, double[] work, S splittable) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
            this.output = output;
            this.work = work;
            this.splittable = splittable;
        }

        @Override
        protected void compute() {
            split(startInclusive, endExclusive, output, work);
        }

        private void split(int startInclusive, int endExclusive, double[] output, double[] work) {
            int n = endExclusive - startInclusive;
            if (n > splitThreshold) {
                int mid = startInclusive + (n >>> 1);

                ForkJoinTask<Void> h2 = new ComputationAction(mid, endExclusive, work, output, splittable.split()).fork();
                split(startInclusive, mid, work, output);
                h2.join();

                if (n > parallelMergeThreshold) {
                    mergeInParallel(startInclusive, mid, endExclusive, output, work);
                } else {
                    mergeSequentially(startInclusive, mid, endExclusive, output, work);
                }

            } else {
                computeSequentially(startInclusive, endExclusive, output);
            }
        }

        private void mergeInParallel(int startInclusive, int mid, int endExclusive, double[] output, double[] work) {
            int q1 = (startInclusive + mid) >>> 1;
            double split = work[q1];

            int left = mid;
            int right = endExclusive;

            while (left < right) {
                int searchMid = (left + right) >>> 1;
                if (work[searchMid] < split) {
                    left = searchMid + 1;
                } else {
                    right = searchMid;
                }
            }

            int s = (q1 + 1) + (right - mid);
            ForkJoinTask<Void> m2 = new MergeAction(work,
                    q1 + 1, mid,
                    right, endExclusive,
                    output, s).fork();
            MergeAction.mergeSequentially(work,
                    startInclusive, q1 + 1,
                    mid, right,
                    output, startInclusive);
            m2.join();
        }

        private void mergeSequentially(int startInclusive, int mid, int endExclusive, double[] output, double[] work) {
            MergeAction.mergeSequentially(work, startInclusive, mid, mid, endExclusive, output, startInclusive);
        }

        private void computeSequentially(int startInclusive, int endExclusive, double[] result) {
            for (int i = startInclusive; i < endExclusive; i++) {
                result[i] = computation.compute(splittable, i);
            }

            Arrays.sort(result, startInclusive, endExclusive);
        }
    }

    static class MergeAction extends RecursiveAction {
        private final double[] src;
        private final int src1Start;
        private final int src1End;
        private final int src2Start;
        private final int src2End;
        private final double[] dst;
        private final int dstStart;

        MergeAction(double[] src, int src1Start, int src1End, int src2Start, int src2End, double[] dst, int dstStart) {
            this.src = src;
            this.src1Start = src1Start;
            this.src1End = src1End;
            this.src2Start = src2Start;
            this.src2End = src2End;
            this.dst = dst;
            this.dstStart = dstStart;
        }

        @Override
        protected void compute() {
            mergeSequentially(src, src1Start, src1End, src2Start, src2End, dst, dstStart);
        }

        static void mergeSequentially(double[] src, int left, int leftEnd, int right, int rightEnd, double[] dst, int dstStart) {
            for (int i = dstStart; ; i++) {
                if (left >= leftEnd) {
                    System.arraycopy(src, right, dst, i, rightEnd - right);
                    return;
                }
                if (right >= rightEnd) {
                    System.arraycopy(src, left, dst, i, leftEnd - left);
                    return;
                }

                if (src[left] <= src[right]) {
                    dst[i] = src[left++];
                } else {
                    dst[i] = src[right++];
                }
            }
        }
    }
}
