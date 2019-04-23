# Release notes

## 0.2.0

- Fast-rng now requires Java 8 or later.
- Introduces a new class [`UniformRNGUtils`](fast-rng/src/main/java/biz/k11i/rng/UniformRNGUtils.java).
    - The method `UniformRNGUtils#nextInt(java.util.Random random, int bound)` is faster than [`Random#nextInt(int bound)`](https://docs.oracle.com/javase/8/docs/api/java/util/Random.html#nextInt-int-).

## 0.1.5

- Support beta random number generator

## 0.1.4

- Improve speed performance of `GammaRNG`

## 0.1.3

- Support exponential random number generator

## 0.1.2

- Provide two implementations of Ziggurat algorithm for Gaussian RNG
    - `GaussianRNG.FAST_RNG` and `GaussianRNG.GENERAL_RNG`

## 0.1.1

- Support gamma random number generator


## 0.1.0

- Initial release
- Support gaussian random number generator
