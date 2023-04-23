import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.graphhopper.reader.osm.Pair;

/**
 * Mixing edges instead of nodes is a lot more intensive
 */
public class OrderedListCrossover<T> {
    /**
     * val1 -> (val2, val3) means val2 and val3 are adjacent to va1
     * Set because i dont want duplicates
     */
    private Map<T, Set<T>> adjacencyList;
    private ArrayList<T> usedForTrackingUnvisited;
    private T firstNode;
    private int size;

    /**
     * Both arrays must have the same size, same contents when unordered, and same first value
     * @param firstOrderedList
     * @param secondOrderedList
     */
    public OrderedListCrossover(ArrayList<T> firstOrderedList, ArrayList<T> secondOrderedList) {
        //checks
        if (firstOrderedList.size() == 0)
            throw new IllegalStateException("Must have at least 1 element");
        if (firstOrderedList.size() != secondOrderedList.size())
            throw new IllegalStateException("Both lists must have the same size");
        if (!firstOrderedList.get(0).equals(secondOrderedList.get(0)))
            throw new IllegalStateException("Both lists must start with the same node");

        firstNode = firstOrderedList.get(0);
        size = firstOrderedList.size();
        usedForTrackingUnvisited = new ArrayList<>(firstOrderedList);

        Map<T, Set<T>> adjList1 = createAdjacencyList(firstOrderedList);
        Map<T, Set<T>> adjList2 = createAdjacencyList(secondOrderedList);

        adjacencyList = merge(adjList1, adjList2);
    }

    private Map<T, Set<T>> createAdjacencyList(ArrayList<T> orderedList) {
        Map<T, Set<T>> adjList = new HashMap<>();
        for (int i = 0; i < size; ++i) {
            adjList.put(
                orderedList.get(i), 
                new HashSet<>(
                    Arrays.asList(
                        orderedList.get(
                            i - 1 < 0 ? size - 1 : i - 1
                        ),
                        orderedList.get(
                            (i + 1) % size
                        )
                    )
                )
            );
        }
        return adjList;
    }

    private Map<T, Set<T>> merge(Map<T, Set<T>> first, Map<T, Set<T>> second) {
        Map<T, Set<T>> mergedMap = first;

        //copy over second's stuff
        second.forEach((key, value) -> {
            mergedMap.merge(key, value, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            });
        });

        return mergedMap;
    }


    /**
     * Usable mulitple times if desired
     * @return An ordered list of T mixed from the 2 parent lists given in the constructor
     */
    public ArrayList<T> crossover() {
        //copy so this can be called again
        Map<T, Set<T>> copyAdjList = new HashMap<>(adjacencyList);
        //keep track of unvisited
        ArrayList<T> unvisited = new ArrayList<>(usedForTrackingUnvisited);
        //return array
        ArrayList<T> crossed = new ArrayList<>();
        T nextNode = firstNode;
        crossed.add(nextNode);
        unvisited.remove(nextNode);
        while (size > crossed.size()) {
            //remove nextNode from being a neighbor
            final T workAround = nextNode; //some weird workaround for java not liking v.remove(nextNode)
            copyAdjList.forEach((k, v) -> {
                v.remove(workAround);
            });
            //if nextNode has neighbors, get neighbor with smallest adjacency list
            if (copyAdjList.get(nextNode).size() != 0) {
                ArrayList<Pair<T, Integer>> neighborSizes = new ArrayList<>();
                copyAdjList.get(nextNode).forEach(k -> {
                    //if (copyAdjList.get(k).size() != 0) {
                        neighborSizes.add(
                            new Pair<>(k, copyAdjList.get(k).size())
                        );
                    //}
                });
                //sort from smallest to largest to get smallest adjacency list
                neighborSizes.sort((a, b) -> Integer.compare(a.second, b.second));
                //remove all not equal in size to the first (smallest)
                neighborSizes.removeIf(a -> !a.second.equals(neighborSizes.get(0).second));
                //assign random node since all left are of equal connectivity
                nextNode = neighborSizes.get((int) (Math.random() * neighborSizes.size())).first;
            }
            //if not, grab a random node
            else {
                nextNode = unvisited.get((int) (Math.random() * unvisited.size()));
                
            }
            crossed.add(nextNode);
            unvisited.remove(nextNode);
        }
        return crossed;
    }
}
