package org.jubensha.aijubenshabackend.ai.workflow.state;


import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

/**
 * 工作流上下文
 *
 * @author zewan
 * @version 1.0
 * @date 2026-01-30 16:25
 * @since 2026
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    /**
     * WorkflowContext 在 MessageState 中的存储key
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 当前执行步骤
     */
    private String currentStep;

    /**
     * 用户输入的提示词
     */
    private String originalPrompt;

    /**
     * 模型输出结果
     */
    private String modelOutput;

    /**
     * 剧本id
     */
    private Long scriptId;

    /**
     * 错误信息
     */
    private String errorMessage;

    // ====== 玩家分配相关字段 ======

    /**
     * 玩家分配结果
     */
    private List<Map<String, Object>> playerAssignments;

    /**
     * DM的ID
     */
    private Long dmId;

    /**
     * Judge的ID
     */
    private Long judgeId;

    // ====== 剧本相关字段 ======

    /**
     * 剧本名称
     */
    private String scriptName;

    /**
     * 剧本类型
     */
    private String scriptType;

    /**
     * 剧本难度
     */
    private String scriptDifficulty;

    /**
     * 角色数量
     */
    private Integer characterCount;

    /**
     * 场景列表
     */
    private List<org.jubensha.aijubenshabackend.models.entity.Scene> scenes;

    // ====== 游戏流程相关字段 ======

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 游戏状态
     */
    private String gameStatus;

    /**
     * 当前游戏阶段
     */
    private String currentPhase;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    // ====== 玩家相关字段 ======

    /**
     * 真人玩家数量
     */
    private Integer realPlayerCount;

    /**
     * AI玩家数量
     */
    private Integer aiPlayerCount;

    /**
     * 总玩家数量
     */
    private Integer totalPlayerCount;

    // ====== 执行状态和元数据字段 ======

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 执行是否成功
     */
    private Boolean success;

    @Serial
    private static final long serialVersionUID = 1L;

    // ====== 上下文操作方法 ======

    /**
     * 从 MessageState 中获取 WorkflowContext
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 将 WorkflowContext 存储到 MessageState 中
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }
}
