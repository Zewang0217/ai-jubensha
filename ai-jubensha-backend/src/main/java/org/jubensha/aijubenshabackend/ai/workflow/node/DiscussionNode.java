package org.jubensha.aijubenshabackend.ai.workflow.node;


import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.service.player.PlayerService;

import java.time.LocalDateTime;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class DiscussionNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.debug("DiscussionNode: {}", context);
            log.info("执行节点：讨论环节");

            try {
                // 获取必要的服务
                AIService aiService = SpringContextUtil.getBean(AIService.class);
                PlayerService playerService = SpringContextUtil.getBean(PlayerService.class);

                // 获取游戏信息
                Long gameId = context.getGameId();
                Long dmId = context.getDmId();
                Long judgeId = context.getJudgeId();
                List<Long> playerIds = getPlayerIds(context);

                log.info("开始讨论环节，游戏ID: {}, DM ID: {}, Judge ID: {}", gameId, dmId, judgeId);
                log.info("玩家数量: {}", playerIds.size());

                // 启动讨论服务
                startDiscussionService(gameId, playerIds, dmId, judgeId);

                // 更新工作流上下文
                context.setCurrentStep("讨论环节");
                context.setModelOutput("讨论环节已启动");
                context.setSuccess(true);
                context.setStartTime(LocalDateTime.now());

            } catch (Exception e) {
                log.error("讨论环节启动失败: {}", e.getMessage(), e);
                context.setErrorMessage("讨论环节启动失败: " + e.getMessage());
                context.setSuccess(false);
            }

            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 获取玩家ID列表
     */
    private static List<Long> getPlayerIds(WorkflowContext context) {
        // 这里需要从上下文中获取玩家ID列表
        // 暂时返回空列表，后续需要根据实际情况实现
        return List.of();
    }

    /**
     * 启动讨论服务
     */
    private static void startDiscussionService(Long gameId, List<Long> playerIds, Long dmId, Long judgeId) {
        // 这里需要初始化讨论服务
        // 后续将由DiscussionService实现
        log.info("启动讨论服务，游戏ID: {}, 玩家数量: {}", gameId, playerIds.size());
    }
}
