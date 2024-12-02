package org.example;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class TaskExecutorBlockingQueueImpl implements TaskExecutor {
    Semaphore globaSemaphore;
    Map<UUID, Semaphore> semaphoreMap;
    ExecutorService executorService;
    Object lock = new ReentrantLock();
    BlockingQueue<Runnable> taskQueue;
    Boolean isShutDown = false;


    TaskExecutorBlockingQueueImpl(int maxConcurrentTask) {
        globaSemaphore = new Semaphore(maxConcurrentTask);
        executorService = Executors.newFixedThreadPool(maxConcurrentTask);
        semaphoreMap = new ConcurrentHashMap<>();
        taskQueue = new LinkedBlockingQueue<>();
    }

    public void startProcessor() {
        Thread taskProcessor = new Thread(() -> {
            while (!isShutDown) {
                if (!taskQueue.isEmpty()) {
                    try {
                        executorService.submit(taskQueue.take());//wait till a new task is available
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break; // Exit the loop on interruption
                    }
                }
            }
        });
        taskProcessor.setDaemon(true);
        taskProcessor.start();
    }

    @Override
    public <T> Future<T> submitTask(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Runnable runnableTask = (() -> {
            try {
                globaSemaphore.acquire();
                Semaphore groupSemaphore = semaphoreMap.computeIfAbsent(task.taskGroup().groupID(), id -> new Semaphore(1));
                groupSemaphore.acquire();
                try {
                    T result = task.taskAction().call();
                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                } finally {
                    groupSemaphore.release();
                }
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            } finally {
                globaSemaphore.release();
            }

        });

        taskQueue.offer(runnableTask);
        return future;
    }


    @Override
    public void shutdown() {
        isShutDown = true;
        executorService.shutdown();
    }
}

