/**
 * Not seeing any progress can be disconcerting for a user. Therefore add a progress bar of some kind.
 */
public class ProgressTracker {
    private int currentVal = 0;
    private int endVal;

    /**
     * 
     * @param finalVal What is considered "complete"
     */
    public ProgressTracker(int finalVal) {
        endVal = finalVal;
    }

    /**
     * Prints a progress tracker
     */
    public void start() {
        System.out.println("May hang at 00% for a bit");
        System.out.print("Progress... 00%");
    }

    /**
     * increments the progress amount and rewrites progress percentage
     */
    public void tick() {
        currentVal++;
        updatePrint();
    }

    /**
     * rewrites progress percentage
     */
    public void updatePrint() {
        System.out.print("\b\b\b" + String.format("%02.0f", currentVal * 100.0 / endVal) + "%");
    }

    /**
     * prints complete
     */
    public void end() {
        System.out.print("\b\b\bcomplete.");
    }
}
