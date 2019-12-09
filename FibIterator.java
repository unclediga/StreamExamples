import java.math.BigInteger;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FibIterator {

    BinaryOperator<BigInteger> action;
    static final int SIZE = 10;

    public void setup() {
        action = (x, y) -> {
            return x.add(y);
        };
    }

    public BigInteger seqSum() {

        return stream().limit(SIZE).reduce(BigInteger.ZERO, action);
    }

    public static Stream<BigInteger> stream() {

        Spliterator<BigInteger> spliterator = Spliterators.spliteratorUnknownSize(new FibbonachiIterator(), 
        Spliterator.ORDERED|Spliterator.SORTED|Spliterator.NONNULL|Spliterator.IMMUTABLE);

        return StreamSupport.stream(spliterator, false);

    }

    static class FibbonachiIterator implements Iterator<BigInteger> {

        BigInteger first = BigInteger.ZERO;
        BigInteger second = BigInteger.ONE;

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public BigInteger next() {

            BigInteger f = first;
            BigInteger s = second.add(first);
            first = second;
            second = s;
            return f;
        }
        
    }

    public static void main(String[] args) {
        FibIterator fibIterator = new FibIterator();
        fibIterator.setup();
        System.out.println(fibIterator.seqSum());
    }
}