import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// JBreak-2016StreamAPI:рекомендации лучших собаководов
// ТагирВалеев

////////////////////// Применение различных операций //////////////////////////////
public class Valeev2016Mix {

    // Задача №13: Обработать перекрывающиеся пары из входного потока
    ///////////////////////////////////////////////////////////////////////////////
    public static void Task13() {
        List<String> input = Arrays.asList("A", "B", "C", "D", "E");

        // StreamEx.of(input)
        //         .pairMap((a, b) -> a + " -> " + b)
        //         .toList();
        // >> [A -> B, B -> C, C -> D, D -> E]

        // легко сделать, если не обращать внимание на параллельность выполнения.
        // Например...но приходится возиться с индексами.
        IntStream.range(0, input.size()-1)
                .mapToObj(idx -> input.get(idx)+" -> "+input.get(idx+1))
                .collect(Collectors.toList());        

        // чтобы не возиться с индексами
        List l1 = pairs(input, (a,b) -> a + " -> "+ b)
                  .collect(toList());

        // не далайте так никогда!
        // reduce() не гарантирует, что элементы в reduce пойдут по-порядку 
        // даже в последовательных стримах. Могут изменить реализацию.
        // в параллельных это естесственно работать не будет.
        List l2 = new ArrayList();
        input.stream().reduce((a, b) -> { 
                                        l2.add(a + " -> "+ b); 
                                        return b;});


<<<<<<< HEAD
        // при "стримозе" и "regex-мозе" мозга(но прикольно !)
=======
        // при "стримозе" (но прикольно !)
>>>>>>> b9cca4ab9a955aa6bafc1272dbf5b70c8700f81f
        // работает и для параллельных потоков, но..
        // 1. создавать/разбирать/менять строки накладно
        // 2. что будет, если в исходном потоке появятся ";" и "->" ?

        String s = input.stream().map(a -> a + ";" + a).collect(Collectors.joining(" -> "));
        // >> A;A -> B;B -> C;C -> D;D -> E;E
        Pattern.compile(";").splitAsStream(s).filter(b -> b.contains(" -> ")).collect(Collectors.toList());
        // >> A -> B;B -> C;C -> D;D -> E
        
        l1 = input.stream().collect(pairs((a,b) -> a + " -> "+ b,toList()));

        for (Object el : l1) {
            System.out.println(el);
        }                  
    }
    // плюс этого коллектора - он берёт downstream и применяет его к полученному результату. Это удобно, 
    // чтобы пользователь вроде как смог решить, как дальше "коллектить" полученный результат (???). 
    // Удобно делать один внутренний класс и там хранить результаты и реализовывать аккумулятор, комбайнер и пр.

    // Как я понял, главная - последняя строка:
    /*
     * 
     * static <T,A,R> Collector<T,A,R> of(Supplier<A> supplier, 
     *                                    BiConsumer<A,T> accumulator, 
     *                                    BinaryOperator<A> combiner, 
     *                                    Function<A,R> finisher,
     *                                    Collector.Characteristics... characteristics) 
     * 
     * Returns a new Collector described by the given supplier, accumulator, 
     * combiner, and finisher functions.
     * 
     * Type Parameters: 
     *      T - The type of input elements for the new collector 
     *      A - The intermediate accumulation type of the new collector 
     *      R - The final result type of the new collector 
     * Parameters: 
     *      supplier    - The supplier function for the new collector
     *      accumugit lator - The accumulator function for the new collector
     *      combiner    - The combiner function for the new collector 
     *      finisher    - The finisher function for the new collector 
     *      characteristics - The collector characteristics for the new collector 
     * 
     *      Returns: the new Collector
     */

    static <T, TT, A, R> Collector<T, ?, R> pairs(BiFunction<T, T, TT> mapper, Collector<TT, A, R> downstream) {
        class Acc {
            T first, last;
            A acc = downstream.supplier().get();

            void add(T next) {
                if (last == null)
                    first = next;
                else
                    downstream.accumulator().accept(acc, mapper.apply(last, next));
                last = next;
            }

            Acc combine(Acc other) {
                downstream.accumulator().accept(acc, mapper.apply(last, other.first));
                acc = downstream.combiner().apply(acc, other.acc);
                last = other.last;
                return this;
            }
        }
        return Collector.of(Acc::new, Acc::add, Acc::combine, acc -> downstream.finisher().apply(acc.acc));
    }

    public static <T, R> Stream<R> pairs(List<T> list, BiFunction<T, T, R> mapper) {
        // так нельзя, потому-что IntStream - поток примитивов int, над которыми позволены действия!
        //IntStream.range(0, list.size() - 1).map(idx -> mapper.apply(list.get(idx), list.get(idx + 1)));
        return IntStream.range(0, list.size() - 1)
                        .mapToObj(idx -> 
                            mapper.apply(list.get(idx), list.get(idx + 1)));
    }

    // Задача №14: Разбить входной поток на группы равной длины
    ///////////////////////////////////////////////////////////////////////////////
    public static void Task14() {
        List<String> input = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        // >> [[a, b, c], [d, e, f], [g, h]]

        // задачу нельзя нормально распараллелить. Сплитератор разбивает массив произвольно...
        // или последовательно
        // коллектор нельзя написать- нельзя сказать коллектору "работай параллельно"
        // 
        List<List<String>> list = ofSublists(input, 3).map(ArrayList::new).collect(Collectors.toList());
        // >>[[a,b,c],[d,e,f],[g,h]]

    }

    static <T> Stream<List<T>> ofSublists(List<T> list, int n) {
        int size = list.size();
        return IntStream.rangeClosed(0, (size - 1) / n)
                .mapToObj(idx -> list.subList(idx * n, Math.min(size, idx * n + n)));
    }


    // Задача №15: Разбить входной поток на группы, начинающиеся с данного элемента
    ///////////////////////////////////////////////////////////////////////////////
    public static void Task15() {

        List<String> input = Arrays.asList("Start", "a", "b", "c", 
                                           "Start", "d", 
                                           "Start", "e", "f", "g", "h", "i",
                                           "Start");
        // StreamEx.of(input).groupRuns((a, b) -> !b.equals("Start")).toList();
        // >> [[Start, a, b, c], [Start, d], [Start, e, f, g, h, i], [Start]]

        IntStream.rangeClosed(0, input.size())
                .filter(idx -> idx == 0 || idx == input.size() || input.get(idx).equals("Start")).boxed()
                .collect(pairs(input::subList, Collectors.toList()));
        // >> [[Start, a, b, c], [Start, d], [Start, e, f, g, h, i], [Start]]

    }

    public static void main(String[] args) {
        Task13();
    }
}
