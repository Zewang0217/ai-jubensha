package org.jubensha.aijubenshabackend.ai.workflow.node;


import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.DiscussionService;
import org.jubensha.aijubenshabackend.ai.service.DiscussionServiceImpl;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.service.player.PlayerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

                // 启动讨论服务并等待完成
                boolean discussionCompleted = startDiscussionServiceAndWait(gameId, playerIds, dmId, judgeId);

                // 更新工作流上下文
                context.setCurrentStep("讨论环节");
                context.setModelOutput(discussionCompleted ? "讨论环节已完成" : "讨论环节已启动");
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
        List<Long> playerIds = new ArrayList<>();
        
        // 从playerAssignments中提取玩家ID
        List<Map<String, Object>> playerAssignments = context.getPlayerAssignments();
        if (playerAssignments != null && !playerAssignments.isEmpty()) {
            for (Map<String, Object> assignment : playerAssignments) {
                Object playerIdObj = assignment.get("playerId");
                if (playerIdObj instanceof Long) {
                    playerIds.add((Long) playerIdObj);
                } else if (playerIdObj instanceof Integer) {
                    playerIds.add(((Integer) playerIdObj).longValue());
                }
            }
        }
        
        return playerIds;
    }

    /**
     * 启动讨论服务并等待完成
     */
    private static boolean startDiscussionServiceAndWait(Long gameId, List<Long> playerIds, Long dmId, Long judgeId) {
        // 获取DiscussionService实例
        DiscussionService discussionService = SpringContextUtil.getBean(DiscussionService.class);
        
        // 检查是否为DiscussionServiceImpl类型
        if (discussionService instanceof DiscussionServiceImpl) {
            DiscussionServiceImpl discussionServiceImpl = (DiscussionServiceImpl) discussionService;
            
            // 设置讨论完成回调
            final boolean[] completed = {false};
            final Object lock = new Object();
            
            discussionServiceImpl.setCompletionCallback(discussionState -> {
                synchronized (lock) {
                    completed[0] = true;
                    lock.notify();
                }
                log.info("讨论已完成，通知工作流继续执行");
            });
            
            // 启动讨论服务
            discussionServiceImpl.startDiscussion(gameId, playerIds, dmId, judgeId);
            log.info("启动讨论服务，游戏ID: {}, 玩家数量: {}", gameId, playerIds.size());
            
            // 等待讨论完成，最多等待2小时
            synchronized (lock) {
                try {
                    log.info("等待讨论环节完成...");
                    // 等待最多2小时
                    lock.wait(2 * 60 * 60 * 1000);
                } catch (InterruptedException e) {
                    log.error("等待讨论完成时被中断: {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
            
            log.info("讨论环节处理完成，完成状态: {}", completed[0]);
            return completed[0];
        } else {
            // 非DiscussionServiceImpl类型，直接启动
            discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);
            log.info("启动讨论服务，游戏ID: {}, 玩家数量: {}", gameId, playerIds.size());
            return false;
        }
    }
    
    /**
     * 启动讨论服务（旧方法，保留用于兼容性）
     */
    private static void startDiscussionService(Long gameId, List<Long> playerIds, Long dmId, Long judgeId) {
        // 获取DiscussionService实例
        DiscussionService discussionService = SpringContextUtil.getBean(DiscussionService.class);
        
        // 启动讨论服务
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);
        
        log.info("启动讨论服务，游戏ID: {}, 玩家数量: {}", gameId, playerIds.size());
    }
}
