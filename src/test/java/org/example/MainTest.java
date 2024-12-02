package org.example;


import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;


public class MainTest{


    @Test
    public void testBasicTaskExecution() throws Exception {
        TaskExecutorSemaphoreImpl executorService = new TaskExecutorSemaphoreImpl(2);

        UUID groupId = UUID.randomUUID();
        TaskGroup group = new TaskGroup(groupId);

        Task<String> task1 = new Task<>(
                UUID.randomUUID(),
                group,
                TaskType.READ,
                () -> "Task 1 Completed"
        );

        Task<String> task2 = new Task<>(
                UUID.randomUUID(),
                group,
                TaskType.READ,
                () -> "Task 2 Completed"
        );

        Future<String> future1 = executorService.submitTask(task1);
        Future<String> future2 = executorService.submitTask(task2);

       assertEquals("Task 1 Completed", future1.get());
        assertEquals("Task 2 Completed", future2.get());

        executorService.shutdown();
    }


    @Test
    public void testGlobalConcurrencyLimit() throws Exception {
        TaskExecutorSemaphoreImpl executorService = new TaskExecutorSemaphoreImpl(2);

        List<Future<Void>> futures = new ArrayList<>();
        AtomicInteger concurrentTasks = new AtomicInteger(0);
        AtomicInteger maxConcurrentTasks = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            UUID groupId = UUID.randomUUID();
            TaskGroup group = new TaskGroup(groupId);

            Task<Void> task = new Task<>(
                    UUID.randomUUID(),
                    group,
                    TaskType.WRITE,
                    () -> {
                        int current = concurrentTasks.incrementAndGet();
                        maxConcurrentTasks.updateAndGet(max -> Math.max(max, current));

                        Thread.sleep(1000); // Simulate work

                        concurrentTasks.decrementAndGet();
                        return null;
                    }
            );

            futures.add(executorService.submitTask(task));
        }

        for (Future<Void> future : futures) {
            future.get();
        }

        assertEquals(2, maxConcurrentTasks.get()); // Ensure concurrency limit is respected

        executorService.shutdown();
    }

    @Test
    public void testTaskSubmissionOrder() throws Exception {
        TaskExecutorSemaphoreImpl executorService = new TaskExecutorSemaphoreImpl(3);

        UUID groupId = UUID.randomUUID();
        TaskGroup group = new TaskGroup(groupId);

        List<String> results = Collections.synchronizedList(new ArrayList<>());

        Task<Void> task1 = new Task<>(
                UUID.randomUUID(),
                group,
                TaskType.WRITE,
                () -> {
                    results.add("Task 1");
                    return null;
                }
        );

        Task<Void> task2 = new Task<>(
                UUID.randomUUID(),
                group,
                TaskType.WRITE,
                () -> {
                    results.add("Task 2");
                    return null;
                }
        );

        Task<Void> task3 = new Task<>(
                UUID.randomUUID(),
                group,
                TaskType.WRITE,
                () -> {
                    results.add("Task 3");
                    return null;
                }
        );

        executorService.submitTask(task1).get();
        executorService.submitTask(task2).get();
        executorService.submitTask(task3).get();

        assertEquals(Arrays.asList("Task 1", "Task 2", "Task 3"), results);

        executorService.shutdown();
    }


}
