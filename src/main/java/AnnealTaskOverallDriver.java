import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.graphhopper.GraphHopper;

import cooling_functions.CoolingFunction;

/**
 *  array of paths = starting list copied crossoverNum times
 *  shuffle each starting path
 *  for each crossovers
 *      create crossoverNum threaddirectors from each starting path
 *      for each theaddirector
 *          spawn totalThreadsPerPath threads
 *              do totalAnneals/crossovers
 *      get topNPathsPerCrossoverRun from each thread director
 *      concat and find crossoverNum best times out of all of them
 *      OrderedListCrossover each pair of best paths
 *      set crossoverNum random paths to each thread director to continue annealing
 *          
 */
public class AnnealTaskOverallDriver {
    private int totalAnneals = 1000;
    private int totalConcurrentThreads = 5;
    //this many different concurrent runs of a single given path
    private int totalThreadsPerPath = 10;
    //this many different paths chosen from crossovers
    private int crossoverNum = 10;
    //how many crossovers ran
    private int crossoverActions = 20;
    //the top number of paths for each annealing section
    private int topNPathsPerCrossoverRun = 5;
    private GraphHopper hopper;
    private ArrayList<LocationPoint> startPath;
    private CoolingFunction coolingFunction;
    private double boltzmannFactor;
    private ArrayList<LocationPoint> bestPath;
    private double bestTimeInMillis;
    // private SpinnerThread spin = new SpinnerThread("Running route optimization");

    /**
     * 
     * @param hopper Set up "car" profile first
     * @param startPath this INCLUDES the starting which is assumed to be in the first index
     * @param coolingFunction copied
     * @param boltzmannFactor
     */
    public AnnealTaskOverallDriver(GraphHopper hopper, ArrayList<LocationPoint> startPath, CoolingFunction coolingFunction, double boltzmannFactor) {
        this.hopper = hopper;
        this.startPath = startPath;
        this.coolingFunction = coolingFunction;
        this.boltzmannFactor = boltzmannFactor;
    }


    public void start() {
        //spin.start();
        AtomicReference<ExecutorService> executorRef = new AtomicReference<>(Executors.newFixedThreadPool(totalConcurrentThreads));
        //duplicate starting path
        List<ArrayList<LocationPoint>> pathsIntoAnnealTask = new ArrayList<>(Collections.nCopies(crossoverNum, startPath));
        //shuffle initial list
        pathsIntoAnnealTask.forEach(path -> Collections.shuffle(path.subList(1, path.size() - 1)));

        //map each starting path to a thread director
        ArrayList<AnnealTaskThreadDirector> threadDirectorList = pathsIntoAnnealTask.stream()
            .map(path -> new AnnealTask(hopper, path, totalAnneals/crossoverActions, coolingFunction, boltzmannFactor))
            .map(task -> new AnnealTaskThreadDirector(task, executorRef.get(), totalThreadsPerPath))
            .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < crossoverActions; ++i) {
            System.out.println(i);
            //do annealing for some time
            threadDirectorList.forEach((td) -> td.run());
            executorRef.get().shutdown();
            try {
                executorRef.get().awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //get a list of best runs (still in tasks) from each thread director
            ArrayList<AnnealTask> bestRuns = threadDirectorList.stream()
                .flatMap(td -> td.getBestRuns(Math.min(topNPathsPerCrossoverRun, 5)).stream())
                .collect(Collectors.toCollection(ArrayList::new));

            //sort by best time and then get the paths for the best
            bestRuns.sort((a, b) -> Double.compare(a.getBestTime(), b.getBestTime()));

            //just grab the best since this is the last iteration
            if (i + 1 == crossoverActions) {
                bestPath = bestRuns.get(0).getBestOrder();
                bestTimeInMillis = bestRuns.get(0).getBestTime();
                break;
            }

            //takes only the top part and converts to paths
            ArrayList<ArrayList<LocationPoint>> bestPaths 
                = bestRuns
                .subList(0, Math.min(crossoverNum/2 + 1, totalThreadsPerPath))
                .stream()
                .map(at -> at.getBestOrder())
                .collect(Collectors.toCollection(ArrayList::new));
            
            

            //cartesian product the best paths then apply crossover to get a new path trying to preserve as many edges as possible of both parents
            pathsIntoAnnealTask.clear();
            pathsIntoAnnealTask.add(bestPaths.get(0));
            //a workaround for thread safety i think
            final AtomicReference<List<ArrayList<LocationPoint>>> pathsIntoAnnealTaskAtomicReference = new AtomicReference<>(pathsIntoAnnealTask);
            bestPaths.stream()
                .forEach(path1 -> {
                bestPaths.stream()
                    .forEach(path2 -> {
                        pathsIntoAnnealTaskAtomicReference.get().add(
                            new OrderedListCrossover<>(path1, path2).crossover()
                        );
                    });
                });

            
            //get crossoverNum random paths as new inputs
            Collections.shuffle(pathsIntoAnnealTask);
            pathsIntoAnnealTask = pathsIntoAnnealTask.subList(0, crossoverNum);

            //set task's paths as crossover'd ones
            for (int j = 0; j < crossoverNum; ++j) {
                threadDirectorList.get(j).setNewPath(
                    pathsIntoAnnealTask.get(j)
                );
            }

            //reset multithreading executor
            executorRef.set(Executors.newFixedThreadPool(totalConcurrentThreads));
            threadDirectorList.forEach(td -> td.setExecutor(executorRef.get()));

            //resume annealing
        }
        //spin.stop();
    }

    public ArrayList<LocationPoint> getBestPath() {
        return bestPath;
    }

    public double getBestTimeInMillis() {
        return bestTimeInMillis;
    }


    public AnnealTaskOverallDriver setTotalAnneals(int totalAnneals) {
        this.totalAnneals = totalAnneals;
        return this;
    }


    public AnnealTaskOverallDriver setTotalConcurrentThreads(int totalConcurrentThreads) {
        this.totalConcurrentThreads = totalConcurrentThreads;
        return this;
    }


    public AnnealTaskOverallDriver setTotalThreadsPerPath(int totalThreadsPerPath) {
        this.totalThreadsPerPath = totalThreadsPerPath;
        return this;
    }


    public AnnealTaskOverallDriver setCrossoverNum(int crossoverNum) {
        this.crossoverNum = crossoverNum;
        return this;
    }


    public AnnealTaskOverallDriver setCrossoverActions(int crossovers) {
        this.crossoverActions = crossovers;
        return this;
    }


    public AnnealTaskOverallDriver setTopNPathsPerCrossoverRun(int topNPathsPerCrossoverRun) {
        this.topNPathsPerCrossoverRun = topNPathsPerCrossoverRun;
        return this;
    }

    
}
