import java.rmi.server.LogStream;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Leibniz {
    public static final long SIZE = 1000_000_000;

    public static double getOldSchool() {
        double r = 0.0;
        for (long i = 0; i < SIZE; i++) {

            double d = ((i & 1) == 0 ? 1.0 : -1.0) / (2 * i + 1);
            r += d;
            // System.out.printf("r=%f  i=%f\n", r, d);
        }
        return r * 4;
    }

    public static double getPiStream() {
        DoubleStream mapToDouble = LongStream.range(0L, SIZE)
                .mapToDouble(i -> ((i & 1) == 0 ? 1.0 : -1.0) / (2 * i + 1));
        // mapToDouble.forEach(System.out::println);
        return mapToDouble.sum() * 4;
    }

    public static double getPiParallelStream() {
        DoubleStream mapToDouble = LongStream.range(0L, SIZE)
                .mapToDouble(i -> ((i & 1) == 0 ? 1.0 : -1.0) / (2 * i + 1)).parallel();
        // mapToDouble.forEach(System.out::println);
        return mapToDouble.sum() * 4;
    }

    public static void main(String[] args) {
        System.out.println("pi    : " + Math.PI);
        System.out.println("old   : " + getOldSchool());
        System.out.println("stream: " + getPiStream());
        System.out.println("parall: " + getPiParallelStream());
    }
}
