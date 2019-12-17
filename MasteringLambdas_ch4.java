import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.Year;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class MasteringLambdas_ch4 {

    static ArrayList<Book> fillLibrary() {

        ArrayList<Book> lib = new ArrayList<>();

        Book nails = new Book("Fundamentals of Chinese Fingernail Image", Arrays.asList("Li", "Fu", "Li"),
                new int[] { 256 }, // pageCount per volume
                Year.of(2014), // publication date
                25.2, // height in cms
                Topic.MEDICINE);
        Book dragon = new Book("Compilers: Principles, Techniques and Tools",
                Arrays.asList("Aho", "Lam", "Sethi", "Ullman"), new int[] { 1009 }, Year.of(2006), // publication date
                                                                                                   // (2nd edition)
                23.6, Topic.COMPUTING);
        Book voss = new Book("Voss", Arrays.asList("Patrick White"), new int[] { 478 }, Year.of(1957), 19.8,
                Topic.FICTION);
        Book lotr = new Book("Lord of the Rings", Arrays.asList("Tolkien"), new int[] { 531, 416, 624 }, Year.of(1955),
                23.0, Topic.FICTION);

        lib.add(dragon);
        lib.add(lotr);
        lib.add(nails);
        lib.add(voss);

        return lib;
    }

    public static void main(String[] args) {
        ArrayList<Book> library = fillLibrary();

        Supplier<Deque<DispRecord>> supplier = ArrayDeque::new;
        BiConsumer<Deque<DispRecord>, Book> accumulator = (dq, b) -> {
            int lastDist = dq.isEmpty() ? 0 : dq.getLast().totalDisp();
            DispRecord dr = new DispRecord(b.getTitle(), lastDist, Arrays.stream(b.getPageCount()).sum());
            dq.add(dr);
        };
        BinaryOperator<Deque<DispRecord>> combiner = (left, right) -> {
            if (left.isEmpty())
                return right;
            int newDisp = left.getLast().totalDisp();
            List<DispRecord> list = right.stream().map(dr -> new DispRecord(dr.title, dr.disp + newDisp, dr.length))
                    .collect(toList());
            left.addAll(list);
            return left;
        };
        Function<Deque<DispRecord>, Map<String, Integer>> finisher = s -> s.stream()
                .collect(toMap(dr -> dr.title, dr -> dr.disp));
        Map<String, Integer> result = library.stream().collect(Collector.of(supplier, accumulator, combiner, finisher));

        for (Map.Entry<String, Integer> e : result.entrySet()) {
            System.out.printf("k:%s -> v:%d\n", e.getKey(), e.getValue());
        }
    }

}

class DispRecord {
    final String title;
    final int disp, length;

    public DispRecord(String title, int disp, int length) {
        this.title = title;
        this.disp = disp;
        this.length = length;
    }

    public int totalDisp() {
        return disp + length;
    }

}

enum Topic {
    COMPUTING, FICTION, HISTORY, MEDICINE, PROGRAMMING
}

class Book {
    String title;
    List<String> authors;
    int[] pageCount;
    Year pubDate;
    Topic topic;
    double height;
    // new Book("Lord of the Rings", Arrays.asList("Tolkien"), new int[] { 531, 416,
    // 624 }, Year.of(1955),

    public String getTitle() {
        return title;
    }

    public int[] getPageCount() {
        return pageCount;
    }

    public Book(String title, List<String> authors, int[] pageCount, Year pubDate, double height, Topic topic) {
        this.title = title;
        this.authors = authors;
        this.pageCount = pageCount;
        this.pubDate = pubDate;
        this.topic = topic;
        this.height = height;
    }
}
