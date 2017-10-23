package cluster;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public class ThreadCounter {
    private static ThreadCounter threadCounter = new ThreadCounter();
    private volatile int counter = 0;

    private ThreadCounter() {
    }

    public static ThreadCounter getInstance() {
        return threadCounter;
    }

    public void increment() {
        counter += 1;
    }

    public void decrement() {
        counter -= 1;
    }

    public int getCounter() {
        return counter;
    }
}
