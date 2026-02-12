package org.jubensha.aijubenshabackend.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.service.util.DMModerator;
import org.jubensha.aijubenshabackend.ai.service.util.DiscussionReasoningManager;
import org.jubensha.aijubenshabackend.ai.service.util.MessageAccumulator;
import org.jubensha.aijubenshabackend.ai.service.util.TurnManager;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private AIMindService aiMindService;

    @Resource
    private MessageQueueService messageQueueService;

    @Resource
    private TimerService timerService;

    @Resource
    private MessageAccumulator messageAccumulator;

    @Resource
    private TurnManager turnManager;

    @Resource
    private DMModerator dmModerator;

    @Resource
    private DiscussionReasoningManager discussionReasoningManager;

    @Resource
    private GamePlayerService gamePlayerService;

    // 讨论状态
    private final Map<String, Object> discussionState = new ConcurrentHashMap<>();

    // 游戏信息
    private Long gameId;
    private List<Long> playerIds;
    private Long dmId;

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

    // 讨论完成标记
    private boolean discussionCompleted = false;
    
    // 讨论完成回调接口
    public interface DiscussionCompletionCallback {
        void onDiscussionCompleted(Map<String, Object> discussionState);
    }
    
    // 讨论完成回调
    private DiscussionCompletionCallback completionCallback;

    /**
     * 线程池，用于并行处理AI推理任务
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void startDiscussion(Long gameId, List<Long> playerIds, Long dmId, Long judgeId) {
        log.info("开始讨论，游戏ID: {}, 玩家数量: {}, DM ID: {}", gameId, playerIds.size(), dmId);

        this.gameId = gameId;
        this.playerIds = playerIds;
        this.dmId = dmId;

        // 初始化讨论状态
        discussionState.clear();
        discussionState.put("gameId", gameId);
        discussionState.put("playerIds", playerIds);
        discussionState.put("dmId", dmId);
        discussionState.put("discussionRound", discussionRound);
        discussionState.put("startTime", LocalDateTime.now());
        discussionState.put("currentPhase", "INITIALIZED");

        // 初始化单聊次数
        for (Long playerId : playerIds) {
            privateChatCounts.put(playerId, 0);
            privateChatInvitations.put(playerId, new ArrayList<>());
        }

        // 获取角色ID列表和剧本ID
        List<Long> characterIds = new ArrayList<>();
        Long scriptId = null;
        
        for (Long playerId : playerIds) {
            // 从GamePlayer关系中获取角色信息
            Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
            if (gamePlayerOpt.isPresent()) {
                GamePlayer gamePlayer = gamePlayerOpt.get();
                Character character = gamePlayer.getCharacter();
                if (character != null) {
                    characterIds.add(character.getId());
                    // 获取剧本ID（所有角色属于同一个剧本）
                    if (scriptId == null) {
                        scriptId = character.getScriptId();
                    }
                } else {
                    log.warn("玩家 {} 没有分配角色", playerId);
                    characterIds.add(1L); // 备用方案
                }
            } else {
                log.warn("未找到玩家 {} 的游戏记录", playerId);
                characterIds.add(1L); // 备用方案
            }
        }
        
        // 确保剧本ID有值
        if (scriptId == null) {
            scriptId = 1L; // 备用方案
            log.warn("未找到剧本ID，使用默认值 1");
        }
        
        log.info("获取到角色ID列表: {}, 剧本ID: {}", characterIds, scriptId);

        // 使用DMModerator启动讨论环节
        dmModerator.startDiscussion(gameId, playerIds, dmId, characterIds, scriptId);

        // 启动讨论完成的监控
        startDiscussionCompletionMonitor();
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
                    // 执行思考并生成陈述
                    String statement = executeThinkingAndGenerateResponse(
                            playerId, "请生成一个针对当前案件的陈述"
                    );

                    // 发送陈述消息
                    if (statement != null && !statement.isEmpty()) {
                        String characterName = getCharacterName(playerId);
                        log.info("AI玩家陈述，玩家ID: {}, 角色: {}, 内容: {}", playerId, characterName, statement);
                        sendDiscussionMessage(playerId, statement);
                    } else {
                        // 备用方案：使用讨论推理管理器
                        String backupStatement = discussionReasoningManager.processReasoningAndDiscussion(gameId, playerId);
                        if (!backupStatement.isEmpty()) {
                            log.info("AI玩家陈述（备用方案），玩家ID: {}, 内容: {}", playerId, backupStatement);
                            sendDiscussionMessage(playerId, backupStatement);
                        }
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
                    // 进行多轮讨论
                    for (int round = 1; round <= 3; round++) {
                        // 随机等待一段时间
                        Thread.sleep((long) (Math.random() * 8000) + 2000);

                        // 执行思考并生成讨论内容
                        String discussionContent = executeThinkingAndGenerateResponse(
                                playerId, "分析当前讨论并生成回应"
                        );

                        // 发送讨论消息
                        if (discussionContent != null && !discussionContent.isEmpty()) {
                            log.info("AI玩家自由讨论，玩家ID: {}, 轮次: {}, 内容: {}", playerId, round, discussionContent);
                            sendDiscussionMessage(playerId, discussionContent);
                        } else {
                            // 备用方案：使用讨论推理管理器
                            String backupContent = discussionReasoningManager.processReasoningAndDiscussion(gameId, playerId);
                            if (!backupContent.isEmpty()) {
                                log.info("AI玩家自由讨论（备用方案），玩家ID: {}, 轮次: {}, 内容: {}", playerId, round, backupContent);
                                sendDiscussionMessage(playerId, backupContent);
                            }
                        }
                    }
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
                    // 获取Player Agent
                    PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
                    if (playerAgent != null) {
                        // 使用AIMindService获取思考结果
                        String thinkingResult = aiMindService.getComprehensiveThinkingResult(
                                gameId, playerId, "分析当前局势并决定是否需要发起单聊"
                        );

                        // 调用PlayerAgent的decidePrivateChat方法
                        String decision = playerAgent.decidePrivateChat(
                                gameId.toString(),
                                playerId.toString()
                        );

                        log.info("AI玩家单聊决策，玩家ID: {}, 决策: {}", playerId, decision);
                        // 解析决策结果，决定是否发起单聊
                        parsePrivateChatDecision(playerId, decision);
                    } else {
                        // 备用方案：使用讨论推理管理器
                        String decision = discussionReasoningManager.decidePrivateChat(gameId, playerId);
                        if (!decision.isEmpty()) {
                            log.info("AI玩家单聊决策（备用方案），玩家ID: {}, 决策: {}", playerId, decision);
                            // 解析决策结果，决定是否发起单聊
                            parsePrivateChatDecision(playerId, decision);
                        }
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

        // 启动AI玩家的答题推理
        for (Long playerId : playerIds) {
            executorService.submit(() -> {
                try {
                    // 执行思考并生成答案
                    String answer = executeThinkingAndGenerateResponse(
                            playerId, "分析所有信息并确定凶手"
                    );

                    // 提交答案
                    if (answer != null && !answer.isEmpty()) {
                        log.info("AI玩家答题，玩家ID: {}, 答案: {}", playerId, answer);
                        submitAnswer(playerId, answer);
                    }
                } catch (Exception e) {
                    log.error("处理AI玩家答题失败: {}", e.getMessage(), e);
                }
            });
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
        discussionCompleted = true;

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

        // 讨论总结由DM负责
        discussionState.put("judgeSummary", "讨论已完成，详细内容请参考讨论记录");

        // 通知回调
        if (completionCallback != null) {
            completionCallback.onDiscussionCompleted(discussionState);
            log.info("已通知讨论完成回调");
        }

        // 标记讨论完成
        log.info("讨论已完成，游戏ID: {}", gameId);

        return discussionState;
    }
    
    /**
     * 设置讨论完成回调
     */
    public void setCompletionCallback(DiscussionCompletionCallback callback) {
        this.completionCallback = callback;
        log.info("已设置讨论完成回调");
    }

    @Override
    public Map<String, Object> getDiscussionState() {
        return discussionState;
    }

    @Override
    public void sendDiscussionMessage(Long playerId, String message) {
        // 获取玩家名称（从GamePlayer关系中获取角色名称）
        String playerName = "AI玩家" + playerId;
        try {
            Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
            if (gamePlayerOpt.isPresent()) {
                GamePlayer gamePlayer = gamePlayerOpt.get();
                if (gamePlayer.getCharacter() != null) {
                    playerName = gamePlayer.getCharacter().getName();
                }
            }
        } catch (Exception e) {
            log.warn("获取玩家角色名称失败: {}", e.getMessage());
        }
        log.info("{}发言: {}", playerName, message);

        // 发送消息到所有玩家
        messageQueueService.sendDiscussionMessage(message, playerIds);
    }

    @Override
    public void sendPrivateChatMessage(Long senderId, Long receiverId, String message) {
        String senderName = "AI玩家" + senderId;
        String receiverName = "AI玩家" + receiverId;
        
        // 获取发送者角色名称
        try {
            Optional<GamePlayer> senderOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, senderId);
            if (senderOpt.isPresent() && senderOpt.get().getCharacter() != null) {
                senderName = senderOpt.get().getCharacter().getName();
            }
        } catch (Exception e) {
            log.warn("获取发送者角色名称失败: {}", e.getMessage());
        }
        
        // 获取接收者角色名称
        try {
            Optional<GamePlayer> receiverOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, receiverId);
            if (receiverOpt.isPresent() && receiverOpt.get().getCharacter() != null) {
                receiverName = receiverOpt.get().getCharacter().getName();
            }
        } catch (Exception e) {
            log.warn("获取接收者角色名称失败: {}", e.getMessage());
        }
        
        log.info("{}向{}发送单聊消息: 单聊内容为{}", senderName, receiverName, message);
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

    /**
     * 获取角色名称
     *
     * @param playerId 玩家ID
     * @return 角色名称
     */
    private String getCharacterName(Long playerId) {
        try {
            Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
            if (gamePlayerOpt.isPresent() && gamePlayerOpt.get().getCharacter() != null) {
                return gamePlayerOpt.get().getCharacter().getName();
            }
        } catch (Exception e) {
            log.warn("获取角色名称失败: {}", e.getMessage());
        }
        return "AI玩家" + playerId;
    }

    /**
     * 执行思考并生成发言
     *
     * @param playerId     玩家ID
     * @param thinkingTask 思考任务
     * @return 发言内容
     */
    private String executeThinkingAndGenerateResponse(Long playerId, String thinkingTask) {
        try {
            PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
            if (playerAgent != null) {
                String characterName = getCharacterName(playerId);
                String thinkingResult = aiMindService.getComprehensiveThinkingResult(
                        gameId, playerId, thinkingTask
                );
                return playerAgent.speakWithReasoning(
                        gameId.toString(),
                        playerId.toString(),
                        thinkingResult,
                        characterName
                );
            }
        } catch (Exception e) {
            log.error("执行思考并生成发言失败: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 解析单聊决策
     */
    private void parsePrivateChatDecision(Long playerId, String decision) {
        try {
            // 简单解析决策结果，寻找可能的目标玩家ID
            // 实际项目中应该使用更复杂的解析逻辑
            for (Long potentialReceiverId : playerIds) {
                if (!potentialReceiverId.equals(playerId) && decision.contains(potentialReceiverId.toString())) {
                    sendPrivateChatInvitation(playerId, potentialReceiverId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("解析单聊决策失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 启动讨论完成监控
     */
    private void startDiscussionCompletionMonitor() {
        executorService.submit(() -> {
            try {
                // 定期检查讨论状态
                while (!discussionCompleted) {
                    Thread.sleep(60000); // 每分钟检查一次
                    log.debug("检查讨论状态，游戏ID: {}, 完成状态: {}", gameId, discussionCompleted);
                }
                log.info("讨论监控完成，游戏ID: {}", gameId);
            } catch (Exception e) {
                log.error("讨论监控失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 获取讨论完成状态
     */
    public boolean isDiscussionCompleted() {
        return discussionCompleted;
    }
}
