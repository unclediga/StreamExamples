import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



// JBreak-2016StreamAPI:рекомендации лучших собаководов
// ТагирВалеев

////////////////////// Промежуточные операции (Intermdiate) //////////////////////////////

public class Valeev2016Intermed {

    // Задача №5: Выбрать из стрима все элементы заданного класса.
    // Например, из списка дочерних Node для XML выбрать только элементы (Elements)
    //////////////////////////////////////////////////////////////////////////////
    public static void Task1() {
        String txt = "<A>" + "  <B1>text b1</B1>" + "  <B2/>" + "  <B3>" + "    <C1/>" + "    <C2/>" + "  </B3>" + "</A>";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        NodeList nodeList = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(txt)));
            nodeList = document.getFirstChild().getChildNodes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Вариант 1.1, 1.2  Получаем стрим Node, а нужны Элементы
        IntStream.range(0, nodeList.getLength())
                 .mapToObj(nodeList::item)
                 .filter(node -> node instanceof Element)
                 .forEach(System.out::println);

        IntStream.range(0, nodeList.getLength())
                 .mapToObj(nodeList::item)
                 .filter(Element.class::isInstance)
                 .forEach(System.out::println);

        // Вариант 1.3, 1.4  Получаем стрим Элементов
        IntStream.range(0, nodeList.getLength())
                 .mapToObj(nodeList::item)
                 .filter(node -> node instanceof Element)
                 .map(node -> (Element)node)
                 .forEach(System.out::println);

        IntStream.range(0, nodeList.getLength())
                 .mapToObj(nodeList::item)
                 .filter(Element.class::isInstance)
                 .map(Element.class::cast)
                 .forEach(System.out::println);

        // Вариант 2 попытка обобщения. Недостаток - ломает порядок стрима, 
        // рушится  последовательность записи через точку         
        select1(IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item), Element.class)
                .forEach(System.out::println);

        // Вариант 2 попытка обобщения. Теперь не ломает...
        IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .flatMap(select2(Element.class))
                .forEach(System.out::println);

        // чтобы не запоминать, какую функцию вызвать и как в flatMap(),
        // в StreamEx написан "стримовый" вариант этой функции кастинга....
        // IntStreamEx.range(0, nodeList.getLength())
        //         .mapToObj(nodeList::item)
        //         .select(Element.class)
        //         .forEach(System.out::println);
    }

    public static <T, TT> Stream<TT> select1(Stream<T> stream, Class<TT> clazz){
        return stream.filter(clazz::isInstance).map(clazz::cast);
    }

    public static <T, TT> Function<T,Stream<TT>> select2(Class<TT> clazz){
        return e -> (clazz.isInstance(e) ? Stream.of(clazz.cast(e)) : null);
     }

         
    // Задача №6: Оставить значения, которые повторяются не менее n раз.
    //////////////////////////////////////////////////////////////////////////////
    public static void Task2() {
        List<String> list = Arrays.asList("JPoint", "Joker", "JBreak", "JBreak", "Joker", "JBreak");

        // некрасивое решение...
        // похоже, что x -> x  есть в библиотеке как Function.identity()
        Map<String, Long> counts = list.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        counts.values().removeIf(cnt -> cnt < 3);
        counts.keySet().forEach(System.out::println);
        
        // красивое решение .. :)
        // Одно "но": в спецификации к Predicate написано, что он должен быть stateless,
        // т.е. не должен хранить состояние. Сказали: "Под нашу ответственность"
        list.stream()
            .filter(distinct(3))
            .forEach(System.out::println);

        list = Arrays.asList("a", "a", "a");
        list.stream()
            .filter(distinct(2))
            .forEach(System.out::println);

    }
    
    public static <T> Predicate<T> distinct(long atLeast) {
        Map<T, Long> map = new ConcurrentHashMap<>();
        
        return t -> {System.out.println("t:"+t+":" + map.containsKey(t));
                     return map.merge(t, 1L, Long::sum) == atLeast;};
    }

    /// Задача №7: Реализовать takeWhile: прервать Stream по условию 
    //////////////////////////////////////////////////////////////////////////////
    public static void Task3() {
        // В Java9 появились 
        //      takeWhile, 
        //      dropWhile

        // Недостатки:
        // 1. Некорректно работает с параллельными стримами
        // 2. "не короткозамкнутый" - всегда проверяются все элементы до конца
        // 3. вытекает из п.2 - не работает с бесконечными стримами.
        Stream.of(1,2,3,-1,4,5,6).filter(takeWhile(x -> x > 0)) .forEach(System.out::println);

        // для выхода из стрима можно использовать:
        // iterator
        // spliterator (предпочтительно, более гибкие возможности)
        takeWhile(new Random().ints().boxed(), x -> x % 10 != 0)
                  .forEach(System.out::println);

        // В StreamEx всё есть из коробки..
        // IntStreamEx.of(new Random()).boxed()
        // .takeWhile(x -> x % 10 != 0)
        // .forEach(System.out::println);
    }
    
    public static <T> Predicate<T> takeWhile(Predicate<T> predicate) {
        AtomicBoolean matched = new AtomicBoolean();
        return t -> {
            if (matched.get())
                return false;
            if (!predicate.test(t)) {
                matched.set(true);
                return false;
            }
            return true;
        };
    }

    public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<T> predicate) {
        Spliterator<T> src = stream.spliterator();
        Spliterator<T> res = new Spliterators.AbstractSpliterator<T>(src.estimateSize(),
                src.characteristics() & ~Spliterator.SIZED & ~Spliterator.SUBSIZED) {
            boolean finished = false;
            T next;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (finished || !src.tryAdvance(t -> next = t) || !predicate.test(next)) {
                    finished = true;
                    return false;
                }
                action.accept(next);
                return true;
            }
        };
        //  нужно закрыть исходный стрим!! 
        // (у Куксенко такого не было. Типа, так делают все библиотеки)
        return StreamSupport.stream(res, stream.isParallel()).onClose(stream::close);
    }

    public static void main(String[] args) {
        // Task1();
        Task2();
        // Task3();
        // Task4();
    }
}