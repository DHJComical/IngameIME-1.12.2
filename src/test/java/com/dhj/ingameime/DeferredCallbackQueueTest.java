package com.dhj.ingameime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeferredCallbackQueueTest {
    @Test
    void drainsTasksInFifoOrder() {
        DeferredCallbackQueue queue = new DeferredCallbackQueue();
        List<Integer> executionOrder = new ArrayList<>();

        queue.enqueue(() -> executionOrder.add(1));
        queue.enqueue(() -> executionOrder.add(2));
        queue.enqueue(() -> executionOrder.add(3));

        queue.drain();

        assertEquals(Arrays.asList(1, 2, 3), executionOrder);
    }

    @Test
    void drainsTasksEnqueuedByRunningTaskAfterExistingWork() {
        DeferredCallbackQueue queue = new DeferredCallbackQueue();
        List<Integer> executionOrder = new ArrayList<>();

        queue.enqueue(() -> {
            executionOrder.add(1);
            queue.enqueue(() -> executionOrder.add(3));
        });
        queue.enqueue(() -> executionOrder.add(2));

        queue.drain();

        assertEquals(Arrays.asList(1, 2, 3), executionOrder);
    }

    @Test
    void clearDiscardsQueuedTasks() {
        DeferredCallbackQueue queue = new DeferredCallbackQueue();
        List<Integer> executionOrder = new ArrayList<>();
        queue.enqueue(() -> executionOrder.add(1));

        queue.clear();
        queue.drain();

        assertTrue(executionOrder.isEmpty());
    }
}
