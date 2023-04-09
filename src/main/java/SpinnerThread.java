/**
 * Used for printing out a rotating character to indicate some sort of "please wait im not stuck i promise"
 */
public class SpinnerThread {
    private String[] characters = {"|", "/", "-", "\\"};
    private boolean repeat = true;
    private int index = 0;
    private Thread thread;

    /**
     * 
     * @param message Message you want displayed before the spinner
     */
    public SpinnerThread(String message) {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.print(message + " ");
                if (message.charAt(message.length() - 1) != ' ') {
                    System.out.print(' '); //needs some padding since \b will erase it
                }
                while(repeat) {
                    System.out.print("\b" + characters[index]);
                    index = (index + 1) % 4;
                    try{Thread.sleep(100);}catch(InterruptedException e){repeat = false;};
                }
            }
        });
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        System.out.print("\b" + "complete.");
    }
}
