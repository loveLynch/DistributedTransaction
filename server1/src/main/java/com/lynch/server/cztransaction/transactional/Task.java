package com.lynch.server.cztransaction.transactional;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lynch on 2019-10-20. <br>
 **/
public class Task {

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    /**
     * 任务等待
     */
    public void waitTask() {
        lock.lock();
        try {
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 任务唤醒
     */
    public void singalTask() {
        lock.lock();
        condition.signal();
        lock.unlock();
    }
}
