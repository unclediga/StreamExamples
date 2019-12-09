import java.util.function.Predicate;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TakeWhile {
    public static <T> Stream<T> stream(Stream<? extends T> source, Predicate<T> p) {
        Spliterator<? extends T> spliterator = source.spliterator();
        Iterator<T> iterator = Spliterators.iterator(spliterator);
        TakeWhileIterator<T> tw = new TakeWhileIterator<>(iterator, p);
        Spliterator<T> s = Spliterators.spliteratorUnknownSize(tw, spliterator.characteristics() | Spliterator.ORDERED);
        return StreamSupport.stream(s, false);
    }

    private static class TakeWhileIterator<T> implements Iterator<T> {
        
        private Iterator<T> it;
        private Predicate<T> pp;
        private T res;

        TakeWhileIterator(Iterator<T> it, Predicate<T> pp) {
            this.it = it;
            this.pp = pp;
        }

        @Override
        public boolean hasNext() {
            if (it.hasNext()) {
                res = it.next();
                if (pp.test(res))
                    return true;
            }
            return false;
        }

        @Override
        public T next() {
            return res;
        }
    }
}
