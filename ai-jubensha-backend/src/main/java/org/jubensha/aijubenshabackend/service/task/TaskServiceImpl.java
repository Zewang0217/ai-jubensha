package org.jubensha.aijubenshabackend.service.task;

import org.jubensha.aijubenshabackend.models.entity.Script;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final ScriptService scriptService;
    private final ExecutorService executorService;
    private final Map<String, TaskInfo> taskMap;

    @Autowired
    public TaskServiceImpl(ScriptService scriptService) {
        this.scriptService = scriptService;
        // 创建线程池，核心线程数为5，最大线程数为10
        this.executorService = new ThreadPoolExecutor(
                5,
                10,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "script-generation-thread-" + counter.incrementAndGet());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.taskMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T> String submitScriptGenerationTask(String scriptName, String description, Integer playerCount, String difficulty, String extraRequirements) {
        String taskId = UUID.randomUUID().toString();
        TaskInfo taskInfo = new TaskInfo(taskId, TaskStatus.PENDING);
        taskMap.put(taskId, taskInfo);

        executorService.submit(() -> {
            try {
                taskInfo.setStatus(TaskStatus.PROCESSING);
                logger.info("Starting script generation task: {}", taskId);

                // 解析难度级别
                DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());

                // 调用剧本生成服务
                Script script = scriptService.generateScript(
                        scriptName,
                        description,
                        playerCount,
                        difficultyLevel,
                        extraRequirements
                );

                taskInfo.setStatus(TaskStatus.COMPLETED);
                taskInfo.setResult(script);
                logger.info("Script generation task completed: {}", taskId);
            } catch (Exception e) {
                taskInfo.setStatus(TaskStatus.FAILED);
                taskInfo.setErrorMessage(e.getMessage());
                logger.error("Script generation task failed: {}", taskId, e);
            }
        });

        return taskId;
    }

    @Override
    public Optional<TaskInfo> getTaskStatus(String taskId) {
        return Optional.ofNullable(taskMap.get(taskId));
    }

    @Override
    public void cleanupExpiredTasks() {
        // 清理已完成或失败超过1小时的任务
        long currentTime = System.currentTimeMillis();
        taskMap.entrySet().removeIf(entry -> {
            TaskInfo taskInfo = entry.getValue();
            return (taskInfo.getStatus() == TaskStatus.COMPLETED || taskInfo.getStatus() == TaskStatus.FAILED);
        });
        logger.info("Cleaned up expired tasks, remaining tasks: {}", taskMap.size());
    }

    // 关闭线程池
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
