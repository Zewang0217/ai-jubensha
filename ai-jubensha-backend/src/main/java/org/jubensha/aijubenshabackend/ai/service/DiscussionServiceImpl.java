package org.jubensha.aijubenshabackend.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.ai.service.util.DMModerator;
import org.jubensha.aijubenshabackend.ai.service.util.DiscussionHelper;
import org.jubensha.aijubenshabackend.ai.service.util.DiscussionReasoningManager;
import org.jubensha.aijubenshabackend.ai.service.util.MessageAccumulator;
import org.jubensha.aijubenshabackend.ai.service.util.TurnManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Resource
    private MessageAccumulator messageAccumulator;

    @Resource
    private TurnManager turnManager;

    @Resource
    private DiscussionHelper discussionHelper;

    @Resource
    private DMModerator dmModerator;

    @Resource
    private DiscussionReasoningManager discussionReasoningManager;

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

    /**
     * 线程池，用于并行处理AI推理任务
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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

        // 这里需要获取角色ID列表和剧本ID
        // 实际项目中应该从游戏信息中获取
        List<Long> characterIds = new ArrayList<>();
        for (Long playerId : playerIds) {
            characterIds.add(1L); // 示例角色ID，实际项目中应从数据库获取
        }
        Long scriptId = 1L; // 示例剧本ID，实际项目中应从数据库获取

        // 使用DMModerator启动讨论环节
        dmModerator.startDiscussion(gameId, playerIds, dmId, characterIds, scriptId);
    }

    @Override
    public void startStatementPhase() {
        log.info("开始陈述阶段");
        currentPhase = "STATEMENT";
        discussionState.put("currentPhase", currentPhase);
        discussionState.put("phaseStartTime", LocalDateTime.now());

        // 切换到陈述阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_STATEMENT);

        // 通知DM开始陈述阶段
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始陈述阶段，每位玩家有5分钟时间");
            log.info("DM响应: {}", response);
        }

        // 启动AI玩家的陈述推理
        for (Long playerId : playerIds) {
            executorService.submit(() -> {
                try {
                    String statement = discussionReasoningManager.processReasoningAndDiscussion(gameId, playerId);
                    if (!statement.isEmpty()) {
                        log.info("AI玩家陈述，玩家ID: {}, 内容: {}", playerId, statement);
                        sendDiscussionMessage(playerId, statement);
                    }
                } catch (Exception e) {
                    log.error("处理AI玩家陈述失败: {}", e.getMessage(), e);
                }
            });
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

        // 切换到自由讨论阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_FREE_DISCUSSION);

        // 通知DM开始自由讨论阶段
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始自由讨论阶段，大家可以畅所欲言");
            log.info("DM响应: {}", response);
        }

        // 启动AI玩家的自由讨论推理
        for (Long playerId : playerIds) {
            executorService.submit(() -> {
                try {
                    // 异步处理AI玩家的推理和讨论
                    discussionReasoningManager.processReasoningAsync(gameId, playerId)
                            .thenAccept(result -> {
                                if (!result.isEmpty()) {
                                    log.info("AI玩家自由讨论，玩家ID: {}, 内容: {}", playerId, result);
                                    sendDiscussionMessage(playerId, result);
                                }
                            })
                            .exceptionally(ex -> {
                                log.error("处理AI玩家自由讨论失败: {}", ex.getMessage(), ex);
                                return null;
                            });
                } catch (Exception e) {
                    log.error("启动AI玩家自由讨论失败: {}", e.getMessage(), e);
                }
            });
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

        // 切换到单聊阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_PRIVATE_CHAT);

        // 通知DM开始单聊阶段
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始单聊阶段，每位玩家有2次单聊机会，每次3分钟");
            log.info("DM响应: {}", response);
        }

        // 启动AI玩家的单聊决策推理
        for (Long playerId : playerIds) {
            executorService.submit(() -> {
                try {
                    String decision = discussionReasoningManager.decidePrivateChat(gameId, playerId);
                    if (!decision.isEmpty()) {
                        log.info("AI玩家单聊决策，玩家ID: {}, 决策: {}", playerId, decision);
                        // TODO:这里可以解析决策结果，决定是否发起单聊
                        // 示例：如果决策包含目标玩家ID，则发起单聊
                    }
                } catch (Exception e) {
                    log.error("处理AI玩家单聊决策失败: {}", e.getMessage(), e);
                }
            });
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

        // 切换到答题阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_ANSWER);

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
        String senderName = "AI玩家" + senderId;
        String receiverName = "AI玩家" + receiverId;
        log.info("AI玩家{}向AI玩家{}发起单聊申请", senderName, receiverName);

        // 使用TurnManager检查单聊请求
        boolean allowed = turnManager.requestPrivateChat(gameId, senderId, receiverId);
        if (!allowed) {
            log.warn("单聊请求被拒绝，发送者: {}", senderName);
            return;
        }

        // 检查接收者是否存在
        if (!playerIds.contains(receiverId)) {
            log.warn("接收者 {} 不存在", receiverName);
            return;
        }

        // 发送单聊邀请
        messageQueueService.sendPrivateChatMessage("单聊邀请", senderId, receiverId);

        // 记录单聊邀请
        privateChatInvitations.get(senderId).add(receiverId);
        privateChatCounts.put(senderId, privateChatCounts.getOrDefault(senderId, 0) + 1);

        // 启动单聊计时器（3分钟）
        timerService.startTimer("PRIVATE_CHAT_" + senderId + "_" + receiverId, 180L, () -> {
            log.info("单聊结束，发送者: {}, 接收者: {}", senderName, receiverName);
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
        // 获取玩家名称（这里简化处理，实际应该从数据库或缓存中获取）
        String playerName = "AI玩家" + playerId;
        log.info("AI玩家{}发言: {}", playerName, message);

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
        String senderName = "AI玩家" + senderId;
        String receiverName = "AI玩家" + receiverId;
        log.info("AI玩家{}向AI玩家{}发送单聊消息: 单聊内容为{}", senderName, receiverName, message);
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
