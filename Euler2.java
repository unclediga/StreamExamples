import java.math.BigInteger;
import java.util.Iterator;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Euler2 {
    static final BigInteger LIMIT = BigInteger.valueOf(150);
    static final int SIZE = 4000;

    public static Predicate<BigInteger> filterPredicate = b -> !b.testBit(0);
    public static Predicate<BigInteger> whilePredicate = b -> b.compareTo(LIMIT) < 0;

    static BinaryOperator<BigInteger> action = (x, y) -> x.add(y);

    public static BigInteger seqIterate() {
        Stream<BigInteger> stream = FibIterate.stream().filter(filterPredicate);
        return TakeWhile.stream(stream, whilePredicate).reduce(BigInteger.ZERO, action);
    }


    public static BigInteger seqSum() {
        BigInteger first = BigInteger.ZERO;
        BigInteger second = BigInteger.ONE;
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i < SIZE; i++) {
            System.out.println(first);
            if (!Euler2.whilePredicate.test(first))
                break;
            if (Euler2.filterPredicate.test(first))
                sum = action.apply(sum, first);
            BigInteger s = second.add(first);
            first = second;
            second = s;
        }
        return sum;
    }

    public static void main(String[] args) {
        System.out.println("old school  :" + seqSum());
        System.out.println("iterate     :" + seqIterate());
        // System.out.println("generate :" +
        // FibGenerate.stream().parallel().limit(SIZE).reduce(BigInteger.ZERO, action));
        // System.out.println("generate n/p:" +
        // FibGenerate.stream().limit(SIZE).reduce(BigInteger.ZERO, action));
        // System.out.println("iterate
        // :"+FibIterate.stream().parallel().limit(SIZE).reduce(BigInteger.ZERO,
        // action));
        // System.out.println("iterator :"+
        // FibIterator.stream().parallel().limit(SIZE).reduce(BigInteger.ZERO, action));
    }

}