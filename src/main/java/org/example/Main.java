package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Hello world!
 */
public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TaskExecutor taskExecutor = new TaskExecutorSemaphoreImpl(4);
        ArrayList<TaskGroup> taskGroupArrayList = new ArrayList<>();
        taskGroupArrayList.add(new TaskGroup(UUID.randomUUID(),1));
        taskGroupArrayList.add(new TaskGroup(UUID.randomUUID(),2));
        taskGroupArrayList.add(new TaskGroup(UUID.randomUUID(),3));

     //   scheduleTask(taskExecutor, taskGroupArrayList);

        //   startTaskExecutor(taskExecutor);
        TaskExecutorBlockingQueueImpl taskExecutorBlockingQueue=new TaskExecutorBlockingQueueImpl(4);
        scheduleTaskWithBlockingQueue(taskExecutorBlockingQueue,taskGroupArrayList);
    }


    public static  void scheduleTaskWithBlockingQueue(TaskExecutorBlockingQueueImpl taskExecutor, ArrayList<TaskGroup> taskGroupArrayList) throws ExecutionException, InterruptedException {
        taskExecutor.startProcessor();
        List<Future<String>> futures = new ArrayList<>();
        Random random = new Random();

        for (TaskGroup taskGroup : taskGroupArrayList) {
            int nofTask = 10;// random.nextInt();
            for (int i = 1; i <= nofTask; i++) {
                int taskID = i;
                Task<String> task = new Task<>(UUID.randomUUID(), taskGroup, TaskType.READ, () -> {
                    Thread.sleep(random.nextInt(1000));
                    return "Task Group :" + taskGroup.taskID() + " task id " + taskID + " completed";
                });
                futures.add(taskExecutor.submitTask(task));
            }

        }
        for (Future<String> result : futures) {
            System.out.println(result.get());
        }
        taskExecutor.shutdown();

    }

    public static void scheduleTask(TaskExecutor taskExecutor, ArrayList<TaskGroup> taskGroupArrayList) throws ExecutionException, InterruptedException {

        List<Future<String>> futures = new ArrayList<>();
        Random random = new Random();
        for (TaskGroup taskGroup : taskGroupArrayList) {
            int nofTask = random.nextInt(100);
            for (int i = 1; i <= nofTask; i++) {
                int taskID = i;
                Task<String> task = new Task<>(UUID.randomUUID(), taskGroup, TaskType.READ, () -> {
                    Thread.sleep(random.nextInt(1000));
                    return "Task Group :" + taskGroup.taskID() + " task id " + taskID + " completed";
                });
                futures.add(taskExecutor.submitTask(task));
            }
        }
        for (Future<String> result : futures) {
            System.out.println(result.get());
        }
        taskExecutor.shutdown();

    }
}
