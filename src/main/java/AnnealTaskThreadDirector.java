import java.util.ArrayList;
import java.util.concurrent.*;

public class AnnealTaskThreadDirector {
    private ExecutorService executor;
    private ArrayList<AnnealTask> taskList;
    private SpinnerThread spin = new SpinnerThread("Running route optimization");

    
    /**
     * 
     * @param annealTask Single task to be copied and done over and over
     * @param threadPoolSize How many threads to be taken up at one time
     * @param totalThreads How many runs of AnnealTask object to be ran
     */
    public AnnealTaskThreadDirector (AnnealTask annealTask, ExecutorService executor, int totalThreads) {
        this.executor = executor;
        taskList = new ArrayList<>();
        for (int i = 0; i < totalThreads; ++ i) {
            taskList.add(annealTask.copy());
        }
    }

    public void run() {
        spin.start();
        for (AnnealTask task : taskList) {
            executor.submit(task);
        }
    }

    public void stop() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        spin.stop();
    }

    //still in AnnealTask since it also stores the time
    public ArrayList<AnnealTask> getBestRuns(int amount) {
        //sort to get best time
        taskList.sort((a, b) -> Double.compare(a.getBestTime(), b.getBestTime()));
        
        //map to path
        return (ArrayList<AnnealTask>) taskList.subList(0, amount);
    }

    /**
     * Use after crossover to resume annealing
     * @param newPath
     */
    public void setNewPath(ArrayList<LocationPoint> newPath) {
        taskList.forEach(task -> task.setPath(newPath));
    }
}
