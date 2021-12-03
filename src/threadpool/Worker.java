package threadpool;

import java.util.LinkedList;

public class Worker extends Thread implements Runnable {
    private final LinkedList<Task> queue = new LinkedList<>();
    private boolean stop = false;
    private OnWork work;
    private int id;

    public Worker(int id, OnWork work) {
        this.id = id;
        this.work = work;
    }

    public int id() {
        return id;
    }

    public void addTask(Task task) {
        boolean wake = size() == 0;
        if (task == null) return;
        add(task);
        if (wake)
            synchronized (queue) {
                queue.notify();
            }
    }

    public void down() {
        stop = true;
    }

    public synchronized int size() {
        return queue.size();
    }

    private synchronized void add(Task item) {
        queue.add(item);
    }

    private synchronized Task poll() {
        return queue.poll();
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Task poll = poll();
                if (poll == null) {
                    synchronized (queue) {
                        work.onSleep(id);
                        queue.wait();
                    }
                    poll = poll();
                }
                poll.callback();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            work.onFinish(id);
        }
    }

    public interface OnWork {
        public void onFinish(int id);

        public void onSleep(int id);
    }
}
