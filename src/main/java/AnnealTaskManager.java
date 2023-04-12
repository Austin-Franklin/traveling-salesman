import java.util.ArrayList;
import java.util.concurrent.*;

public class AnnealTaskManager {
    private ExecutorService executor;
    private ArrayList<AnnealTask> taskList;
    private SpinnerThread spin = new SpinnerThread("Running route optimization");
    
    /**
     * 
     * @param annealTask Single task to be copied and done over and over
     * @param threadPoolSize How many threads to be taken up at one time
     * @param totalThreads How many runs of AnnealTask object to be ran
     */
    public AnnealTaskManager (AnnealTask annealTask, int threadPoolSize, int totalThreads) {
        executor = Executors.newFixedThreadPool(threadPoolSize);
        taskList = new ArrayList<>();
        for (int i = 0; i < totalThreads; ++ i) {
            taskList.add(annealTask.copy());
        }
    }

    public void run() {
        spin.start();
        //ArrayList<AnnealTask> taskList = new ArrayList<>();
        for (AnnealTask task : taskList) {
            executor.submit(task);
        }
    }

    public void join() throws InterruptedException{
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    public void stop() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        spin.stop();
    }

    public ArrayList<LocationPoint> getBestOrder() {
        long bestTime = Long.MAX_VALUE;
        ArrayList<LocationPoint> bestPath = null;
        for (AnnealTask task : taskList) {
            if (task.getBestTime() < bestTime) {
                bestPath = task.getBestOrder();
            }
        }
        return bestPath;
    }
}
