import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

// JBreak-2016StreamAPI:рекомендации лучших собаководов
// ТагирВалеев

public class Valeev2016 {

    // Задача №1: Сделать источник дочерних узлов для заданного родительского DOM
    // XML узла
    //////////////////////////////////////////////////////////////////////////////

    public static Stream<Node> children(Node parent) {
        NodeList nodeList = parent.getChildNodes();
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
    }

    public static void Task1() {
        String txt = "<A>" + "  <B1/>" + "  <B2/>" + "  <B3>" + "    <C1/>" + "    <C2/>" + "  </B3>" + "</A>";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(txt)));
            Stream<Node> stream = children(document.getFirstChild());
            stream.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Задача №2: Сделать источник элементов списка вместе с индексами.
    //////////////////////////////////////////////////////////////////////////////

    public static class IndexedValue<T> {
        public final int index;
        public final T value;

        public IndexedValue(int index, T value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public String toString() {
            return "IndexedValue [index=" + index + ", value=" + value + "]";
        }
    }

    public static <T> Stream<IndexedValue<T>> withIndices(List<T> list) {
        return IntStream.range(0, list.size()).mapToObj(idx -> new IndexedValue<>(idx, list.get(idx)));
    }

    public static void Task2() {
        withIndices(Arrays.asList(1, 2, 3, 4, 5, 6)).forEach(System.out::println);
    }

    // Задача №3: Сделать источник пользователей в существующем классе Group.
    public class Group {
        private User[] users;

        public Stream<User> users() {
            return Arrays.stream(users);
        }
    }

    public static class User {
        String name;

        public User(String name) {
            this.name = name;
        }
    }

    public static class Group1 {
        private static Map<String, User> nameToUser = new HashMap<>();

        public static Stream<User> users() {
            return nameToUser.values().stream();
        }
    }

    public static class Group2 {
        private static List<User> users;

        public static Stream<User> users() {
            return users.stream();
        }
    }

    public static void Task3() {
        Group1.users().forEach(System.out::println);
    }

    // Задача №4:
    // Сделать источник, генерирующий декартово произведение списков строк.

    public static void Task4() {
     
        List<List<String>> input = Arrays.asList(Arrays.asList("a", "b", "c"), Arrays.asList("x", "y"), Arrays.asList("1", "2", "3"));
        Stream<String> stream = 
            input.get(0).stream()
                .flatMap(a -> input.get(1).stream()
                    .flatMap(b -> input.get(2).stream()
                        .map(c -> a + b + c)));
        
                stream.forEach(System.out::println);


        Supplier<Stream<String>> s = input.stream()
                // Stream<List<String>>
                .<Supplier<Stream<String>>>map(list -> list::stream)
                // Stream<Supplier<Stream<String>>>
                .reduce((sup1, sup2) -> () -> sup1.get().flatMap(e1 -> sup2.get().map(e2 -> e1 + e2)))
                // Optional<Supplier<Stream<String>>>
                .orElse(() -> Stream.of(""));

        s.get().forEach(System.out::println);
    }

    public static void main(String[] args) {
        // Task1();
        // Task2();
        // Task3();
        Task4();
    }
}