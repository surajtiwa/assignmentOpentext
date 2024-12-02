Implement a task executor service according to the following specification.
The entry point for the service is Task Executor interface. The interface is defined bellow
including its dependencies.
The service is required to implement the following behaviors:
1. Tasks can be submitted concurrently. Task submission should not block the submitter.
2. Tasks are executed asynchronously and concurrently. Maximum allowed concurrency
may be restricted.
3. Once task is finished, its results can be retrieved from the Future received during task
submission.
4. The order of tasks must be preserved.
o The first task submitted must be the first task started.
o The task result should be available as soon as possible after the task completes.
5. Tasks sharing the same TaskGroup must not run concurrently.
Additional implementation requirements:
1. The implementation must run on OpenJDK 17.
2. No third-party libraries can be used.
3. The provided interfaces and classes must not be modified.
Please, write down any assumptions you made.


For the following problem Assumptions which i made were
Every Task Group should be exectued independently and all task submmited in that group should be executed in sequential order for example if we are executing a task from taskgroup A then we should not execute a task from same task group.
to Achieve this I used mutex by having two seamphore one at group level and other at concurrency level.
The group semaphore ensures no two tasks from the same group run concurrently.

So now for Task submmision I went with the executor service instead of using a blocking queue and synchronized block. The main reason for me using executor service as it encapsulates  all the task submission and execution order which we had to handle manually while going with Blocking queue and code is more readable with executor service.
