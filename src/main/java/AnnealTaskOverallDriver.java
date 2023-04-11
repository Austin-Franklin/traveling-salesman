import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.graphhopper.GraphHopper;

import cooling_functions.CoolingFunction;

/**
 *  array of paths = starting list copied crossoverNum times
 *  for totalFullRuns
 *      create crossoverNum threaddirectors
 *      for each theaddirector
 *          spawn threadsPerCrossoverRun threads
 *              do annealRunsPerThread
 *      get topNPathsPerCrossoverRun from each threaddirector
 *      concat and find crossoverNum best times out of all of them
 *      OrderedListCrossover each adjacent pair
 *      array of paths = crossover'd paths
 *          
 */
public class AnnealTaskOverallDriver {
    private int annealRunsPerThread = 10;
    private int threadsPerCrossoverRun = 5;
    private int crossoverNum = 10;
    private int totalFullRuns = 20;
    private int topNPathsPerCrossoverRun = 5;
    private GraphHopper hopper;
    private ArrayList<LocationPoint> startList;
    private CoolingFunction coolingFunction;
    private double boltzmannFactor;
    private ArrayList<LocationPoint> bestPath;
    private double bestTimeInMillis;

    /**
     * 
     * @param hopper Set up "car" profile first
     * @param startList this INCLUDES the starting which is assumed to be in the first index
     * @param coolingFunction copied
     * @param boltzmannFactor
     */
    public AnnealTaskOverallDriver(GraphHopper hopper, ArrayList<LocationPoint> startList, CoolingFunction coolingFunction, double boltzmannFactor) {
        this.hopper = hopper;
        this.startList = startList;
        this.coolingFunction = coolingFunction;
        this.boltzmannFactor = boltzmannFactor;
    }

    public AnnealTaskOverallDriver setAnnealRunsPerThread(int annealRunsPerThread) {
        this.annealRunsPerThread = annealRunsPerThread;
        return this;
    }

    public AnnealTaskOverallDriver setThreadsPerCrossoverRun(int threadsPerCrossoverRun) {
        this.threadsPerCrossoverRun = threadsPerCrossoverRun;
        return this;
    }

    public AnnealTaskOverallDriver settotalFullRuns(int totalFullRuns) {
        this.totalFullRuns = totalFullRuns;
        return this;
    }

    public void start() {
        ArrayList<ArrayList<LocationPoint>> pathsIntoAnnealTask = new ArrayList<>(Collections.nCopies(1, startList));
        for (int i = 0; i < totalFullRuns; ++i) {
            ArrayList<AnnealTaskThreadDirector> threadDirectorList = pathsIntoAnnealTask.stream()
                .map((path) -> new AnnealTask(hopper, path, annealRunsPerThread, coolingFunction.clone(), boltzmannFactor))
                .map((task) -> new AnnealTaskThreadDirector(task, 5, threadsPerCrossoverRun))
                .collect(Collectors.toCollection(ArrayList::new));

            
            threadDirectorList.forEach((td) -> td.run());
            threadDirectorList.forEach((td) -> {
                try {
                    td.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            //get a list of best runs (still in tasks) from each thread director
            ArrayList<AnnealTask> bestRuns = threadDirectorList.stream()
                .flatMap((td) -> td.getBestRuns(5 < topNPathsPerCrossoverRun ? 5 : topNPathsPerCrossoverRun).stream())
                .collect(Collectors.toCollection(ArrayList::new));

            //sort by best time and then get the paths for the best
            bestRuns.sort((a, b) -> Double.compare(a.getBestTime(), b.getBestTime()));

            //just grab the best since this is the last iteration
            if (i - 1 == totalFullRuns) {
                bestPath = bestRuns.get(0).getBestOrder();
                bestTimeInMillis = bestRuns.get(0).getBestTime();
                break;
            }

            //takes only the top part and converts to paths
            ArrayList<ArrayList<LocationPoint>> bestPaths 
                = bestRuns
                .subList(0, crossoverNum/2)
                .stream()
                .map((at) -> at.getBestOrder())
                .collect(Collectors.toCollection(ArrayList::new));
            
            

            //cartesian product the best paths then apply crossover to get a new path trying to preserve as many edges as possible of both parents
            pathsIntoAnnealTask = new ArrayList<>();
            //a workaround for thread safety i think
            final AtomicReference<ArrayList<ArrayList<LocationPoint>>> pathsIntoAnnealTaskAtomicReference = new AtomicReference<>(pathsIntoAnnealTask);
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
            pathsIntoAnnealTask = (ArrayList<ArrayList<LocationPoint>>) pathsIntoAnnealTask.subList(0, crossoverNum);
        }

    }

    public ArrayList<LocationPoint> getBestPath() {
        return bestPath;
    }

    public double getBestTimeInMillis() {
        return bestTimeInMillis;
    }
}
