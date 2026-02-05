package org.jubensha.aijubenshabackend.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 讨论服务实现
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 15:40
 * @since 2026
 */
@Slf4j
@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Resource
    private AIService aiService;

    @Resource
    private MessageQueueService messageQueueService;

    @Resource
    private TimerService timerService;

    // 讨论状态
    private final Map<String, Object> discussionState = new ConcurrentHashMap<>();

    // 游戏信息
    private Long gameId;
    private List<Long> playerIds;
    private Long dmId;
    private Long judgeId;

    // 讨论阶段
    private String currentPhase;

    // 玩家答案
    private final Map<Long, String> playerAnswers = new ConcurrentHashMap<>();

    // 单聊邀请
    private final Map<Long, List<Long>> privateChatInvitations = new ConcurrentHashMap<>();

    // 单聊次数
    private final Map<Long, Integer> privateChatCounts = new ConcurrentHashMap<>();

    // 讨论轮次
    private int discussionRound = 1;

    @Override
    public void startDiscussion(Long gameId, List<Long> playerIds, Long dmId, Long judgeId) {
        log.info("开始讨论，游戏ID: {}, 玩家数量: {}, DM ID: {}, Judge ID: {}", gameId, playerIds.size(), dmId, judgeId);

        this.gameId = gameId;
        this.playerIds = playerIds;
        this.dmId = dmId;
        this.judgeId = judgeId;

        // 初始化讨论状态
        discussionState.clear();
        discussionState.put("gameId", gameId);
        discussionState.put("playerIds", playerIds);
        discussionState.put("dmId", dmId);
        discussionState.put("judgeId", judgeId);
        discussionState.put("discussionRound", discussionRound);
        discussionState.put("startTime", LocalDateTime.now());
        discussionState.put("currentPhase", "INITIALIZED");

        // 初始化单聊次数
        for (Long playerId : playerIds) {
            privateChatCounts.put(playerId, 0);
            privateChatInvitations.put(playerId, new ArrayList<>());
        }

        // 通知DM开始讨论
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String discussionInfo = "游戏ID: " + gameId + ", 玩家数量: " + playerIds.size() + ", 讨论轮次: " + discussionRound;
            String response = dmAgent.startDiscussion(discussionInfo);
            log.info("DM响应: {}", response);
        }

        // 通知所有玩家讨论开始
        for (Long playerId : playerIds) {
            PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
            if (playerAgent != null) {
                playerAgent.discuss("讨论开始，请准备你的陈述");
            }
        }

        // 开始陈述阶段
        startStatementPhase();
    }

    @Override
    public void startStatementPhase() {
        log.info("开始陈述阶段");
        currentPhase = "STATEMENT";
        discussionState.put("currentPhase", currentPhase);
        discussionState.put("phaseStartTime", LocalDateTime.now());

        // 通知DM开始陈述阶段
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始陈述阶段，每位玩家有5分钟时间");
            log.info("DM响应: {}", response);
        }

        // 启动陈述阶段计时器（5分钟/人）
        timerService.startTimer("STATEMENT", 300L, this::startFreeDiscussionPhase);
    }

    @Override
    public void startFreeDiscussionPhase() {
        log.info("开始自由讨论阶段");
        currentPhase = "FREE_DISCUSSION";
        discussionState.put("currentPhase", currentPhase);
        discussionState.put("phaseStartTime", LocalDateTime.now());

        // 通知DM开始自由讨论阶段
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始自由讨论阶段，大家可以畅所欲言");
            log.info("DM响应: {}", response);
        }

        // 启动自由讨论阶段计时器（默认30分钟）
        timerService.startTimer("FREE_DISCUSSION", 1800L, this::startPrivateChatPhase);
    }

    @Override
    public void startPrivateChatPhase() {
        log.info("开始单聊阶段");
        currentPhase = "PRIVATE_CHAT";
        discussionState.put("currentPhase", currentPhase);
        discussionState.put("phaseStartTime", LocalDateTime.now());

        // 通知DM开始单聊阶段
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始单聊阶段，每位玩家有2次单聊机会，每次3分钟");
            log.info("DM响应: {}", response);
        }

        // 启动单聊阶段计时器（默认20分钟）
        timerService.startTimer("PRIVATE_CHAT", 1200L, this::startAnswerPhase);
    }

    @Override
    public void startAnswerPhase() {
        log.info("开始答题阶段");
        currentPhase = "ANSWER";
        discussionState.put("currentPhase", currentPhase);
        discussionState.put("phaseStartTime", LocalDateTime.now());

        // 通知DM开始答题阶段
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始答题阶段，请每位玩家给出你的答案");
            log.info("DM响应: {}", response);
        }

        // 启动答题阶段计时器（默认10分钟）
        timerService.startTimer("ANSWER", 600L, this::endDiscussionPhase);
    }

    @Override
    public void sendPrivateChatInvitation(Long senderId, Long receiverId) {
        log.info("发送单聊邀请，发送者: {}, 接收者: {}", senderId, receiverId);

        // 检查单聊次数
        Integer count = privateChatCounts.get(senderId);
        if (count == null || count >= 2) {
            log.warn("玩家 {} 单聊次数已达上限", senderId);
            return;
        }

        // 检查接收者是否存在
        if (!playerIds.contains(receiverId)) {
            log.warn("接收者 {} 不存在", receiverId);
            return;
        }

        // 发送单聊邀请
        messageQueueService.sendPrivateChatMessage("单聊邀请", senderId, receiverId);

        // 记录单聊邀请
        privateChatInvitations.get(senderId).add(receiverId);
        privateChatCounts.put(senderId, count + 1);

        // 启动单聊计时器（3分钟）
        timerService.startTimer("PRIVATE_CHAT_" + senderId + "_" + receiverId, 180L, () -> {
            log.info("单聊结束，发送者: {}, 接收者: {}", senderId, receiverId);
        });
    }

    @Override
    public void submitAnswer(Long playerId, String answer) {
        log.info("玩家 {} 提交答案", playerId);
        playerAnswers.put(playerId, answer);

        // 通知DM有玩家提交答案
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            messageQueueService.sendAnswerMessage(answer, playerId, dmId);
        }
    }

    @Override
    public Map<String, Object> endDiscussion() {
        log.info("结束讨论");
        discussionState.put("endTime", LocalDateTime.now());
        discussionState.put("playerAnswers", playerAnswers);

        // 通知DM评分
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            List<Map<String, Object>> answers = new ArrayList<>();
            for (Map.Entry<Long, String> entry : playerAnswers.entrySet()) {
                Map<String, Object> answerMap = new HashMap<>();
                answerMap.put("playerId", entry.getKey());
                answerMap.put("answer", entry.getValue());
                answers.add(answerMap);
            }
            String response = dmAgent.scoreAnswers(answers);
            log.info("DM评分响应: {}", response);
            discussionState.put("scoreResponse", response);
        }

        // 通知Judge总结讨论
        JudgeAgent judgeAgent = aiService.getJudgeAgent(judgeId);
        if (judgeAgent != null) {
            String discussionContent = "讨论内容摘要...";
            String summary = judgeAgent.summarizeDiscussion(discussionContent);
            log.info("Judge总结: {}", summary);
            discussionState.put("judgeSummary", summary);
        }

        return discussionState;
    }

    @Override
    public Map<String, Object> getDiscussionState() {
        return discussionState;
    }

    @Override
    public void sendDiscussionMessage(Long playerId, String message) {
        log.info("玩家 {} 发送讨论消息: {}", playerId, message);

        // 发送消息到所有玩家
        messageQueueService.sendDiscussionMessage(message, playerIds);

        // 通知Judge监控讨论
        JudgeAgent judgeAgent = aiService.getJudgeAgent(judgeId);
        if (judgeAgent != null) {
            boolean valid = judgeAgent.monitorDiscussion(message);
            if (!valid) {
                log.warn("Judge认为消息无效: {}", message);
            }
        }
    }

    @Override
    public void sendPrivateChatMessage(Long senderId, Long receiverId, String message) {
        log.info("玩家 {} 向玩家 {} 发送单聊消息: {}", senderId, receiverId, message);
        messageQueueService.sendPrivateChatMessage(message, senderId, receiverId);
    }

    @Override
    public void startSecondDiscussion() {
        log.info("开始第二轮讨论");
        discussionRound = 2;
        discussionState.put("discussionRound", discussionRound);

        // 重置单聊次数
        for (Long playerId : playerIds) {
            privateChatCounts.put(playerId, 0);
            privateChatInvitations.put(playerId, new ArrayList<>());
        }

        // 通知DM开始第二轮讨论
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String discussionInfo = "游戏ID: " + gameId + ", 玩家数量: " + playerIds.size() + ", 讨论轮次: " + discussionRound;
            String response = dmAgent.startDiscussion(discussionInfo);
            log.info("DM响应: {}", response);
        }

        // 开始陈述阶段
        startStatementPhase();
    }

    /**
     * 结束当前讨论阶段
     */
    private void endDiscussionPhase() {
        log.info("结束当前讨论阶段: {}", currentPhase);

        if (discussionRound == 1) {
            // 开始第二轮讨论
            startSecondDiscussion();
        } else {
            // 结束所有讨论
            endDiscussion();
        }
    }
}
