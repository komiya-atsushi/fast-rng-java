package biz.k11i.rng.util;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ParallelSorts {
    public static ForkJoinTask<Void> quickSort(double[] array) {
        return quickSortRecursive(array, 0, array.length);
    }

    private static ForkJoinTask<Void> quickSortRecursive(final double[] array, final int left, final int right) {
        return new RecursiveAction() {
            @Override
            protected void compute() {
                int length = right - left;
                if (length < 8) {
                    insertSort();
                    return;
                }

                double pivot = selectMedianAndSwap(length);
                double t;

                int leftHead = left, leftEq = left;
                int rightHead = right - 1, rightEq = right - 1;

                while (true) {
                    while (leftHead <= rightHead && array[leftHead] <= pivot) {
                        if (array[leftHead] == pivot) {
                            t = array[leftEq];
                            array[leftEq] = array[leftHead];
                            array[leftHead] = t;
                            leftEq++;
                        }
                        leftHead++;
                    }
                    while (rightHead > leftHead && array[rightHead] >= pivot) {
                        if (array[rightHead] == pivot) {
                            t = array[rightHead];
                            array[rightHead] = array[rightEq];
                            array[rightEq] = t;
                            rightEq--;
                        }
                        rightHead--;
                    }

                    if (leftHead >= rightHead) {
                        break;
                    }

                    t = array[leftHead];
                    array[leftHead] = array[rightHead];
                    array[rightHead] = t;
                }

                int leftEnd = flipHeadTailRoughly(array, left, leftEq, leftHead);
                int rightStart = flipHeadTailRoughly(array, leftHead, rightEq + 1, right);

                invokeAll(
                        quickSortRecursive(array, left, leftEnd),
                        quickSortRecursive(array, rightStart, right));
            }

            private void insertSort() {
                outer:
                for (int i = left + 1; i < right; i++) {
                    double val = array[i];

                    for (int j = i - 1; j >= left; j--) {
                        if (val < array[j]) {
                            array[j + 1] = array[j];
                        } else {
                            array[j + 1] = val;
                            continue outer;
                        }
                    }

                    array[left] = val;
                }
            }

            private double selectMedianAndSwap(int length) {
                int mid = left + length / 2;
                double leftVal = array[left], midVal = array[mid], rightVal = array[right - 1];

                if (midVal < leftVal) {
                    // mid < left

                    if (leftVal < rightVal) {
                        // mid < left < right
                        return leftVal;

                    } else if (midVal < rightVal) {
                        // mid < right < left
                        return rightVal;

                    } else {
                        // right <= mid < left
                        return midVal;
                    }

                } else {
                    // left <= mid

                    if (rightVal < leftVal) {
                        // right < left <= mid
                        return leftVal;

                    } else if (rightVal < midVal) {
                        // left < right < mid
                        return rightVal;

                    } else {
                        // left <= mid <= right
                        return midVal;
                    }
                }
            }

            private int flipHeadTailRoughly(double[] array, int head, int mid, int tail) {
                int headLength = mid - head;
                int tailLength = tail - mid;
                int minLength = Math.min(headLength, tailLength);
                int headIndex = head;
                int rightIndex = tail - 1;

                for (int i = 0; i < minLength; i++) {
                    double t = array[headIndex];
                    array[headIndex] = array[rightIndex];
                    array[rightIndex] = t;
                    headIndex++;
                    rightIndex--;
                }

                return head + tailLength;
            }
        };
    }

    public static void merge(double[] src, double[] dest, int start, int mid, int end) {
        int left = start;
        int right = mid;

        final int leftEnd = mid;
        final int rightEnd = end;

        for (int i = start; i < end; i++) {
            if (right >= rightEnd || (left < leftEnd && src[left] <= src[right])) {
                dest[i] = src[left];
                left++;
            } else {
                dest[i] = src[right];
                right++;
            }
        }
    }
}
