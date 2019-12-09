import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FibGenerate {

    BinaryOperator<BigInteger> action;
    static final int SIZE = 10;

    public void setup() {
        action = (x, y) -> {
            return x.add(y);
        };
    }

    public BigInteger seqSum() {

        return stream().limit(SIZE).reduce(BigInteger.ZERO,action);
    }
    
    
    public static Stream<BigInteger> stream(){

        return Stream.generate(new Supplier<BigInteger>() {
            BigInteger first = BigInteger.ZERO;
            BigInteger second = BigInteger.ONE;
            @Override
            public BigInteger get() {
                BigInteger s = second.add(first);
                first = second;
                second = s;
                return first;
            }
        });
    }


    public static void main(String[] args) {
        FibIterator fibGenerate = new FibIterator();
        fibGenerate.setup();
        System.out.println(fibGenerate.seqSum());
    }
}