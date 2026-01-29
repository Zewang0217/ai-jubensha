package org.jubensha.aijubenshabackend.service.task;

import java.util.Optional;
import java.util.concurrent.Future;

public interface TaskService {
    /**
     * 提交剧本生成任务
     */
    <T> String submitScriptGenerationTask(String scriptName, String description, Integer playerCount, String difficulty, String extraRequirements);

    /**
     * 获取任务状态
     */
    Optional<TaskInfo> getTaskStatus(String taskId);

    /**
     * 清理过期任务
     */
    void cleanupExpiredTasks();
}
