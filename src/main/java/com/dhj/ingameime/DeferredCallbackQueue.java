package com.dhj.ingameime;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

/**
 * Holds native callback work until the client thread can execute it outside JNI.
 */
public final class DeferredCallbackQueue {
    private final Queue<Runnable> tasks = new ArrayDeque<>();

    /** Appends callback work in arrival order. */
    public synchronized void enqueue(Runnable task) {
        tasks.add(Objects.requireNonNull(task, "task"));
    }

    /** Executes queued work in FIFO order, including work appended by a running task. */
    public void drain() {
        while (true) {
            Runnable task;
            synchronized (this) {
                task = tasks.poll();
            }
            if (task == null) {
                return;
            }
            task.run();
        }
    }

    /** Discards callback work owned by an input context that is no longer valid. */
    public synchronized void clear() {
        tasks.clear();
    }
}
