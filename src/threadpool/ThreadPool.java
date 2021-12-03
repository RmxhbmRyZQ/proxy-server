package threadpool;

import callback.OnSelect;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;

/**
 * 线程池，还需要处理同步问题
 */
public class ThreadPool implements Worker.OnWork {
    private final Worker[] workers;
    private final LinkedList<Worker> freeWorker = new LinkedList<>();
    private final int[] taskNumber;

    public ThreadPool() {
        int core = Runtime.getRuntime().availableProcessors();
        workers = new Worker[core];
        taskNumber = new int[core];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker(i, this);
            freeWorker.add(workers[i]);
            workers[i].start();
        }
    }

    public void execute(OnSelect select, SelectionKey key) {
        if (select == null || key == null) return;
        Worker worker;
        if (freeWorker.size() > 0) {  // 有空闲线程
            worker = freeWorker.poll();
        } else {
            int id = 0, min = 0xffff;
            for (int i = 0; i < taskNumber.length; i++) {  // 使用任务最少得线程
                if (taskNumber[i] < min) {
                    id = i;
                    min = taskNumber[i];
                }
            }
            worker = workers[id];
        }
        taskNumber[worker.id()]++;
        worker.addTask(new Task(select, key));
    }

    public void shutdown() {
        for (Worker worker : workers) {
            worker.down();
        }
    }

    @Override
    public void onFinish(int id) {
        taskNumber[id]--;
    }

    @Override
    public void onSleep(int id) {
        freeWorker.add(workers[id]);
    }
}
