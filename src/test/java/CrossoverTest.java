import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CrossoverTest {
    @Test
    public void testCrossover() {
        ArrayList<Integer> a = new ArrayList<>(
            List.of(1, 2, 3, 4, 5, 6, 7)
        );
        ArrayList<Integer> b = new ArrayList<>(
            List.of(1, 3, 6, 5, 7, 2, 4)
        );

        ArrayList<Integer> crossed = new OrderedListCrossover<>(a, b).crossover();

        assertEquals(a.size(), crossed.size());
    }
}
