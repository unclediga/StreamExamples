import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// JBreak-2016StreamAPI:рекомендации лучших собаководов
// ТагирВалеев

////////////////////// Терминальные операции (Terminal opers) //////////////////////////////

public class Valeev2016Terminal {

    // Задача №8: Преобразовать List<Map<K, V>> в Map<K, List<V>>

    // Дано: [{a=1, b=2}, {a=3, d=4, e=5}, {a=5, b=6, e=7} ]
    // Надо: {a=[1, 3, 5], b=[2, 6], d=[4], e=[5, 7] }
    //////////////////////////////////////////////////////////////////////////////
    public static void Task1() {

        List<Map<String, Integer>> input = new ArrayList(3);
        HashMap<String, Integer> m1 = new HashMap();
        m1.put("a", 1);
        m1.put("b", 2);
        input.add(m1);
        m1 = new HashMap();
        m1.put("a", 3);
        m1.put("d", 4);
        m1.put("e", 5);
        input.add(m1);
        m1 = new HashMap();
        m1.put("a", 5);
        m1.put("b", 6);
        m1.put("e", 7);
        input.add(m1);

        // одна из распространённых конструкций на stackoverflow
        Map m = input.stream().flatMap(map -> map.entrySet().stream()).collect(
                Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        // StreamEx.of(input) .flatMapToEntry(m -> m) .grouping();

        for (Object list : m.entrySet()) {
            System.out.println(list);
        }

        // Для упрощения можно статический импорт
        // import static java.util.stream.Collectors.*;
        m = input.stream()
            .flatMap(map -> map.entrySet().stream())
            .collect(groupingBy(Map.Entry::getKey, 
                                mapping(Map.Entry::getValue, 
                                toList())));


    }

    // Задача №9: Найти отделы с суммарной зарплатой всех сотрудников более миллиона рублей, 
    // отсортировав по убыванию суммарной зарплаты. 
    //////////////////////////////////////////////////////////////////////////////
    interface Employee {
        Department getDepartment();
        long getSalary();
    }

    public static void Task2() {
        // Можно в два стрима. 
        Map<Department, Long> deptSalaries = employees.stream()
                .collect(groupingBy(Employee::getDepartment, summingLong(Employee::getSalary)));

        deptSalaries.entrySet().stream().filter(e -> e.getValue() > 1_000_000)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        // 1. Чтобы сохранить порядок - выбираем  LinkedHashMap
        // 2. дефолтное поведение при совпадении ключей - (a, b) -> throw new IllegalStateException()                
        //    но народ ленится и пишет (a, b) -> a, т.к. всё равно коллизий не ожидается HashMap же.
        //    это плохо - запутывает, позволяет думать, что это специально выбранная стратегия.

        // StreamEx
        // Map<Department, Long> deptSalaries = StreamEx.of(employees)
        // .groupingBy(Employee::getDepartment, summingLong(Employee::getSalary));
        // EntryStream.of(deptSalaries) .filterValues(salary -> salary > 1_000_000)
        // .reverseSorted(Map.Entry.comparingByValue())
        // .toCustomMap(LinkedHashMap::new);

        // Стримоз головного мозга - запихивать всё в один стрим.
        // Например, с использоваением collectingAndThen()
        // Не делайте так никогда!
        Map<Department, Long> result = employees.stream().collect(collectingAndThen(
                groupingBy(Employee::getDepartment, summingLong(Employee::getSalary)),
                deptSalaries -> deptSalaries.entrySet().stream().filter(e -> e.getValue() > 1_000_000)
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new))));

    }
    


    // Задача №10: Найти все максимальные элементы по заданному компаратору. 
    //////////////////////////////////////////////////////////////////////////////
    public static void Task3() {
        // >> [JBreak, JPoint]
        Stream.of("JBreak", "JPoint", "Joker")
            .collect(maxAll(Comparator.comparing(String::length)))
            .forEach(System.out::println);
        
    }    

    public static <T> Collector<T, ?, List<T>> maxAll(Comparator<T> cmp) {
        BiConsumer<List<T>, T> accumulator = (list, t) -> {
            if (!list.isEmpty()) {
                int c = cmp.compare(list.get(0), t);
                if (c > 0)
                    return;
                if (c < 0)
                    list.clear();
            }
            list.add(t);
        };
        return Collector.of(ArrayList::new, 
                            accumulator, (l1, l2) -> {
                                            l2.forEach(t -> accumulator.accept(l1, t));
                                            return l1; 
                                        }
                );
    }

    //Задача №11: Проверить, уникальны ли входные элементы (да или нет). 
    //////////////////////////////////////////////////////////////////////////////
    public static void Task11() {
        Stream.of("JUG.ru", "JBreak", "JPoint", "Joker", "JFocus", "JavaOne", "JBreak")
                .allMatch(ConcurrentHashMap.newKeySet()::add);
    }

    // Задача №12: Посчитать хэш-код строки стандартным алгоритмом
    //////////////////////////////////////////////////////////////////////////////
    public static void Task12() {
        "JBreak".hashCode();
        // >> -2111961387
        "JBreak".chars().reduce(0, (a, b) -> a * 31 + b);
        // >> -2111961387

        "JBreak".chars().parallel().reduce(0, (a, b) -> a * 31 + b);
        // >> 4473889
        // (a*31+b)*31+c != a*31+(b*31+c)

        foldLeft("JBreak".chars().parallel(), 0, (a, b) -> a * 31 + b);
    }

    public static int foldLeft(IntStream input, int seed, IntBinaryOperator op) {
        int[] box = { seed };
        input.forEachOrdered(t -> box[0] = op.applyAsInt(box[0], t));
        return box[0];
    }
    
    public static void Task13() {
    }
    public static void Task14() {
    }
    public static void Task15() {
    }
    public static void main(String[] args) {
        // Task1();
        // Task2();
        Task3(); 
        // Task4();
    }
}