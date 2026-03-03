package org.jubensha.aijubenshabackend.ai.service;

import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流状态管理服务
 * 用于跟踪和管理工作流的执行状态
 */
@Service
public class WorkflowStatusService {

    /**
     * 工作流状态存储，使用ConcurrentHashMap保证线程安全
     * key: 游戏ID
     * value: 工作流状态信息
     */
    private final Map<Long, WorkflowStatus> workflowStatusMap = new ConcurrentHashMap<>();

    /**
     * 工作流状态枚举
     */
    public enum WorkflowState {
        PENDING,     // 待执行
        RUNNING,     // 执行中
        COMPLETED,   // 执行完成
        FAILED       // 执行失败
    }

    /**
     * 工作流状态信息
     */
    public static class WorkflowStatus {
        private final Long workflowId;
        private final Long gameId;
        private WorkflowState state;
        private String currentStep;
        private String errorMessage;
        private WorkflowContext workflowContext;

        public WorkflowStatus(Long workflowId, Long gameId) {
            this.workflowId = workflowId;
            this.gameId = gameId;
            this.state = WorkflowState.PENDING;
            this.currentStep = "初始化";
        }

        // Getters and Setters
        public Long getWorkflowId() {
            return workflowId;
        }

        public Long getGameId() {
            return gameId;
        }

        public WorkflowState getState() {
            return state;
        }

        public void setState(WorkflowState state) {
            this.state = state;
        }

        public String getCurrentStep() {
            return currentStep;
        }

        public void setCurrentStep(String currentStep) {
            this.currentStep = currentStep;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public WorkflowContext getWorkflowContext() {
            return workflowContext;
        }

        public void setWorkflowContext(WorkflowContext workflowContext) {
            this.workflowContext = workflowContext;
        }
    }

    /**
     * 生成唯一的工作流ID
     * @return 工作流ID
     */
    private Long generateWorkflowId() {
        return System.currentTimeMillis() + (long) (Math.random() * 1000);
    }

    /**
     * 创建工作流状态
     * @param gameId 游戏ID
     * @return 工作流状态
     */
    public WorkflowStatus createWorkflowStatus(Long gameId) {
        Long workflowId = generateWorkflowId();
        WorkflowStatus status = new WorkflowStatus(workflowId, gameId);
        workflowStatusMap.put(gameId, status);
        return status;
    }

    /**
     * 获取工作流状态
     * @param gameId 游戏ID
     * @return 工作流状态，如果不存在返回null
     */
    public WorkflowStatus getWorkflowStatus(Long gameId) {
        return workflowStatusMap.get(gameId);
    }

    /**
     * 更新工作流状态
     * @param gameId 游戏ID
     * @param status 工作流状态
     */
    public void updateWorkflowStatus(Long gameId, WorkflowStatus status) {
        workflowStatusMap.put(gameId, status);
    }

    /**
     * 更新工作流状态为运行中
     * @param gameId 游戏ID
     * @param currentStep 当前步骤
     */
    public void updateWorkflowRunning(Long gameId, String currentStep) {
        WorkflowStatus status = workflowStatusMap.get(gameId);
        if (status != null) {
            status.setState(WorkflowState.RUNNING);
            status.setCurrentStep(currentStep);
            workflowStatusMap.put(gameId, status);
        }
    }

    /**
     * 更新工作流状态为完成
     * @param gameId 游戏ID
     * @param workflowContext 工作流上下文
     */
    public void updateWorkflowCompleted(Long gameId, WorkflowContext workflowContext) {
        WorkflowStatus status = workflowStatusMap.get(gameId);
        if (status != null) {
            status.setState(WorkflowState.COMPLETED);
            status.setWorkflowContext(workflowContext);
            workflowStatusMap.put(gameId, status);
        }
    }

    /**
     * 更新工作流状态为失败
     * @param gameId 游戏ID
     * @param errorMessage 错误信息
     */
    public void updateWorkflowFailed(Long gameId, String errorMessage) {
        WorkflowStatus status = workflowStatusMap.get(gameId);
        if (status != null) {
            status.setState(WorkflowState.FAILED);
            status.setErrorMessage(errorMessage);
            workflowStatusMap.put(gameId, status);
        }
    }

    /**
     * 移除工作流状态
     * @param gameId 游戏ID
     */
    public void removeWorkflowStatus(Long gameId) {
        workflowStatusMap.remove(gameId);
    }
}