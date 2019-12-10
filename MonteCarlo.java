import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MonteCarlo {
    public static final long SIZE = 100_000_000;

    public double getPiOldSchool() {
        long cnt = 0;
        for (int i = 0; i < SIZE; i++) {
            double x = ThreadLocalRandom.current().nextDouble();
            double y = ThreadLocalRandom.current().nextDouble();
            if (x * x + y * y < 1.0)
                cnt++;
        }
        return 4.0 * cnt / SIZE;
    }

    public double getPiZipBoxed() {
        long cnt = 0;
        DoubleStream as = new SplittableRandom().doubles(SIZE);
        DoubleStream bs = new SplittableRandom().doubles(SIZE);

        BiFunction<Double, Double, Double> zipper = new BiFunction<Double, Double, Double>() {
            @Override
            public Double apply(Double x, Double y) {
                return x * x + y * y;
            }
        };

        // System.out.println("1 + 2 = " + zipper.apply(Double.valueOf(1.0), Double.valueOf(2.0)));

        Stream<Double> zip = Zipper.zip(as.boxed(), bs.boxed(), zipper);

        cnt = zip.filter(x -> x < 1.0).count();
        return 4.0 * cnt / SIZE;
    }

    // public double getPiZipDouble() {
    // long cnt = 0;
    // DoubleStream as = new SplittableRandom().doubles(SIZE);
    // DoubleStream bs = new SplittableRandom().doubles(SIZE);
    // cnt = Zipper.zip(as, bs, (x, y) -> x * x + y * y).filter(x -> x <
    // 1.0).count();
    // return 4.0 * cnt / SIZE;
    // }

    static class Zipper {
        public static <A, B, C> Stream<C> zip(Stream<A> a, Stream<B> b, BiFunction<A, B, C> zipper) {
            Objects.requireNonNull(zipper);

            Spliterator<A> as = (Spliterator<A>) Objects.requireNonNull(a).spliterator();
            Spliterator<B> bs = (Spliterator<B>) Objects.requireNonNull(b).spliterator();

            long size = Math.min(as.estimateSize(), bs.estimateSize());
            int characteristics = as.characteristics() & bs.characteristics();

            // System.out.println("Zipper(zipper = "+zipper+")");
            // System.out.println("zipper "+ zipper.apply(t, u));

            Spliterator<C> cs = new ZipperSpliterator(as, bs, zipper, size, characteristics);

            return ((a.isParallel() || b.isParallel()) ? StreamSupport.stream(cs, true)
                    : StreamSupport.stream(cs, false));
        }

        private static final class ExtractingConsumer<T> implements Consumer<T> {
            private T value;

            @Override
            public void accept(T t) {
                value = t;
            }

            public T get() {
                return value;
            }

        }

        private static final class ZipperSpliterator<A, B, C> extends Spliterators.AbstractSpliterator<C> {
            Spliterator<A> as;
            Spliterator<B> bs;
            BiFunction<A, B, C> zipper3;

            ExtractingConsumer<A> aExtractor;
            ExtractingConsumer<B> bExtractor;

            ZipperSpliterator(Spliterator<A> as, Spliterator<B> bs, BiFunction<A, B, C> zipper2, long size,
                    int characteristics) {
                super(size, characteristics);
                this.as = as;
                this.bs = bs;
                this.zipper3 = zipper2;
                // System.out.println("ZipperSpliterator(zipper2 = "+zipper2+") => zipper3 = " + zipper3);
                aExtractor = new ExtractingConsumer<>();
                bExtractor = new ExtractingConsumer<>();
            }

            @Override
            public boolean tryAdvance(Consumer<? super C> action) {

                if (as.tryAdvance(aExtractor) && bs.tryAdvance(bExtractor)) {

                    A t = aExtractor.get();
                    // System.out.println("aExtractor.get() => " + t);
                    B u = bExtractor.get();
                    // System.out.println("bExtractor.get() => " + u);
                    // System.out.println("tryAdvance() => zipper3=" + zipper3);
                    //System.out.println("1 + 2 = " + zipper3.apply(Double.valueOf(1.0), Double.valueOf(2.0)));
                    C apply = zipper3.apply(t, u);
                    action.accept(apply);
                    return true;
                }
                return false;
            }
        }
    }

    public static void main(String[] args) {
        MonteCarlo monteCarlo = new MonteCarlo();
        long start = System.currentTimeMillis();
        System.out.println("old : " + monteCarlo.getPiOldSchool());
        System.out.println("time,ms " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        System.out.println("zipbox: " + monteCarlo.getPiZipBoxed());
        System.out.println("time,ms " + (System.currentTimeMillis() - start));
    }
}