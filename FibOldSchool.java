import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class FibOldSchool {

    final static BinaryOperator<BigInteger> action = (x, y) -> {
        return x.add(y);
    };
    static final int SIZE = 10;

    public void setup() {
    }

    public BigInteger seqSum() {
        BigInteger first = BigInteger.ZERO;
        BigInteger second = BigInteger.ONE;
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i < SIZE; i++) {
            sum = action.apply(sum, first);
            BigInteger s = second.add(first);
            first = second;
            second = s;
        }
        return sum;

    }

    public static void main(String[] args) {
        FibOldSchool fibOldSchool = new FibOldSchool();
        fibOldSchool.setup();
        System.out.println("old school  :" + fibOldSchool.seqSum());
        System.out.println("generate    :" + FibGenerate.stream().parallel().limit(SIZE).reduce(BigInteger.ZERO, action));
        System.out.println("generate n/p:" + FibGenerate.stream().limit(SIZE).reduce(BigInteger.ZERO, action));
        System.out.println("iterate     :"+FibIterate.stream().parallel().limit(SIZE).reduce(BigInteger.ZERO, action));
        System.out.println("iterator    :"+ FibIterator.stream().parallel().limit(SIZE).reduce(BigInteger.ZERO, action));        
    }
}