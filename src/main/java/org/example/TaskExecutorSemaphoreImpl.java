package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class TaskExecutorSemaphoreImpl implements TaskExecutor {
    Semaphore globaSemaphore;
    Map<UUID, Semaphore> semaphoreMap;
    ExecutorService executorService;

    TaskExecutorSemaphoreImpl(int maxConcurrentTask) {
        globaSemaphore = new Semaphore(maxConcurrentTask);
        executorService = Executors.newFixedThreadPool(maxConcurrentTask);
        semaphoreMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T> Future<T> submitTask(Task<T> task) {
        CompletableFuture future = new CompletableFuture();
        executorService.submit(() -> {
            try {
                globaSemaphore.acquire();
                Semaphore groupSemaphore = semaphoreMap.computeIfAbsent(task.taskUUID(), id -> new Semaphore(1));
                groupSemaphore.acquire();
                try {
                    T result=task.taskAction().call();
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }finally {
                    groupSemaphore.release();
                }
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            } finally {
                globaSemaphore.release();
            }


        });

        return future;

    }


    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}
