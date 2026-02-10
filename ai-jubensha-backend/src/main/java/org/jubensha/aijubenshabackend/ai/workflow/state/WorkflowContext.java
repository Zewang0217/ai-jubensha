package org.jubensha.aijubenshabackend.ai.workflow.state;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流上下文
 *
 * @author zewan
 * @author luobo
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
    @Serial
    private static final long serialVersionUID = 1L;
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

    // ====== 玩家分配相关字段 ======
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 玩家分配结果
     */
    private List<Map<String, Object>> playerAssignments;
    /**
     * DM的ID
     */
    private Long dmId;

    // ====== 剧本相关字段 ======
    /**
     * Judge的ID
     */
    private Long judgeId;
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
     * 是否创建新剧本
     */
    private Boolean createNewScript;
    /**
     * 现有剧本ID
     */
    private Long existingScriptId;

    // ====== 游戏流程相关字段 ======
    /**
     * 场景列表
     */
    private List<org.jubensha.aijubenshabackend.models.entity.Scene> scenes;
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

    // ====== 玩家相关字段 ======
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 真人玩家数量
     */
    private Integer realPlayerCount;
    /**
     * AI玩家数量
     */
    private Integer aiPlayerCount;

    // ====== 执行状态和元数据字段 ======
    /**
     * 总玩家数量
     */
    private Integer totalPlayerCount;
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

    // ====== 讨论相关字段 ======
    /**
     * 当前讨论阶段
     */
    private String currentDiscussionPhase;
    /**
     * 讨论轮次
     */
    private Integer discussionRound;
    /**
     * 玩家答案
     */
    private Map<Long, String> playerAnswers;
    /**
     * 讨论状态
     */
    private Map<String, Object> discussionState;
    /**
     * 单聊邀请
     */
    private Map<Long, List<Long>> privateChatInvitations;
    /**
     * 单聊次数
     */
    private Map<Long, Integer> privateChatCounts;

    // ====== 搜证相关字段 ======
    /**
     * 默认搜证次数限制（每个玩家每轮可搜证次数）
     */
    public static final int DEFAULT_INVESTIGATION_LIMIT = 3;
    /**
     * 玩家搜证次数限制 key: playerId, value: 剩余次数
     */
    private Map<Long, Integer> playerInvestigationCounts;
    /**
     * 玩家搜证历史记录 key: playerId, value: 搜证历史列表
     */
    private Map<Long, List<Map<String, Object>>> playerInvestigationHistories;

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

    // ====== 搜证相关方法 ======

    /**
     * 初始化玩家搜证次数
     * 在游戏进入搜证阶段时调用，为每个玩家分配默认的搜证次数
     *
     * @param playerIds 玩家ID列表
     */
    public void initInvestigationCounts(List<Long> playerIds) {
        if (this.playerInvestigationCounts == null) {
            this.playerInvestigationCounts = new java.util.HashMap<>();
        }
        if (this.playerInvestigationHistories == null) {
            this.playerInvestigationHistories = new java.util.HashMap<>();
        }
        for (Long playerId : playerIds) {
            this.playerInvestigationCounts.put(playerId, DEFAULT_INVESTIGATION_LIMIT);
            this.playerInvestigationHistories.put(playerId, new java.util.ArrayList<>());
        }
    }

    /**
     * 获取玩家的剩余搜证次数
     *
     * @param playerId 玩家ID
     * @return 剩余次数，如果未找到则返回0
     */
    public int getRemainingInvestigationCount(Long playerId) {
        if (this.playerInvestigationCounts == null) {
            return 0;
        }
        return this.playerInvestigationCounts.getOrDefault(playerId, 0);
    }

    /**
     * 检查玩家是否还有搜证次数
     *
     * @param playerId 玩家ID
     * @return true 如果还有剩余次数，false 否则
     */
    public boolean hasInvestigationChance(Long playerId) {
        return getRemainingInvestigationCount(playerId) > 0;
    }

    /**
     * 消耗一次搜证次数
     * 如果玩家还有剩余次数，则扣减一次并返回true；否则返回false
     *
     * @param playerId 玩家ID
     * @return true 如果成功扣减次数，false 如果没有剩余次数
     */
    public boolean consumeInvestigationChance(Long playerId) {
        if (!hasInvestigationChance(playerId)) {
            return false;
        }
        int remaining = this.playerInvestigationCounts.get(playerId);
        this.playerInvestigationCounts.put(playerId, remaining - 1);
        return true;
    }

    /**
     * 记录玩家的搜证历史
     *
     * @param playerId 玩家ID
     * @param sceneId  场景ID
     * @param clueId   线索ID
     * @param clueName 线索名称
     */
    public void recordInvestigationHistory(Long playerId, Long sceneId, Long clueId, String clueName) {
        if (this.playerInvestigationHistories == null) {
            this.playerInvestigationHistories = new java.util.HashMap<>();
        }
        List<Map<String, Object>> history = this.playerInvestigationHistories.computeIfAbsent(playerId, k -> new java.util.ArrayList<>());
        Map<String, Object> record = new java.util.HashMap<>();
        record.put("sceneId", sceneId);
        record.put("clueId", clueId);
        record.put("clueName", clueName);
        record.put("investigateTime", LocalDateTime.now());
        history.add(record);
    }

    /**
     * 获取玩家的搜证历史
     *
     * @param playerId 玩家ID
     * @return 搜证历史列表
     */
    public List<Map<String, Object>> getInvestigationHistory(Long playerId) {
        if (this.playerInvestigationHistories == null) {
            return new java.util.ArrayList<>();
        }
        return this.playerInvestigationHistories.getOrDefault(playerId, new java.util.ArrayList<>());
    }

    /**
     * 获取所有玩家的搜证次数信息
     *
     * @return 玩家ID到剩余次数的映射
     */
    public Map<Long, Integer> getAllInvestigationCounts() {
        if (this.playerInvestigationCounts == null) {
            return new java.util.HashMap<>();
        }
        return new java.util.HashMap<>(this.playerInvestigationCounts);
    }
}
