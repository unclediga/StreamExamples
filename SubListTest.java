import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubListTest {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("a","b","c","d","e","f");
        ArrayList<String> list2 = new ArrayList();
        list2.addAll(list);
        System.out.println("list ("+System.identityHashCode(list)+"):" + list);
        System.out.println("list2 ("+System.identityHashCode(list2)+"):" + list2);

        List list3 = list2.subList(2,4);
        System.out.println("list3 ("+System.identityHashCode(list3)+"):" + list3);
        System.out.println("list.subList(2,4) ("+System.identityHashCode(list.subList(2,4))+"): " + list.subList(2,4));
        System.out.println("list2.subList(2,4) ("+System.identityHashCode(list2.subList(2,4))+"): " + list2.subList(2,4));
        
        // UnsupportedOperationException
        // list.subList(2,4).clear(); 
        list2.subList(2,4).clear(); 
        System.out.println("list2 after 'list2.subList(2,4).clear()' ["+System.identityHashCode(list2.subList(2,4))+"]:" + list2);

    }
}
