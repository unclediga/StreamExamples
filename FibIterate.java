import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FibIterate {

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

        return Stream
            .iterate(new BigInteger[]{BigInteger.ZERO,BigInteger.ONE}, 
                (BigInteger[] p) -> new BigInteger[]{p[1],p[1].add(p[0])})
            .map((BigInteger[] p) -> p[0]);
        
    }


    public static void main(String[] args) {
        FibIterator fibIterate = new FibIterator();
        fibIterate.setup();
        System.out.println(fibIterate.seqSum());
    }
}