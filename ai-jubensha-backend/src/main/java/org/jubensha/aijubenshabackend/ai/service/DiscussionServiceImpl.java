package org.jubensha.aijubenshabackend.ai.service;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.service.util.DMModerator;
import org.jubensha.aijubenshabackend.ai.service.util.DiscussionReasoningManager;
import org.jubensha.aijubenshabackend.ai.service.util.MessageAccumulator;
import org.jubensha.aijubenshabackend.ai.service.util.TurnManager;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Resource
    private org.jubensha.aijubenshabackend.repository.dialogue.DialogueRepository dialogueRepository;

    @Resource
    private org.jubensha.aijubenshabackend.service.game.GameService gameService;

    @Resource
    private org.jubensha.aijubenshabackend.service.player.PlayerService playerService;

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

    // 讨论完成标记
    private boolean discussionCompleted = false;
    @Autowired
    private CharacterService characterService;

    // 讨论完成回调接口
    public interface DiscussionCompletionCallback {
        void onDiscussionCompleted(Map<String, Object> discussionState);
    }
    
    // 讨论完成回调
    private DiscussionCompletionCallback completionCallback;
    
    // 中央调度器
    private ScheduledExecutorService centralDirector;
    private ScheduledFuture<?> directorFuture;
    
    // 发言欲望值 (Desire to Speak Score)
    private final Map<Long, Integer> desireScores = new ConcurrentHashMap<>();
    
    // 最后发言时间
    private final Map<Long, LocalDateTime> lastSpeakTimes = new ConcurrentHashMap<>();
    
    // 发言阈值
    private static final int SPEAK_THRESHOLD = 120;

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
        
        // 注意：DMModerator会在单独的线程中执行陈述阶段
        // 陈述阶段结束后，自由讨论阶段将由中央调度器控制
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
        log.info("[自由讨论] 开始自由讨论阶段，游戏ID: {}, 玩家数量: {}, DM ID: {}", gameId, playerIds != null ? playerIds.size() : 0, dmId);
        currentPhase = "FREE_DISCUSSION";
        discussionState.put("currentPhase", currentPhase);
        discussionState.put("phaseStartTime", LocalDateTime.now());
        log.debug("[自由讨论] 更新讨论状态为: FREE_DISCUSSION");

        // 切换到自由讨论阶段
        log.debug("[自由讨论] 切换到自由讨论阶段");
        turnManager.switchPhase(gameId, TurnManager.PHASE_FREE_DISCUSSION);
        log.debug("[自由讨论] 已切换到自由讨论阶段");

        // 通知DM开始自由讨论阶段
        log.debug("[自由讨论] 通知DM开始自由讨论阶段");
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent != null) {
            String response = dmAgent.moderateDiscussion("开始自由讨论阶段，大家可以畅所欲言");
            log.info("[自由讨论] DM响应: {}", response);
        } else {
            log.warn("[自由讨论] 无法获取DM Agent");
        }

        // 启动中央调度器
        log.debug("[自由讨论] 启动中央调度器");
        startCentralDirector();
        log.info("[自由讨论] 中央调度器已启动，每1秒执行一次tick方法");

        // 启动自由讨论阶段计时器（2分钟）
        log.debug("[自由讨论] 启动自由讨论阶段计时器，时长: 2分钟");
        timerService.startTimer("FREE_DISCUSSION", 120L, this::startAnswerPhase);
        log.info("[自由讨论] 自由讨论阶段计时器已启动，2分钟后结束");
        log.info("[自由讨论] 跳过单聊环节，直接进入答题阶段");
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

        // 检测是否所有玩家都已提交答案
        if (playerAnswers.size() == playerIds.size()) {
            log.info("所有玩家都已提交答案，开始评分流程");
            endDiscussion();
        }
    }

    @Override
    public Map<String, Object> endDiscussion() {
        log.info("结束讨论");
        discussionState.put("endTime", LocalDateTime.now());
        discussionState.put("playerAnswers", playerAnswers);

        // 停止中央调度器
        stopCentralDirector();

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
            String scoreData = dmAgent.scoreAnswers(answers);
            log.info("DM评分响应: {}", scoreData);
            discussionState.put("scoreResponse", scoreData);
        }

        // 通知Judge总结讨论
        JudgeAgent judgeAgent = aiService.getJudgeAgent(judgeId);
        if (judgeAgent != null) {
            String discussionContent = "讨论内容摘要...";
            String summary = judgeAgent.summarizeDiscussion(discussionContent);
            log.info("Judge总结: {}", summary);
            discussionState.put("judgeSummary", summary);
        }

        // 标记讨论完成
        discussionCompleted = true;
        log.info("讨论已完成，游戏ID: {}", gameId);

        // 通知回调
        if (completionCallback != null) {
            completionCallback.onDiscussionCompleted(discussionState);
            log.info("已通知讨论完成回调");
        }

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
        Long characterId = null;
        try {
            Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
            if (gamePlayerOpt.isPresent()) {
                GamePlayer gamePlayer = gamePlayerOpt.get();
                if (gamePlayer.getCharacter() != null) {
                    playerName = gamePlayer.getCharacter().getName();
                    characterId = gamePlayer.getCharacter().getId();
                }
            }
        } catch (Exception e) {
            log.warn("[消息发送] 获取玩家角色名称失败: {}", e.getMessage());
        }
        log.info("[消息发送] {}发言: {}", playerName, message);
//        log.debug("[消息发送] 发言长度: {}字符", message.length());

        // 发送消息到所有玩家
//        log.debug("[消息发送] 发送消息到所有玩家，接收者数量: {}", playerIds != null ? playerIds.size() : 0);
        messageQueueService.sendDiscussionMessage(message, playerIds);
        log.info("[消息发送] 讨论消息已成功发送");
        
        // 存储讨论内容到数据库
        storeDiscussionMessageToDatabase(playerId, characterId, message);
        
        // 更新最后发言时间
        LocalDateTime now = LocalDateTime.now();
        lastSpeakTimes.put(playerId, now);
        log.debug("[消息发送] 更新玩家 {} 的最后发言时间为: {}", playerName, now);
        
        // 重置该玩家的发言欲望值
        desireScores.put(playerId, 0);
        log.debug("[消息发送] 重置玩家 {} 的发言欲望值为: 0", playerName);
    }
    
    /**
     * 存储讨论消息到数据库
     * 
     * @param playerId 玩家ID
     * @param characterId 角色ID
     * @param message 消息内容
     */
    private void storeDiscussionMessageToDatabase(Long playerId, Long characterId, String message) {
        try {
            log.debug("[消息存储] 开始存储讨论消息，玩家ID: {}, 角色ID: {}", playerId, characterId);
            
            // 创建对话记录
            org.jubensha.aijubenshabackend.models.entity.Dialogue dialogue = new org.jubensha.aijubenshabackend.models.entity.Dialogue();
            
            // 设置游戏信息
            org.jubensha.aijubenshabackend.models.entity.Game game = gameService.getGameById(gameId).orElse(null);
            if (game == null) {
                log.warn("[消息存储] 游戏不存在，游戏ID: {}", gameId);
                return;
            }
            dialogue.setGame(game);
            
            // 设置玩家信息
            org.jubensha.aijubenshabackend.models.entity.Player player = playerService.getPlayerById(playerId).orElse(null);
            if (player == null) {
                log.warn("[消息存储] 玩家不存在，玩家ID: {}", playerId);
                return;
            }
            dialogue.setPlayer(player);
            
            // 设置角色信息
            if (characterId != null) {
                org.jubensha.aijubenshabackend.models.entity.Character character = characterService.getCharacterById(characterId).orElse(null);
                if (character != null) {
                    dialogue.setCharacter(character);
                }
            }
            
            // 设置消息内容和类型
            dialogue.setContent(message);
            dialogue.setType(org.jubensha.aijubenshabackend.models.enums.DialogueType.CHAT);
            
            // 保存到数据库
            dialogueRepository.save(dialogue);
            log.info("[消息存储] 讨论消息已成功存储到数据库，对话ID: {}", dialogue.getId());
            
        } catch (Exception e) {
            log.error("[消息存储] 存储讨论消息失败: {}", e.getMessage(), e);
        }
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

        // 无论当前是第几轮，如果是答题阶段结束，直接结束讨论
        if ("ANSWER".equals(currentPhase)) {
            log.info("答题时间结束，结束讨论并进行评分");
            endDiscussion();
        } else if (discussionRound == 1) {
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
            log.info("[发言生成] 开始为玩家 {} 生成发言，任务: {}", getCharacterName(playerId), thinkingTask);
            
            // 获取Player Agent
            PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
            if (playerAgent == null) {
                log.warn("[发言生成] 无法获取玩家 {} 的PlayerAgent", getCharacterName(playerId));
                return null;
            }
            
            // 获取角色信息
            Character character = getCharacter(playerId);
            if (character == null) {
                log.warn("[发言生成] 无法获取玩家 {} 的角色信息", getCharacterName(playerId));
                return null;
            }
            
            String characterName = character.getName();
            log.debug("[发言生成] 玩家 {} 对应角色: {}", getCharacterName(playerId), characterName);
            
            // 获取思考结果
            String thinkingResult = null;
            try {
                thinkingResult = aiMindService.getComprehensiveThinkingResult(
                        gameId, playerId, thinkingTask
                );
                if (thinkingResult != null && !thinkingResult.isEmpty()) {
                    log.debug("[发言生成] 玩家 {} 思考结果长度: {}", getCharacterName(playerId), thinkingResult.length());
                } else {
                    log.warn("[发言生成] 玩家 {} 思考结果为空", getCharacterName(playerId));
                }
            } catch (Exception e) {
                log.warn("[发言生成] 获取思考结果失败: {}", e.getMessage());
            }
            
            // 获取角色信息
            String timeline = character.getTimeline();
            String secret = character.getSecret();
            String backgroundStory = character.getBackgroundStory();
            
            log.debug("[发言生成] 角色信息 - 时间线: {}, 秘密: {}, 背景故事: {}", 
                    timeline != null ? "有" : "无",
                    secret != null ? "有" : "无",
                    backgroundStory != null ? "有" : "无"
            );
            
            // 生成发言或答案
            try {
                String response;
                if ("分析所有信息并确定凶手".equals(thinkingTask)) {
                    // 使用专门的answer方法生成答案
                    response = playerAgent.answer(
                            gameId.toString(),
                            playerId.toString(),
                            characterName
                    );
                    log.info("[答案生成] 玩家 {} 成功生成答案，长度: {}", getCharacterName(playerId), response != null ? response.length() : 0);
                } else {
                    // 使用speakWithReasoning方法生成普通发言
                    response = playerAgent.speakWithReasoning(
                            gameId.toString(),
                            playerId.toString(),
                            thinkingResult != null ? thinkingResult : "分析当前局势并生成回应",
                            characterName,
                            secret != null ? secret : "",
                            timeline != null ? timeline : "",
                            backgroundStory != null ? backgroundStory : ""
                    );
                    log.info("[发言生成] 玩家 {} 成功生成发言，长度: {}", getCharacterName(playerId), response != null ? response.length() : 0);
                }
                
                if (response != null && !response.isEmpty()) {
                    return response;
                } else {
                    log.warn("[生成失败] 玩家 {} 生成的内容为空", getCharacterName(playerId));
                }
            } catch (Exception e) {
                log.error("[生成失败] 调用PlayerAgent方法失败: {}", e.getMessage(), e);
            }
            
        } catch (Exception e) {
            log.error("[发言生成] 执行思考并生成发言失败: {}", e.getMessage(), e);
        }
        
        log.warn("[发言生成] 玩家 {} 发言生成失败，需要使用备用方案", getCharacterName(playerId));
        return null;
    }

    /**
     * 获取角色
     */
    private Character getCharacter(Long playerId) {
        try {
            GamePlayer gamePlayer = gamePlayerService.getCharacterByPlayerId(playerId).orElse(null);
            return gamePlayer.getCharacter();
        } catch (Exception e) {
            log.error("获取角色失败: {}", e.getMessage(), e);
            return null;
        }
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
                int checkCount = 0;
                while (!discussionCompleted && checkCount < 60) { // 最多检查60次（1小时）
                    Thread.sleep(60000); // 每分钟检查一次
                    log.debug("检查讨论状态，游戏ID: {}, 完成状态: {}", gameId, discussionCompleted);
                    checkCount++;
                }
                if (discussionCompleted) {
                    log.info("讨论监控完成，游戏ID: {}", gameId);
                } else {
                    log.warn("讨论监控超时，游戏ID: {}, 可能存在状态检测问题", gameId);
                }
            } catch (Exception e) {
                log.error("讨论监控失败: {}", e.getMessage(), e);
            }
        });
    }
    
    /**
     * 启动中央调度器
     */
    private void startCentralDirector() {
        log.info("[中央调度器] 启动中央调度器");
        
        // 检查是否有玩家
        if (playerIds == null || playerIds.isEmpty()) {
            log.warn("[中央调度器] 没有玩家，无法启动中央调度器");
            return;
        }
        
        // 初始化欲望值和最后发言时间
        log.debug("[中央调度器] 初始化欲望值和最后发言时间");
        resetDesireScores();
        LocalDateTime now = LocalDateTime.now();
        for (Long playerId : playerIds) {
            lastSpeakTimes.put(playerId, now);
//            log.debug("[中央调度器] 初始化玩家 {} 的最后发言时间为: {}", getCharacterName(playerId), now);
        }
        log.info("[中央调度器] 已初始化 {} 个玩家的欲望值和最后发言时间", playerIds.size());
        
        // 创建并启动中央调度器
//        log.debug("[中央调度器] 创建并启动中央调度器线程");
        centralDirector = Executors.newSingleThreadScheduledExecutor();
        directorFuture = centralDirector.scheduleAtFixedRate(
            this::tick,
            0, // 初始延迟
            1, // 每1秒执行一次，减少发言滞后性
            TimeUnit.SECONDS
        );
        
        log.info("[中央调度器] 中央调度器已启动，每1秒执行一次tick方法，初始延迟: 0秒");
    }
    
    /**
     * 停止中央调度器
     */
    private void stopCentralDirector() {
        if (directorFuture != null && !directorFuture.isCancelled()) {
            directorFuture.cancel(false);
        }
        if (centralDirector != null && !centralDirector.isShutdown()) {
            centralDirector.shutdown();
            try {
                if (!centralDirector.awaitTermination(5, TimeUnit.SECONDS)) {
                    centralDirector.shutdownNow();
                }
            } catch (InterruptedException e) {
                centralDirector.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("中央调度器已停止");
    }
    
    /**
     * 重置所有玩家的欲望值
     */
    private void resetDesireScores() {
        log.debug("[中央调度器] 重置所有玩家的欲望值");
        if (playerIds == null || playerIds.isEmpty()) {
            log.warn("[中央调度器] 没有玩家，无法重置欲望值");
            return;
        }
        
        // 随机选择一个玩家设置为120，其他玩家依次下降
        int randomIndex = new java.util.Random().nextInt(playerIds.size());
        int baseScore = 120;
        
        for (int i = 0; i < playerIds.size(); i++) {
            Long playerId = playerIds.get(i);
            int score = i == randomIndex ? baseScore : baseScore - (i * 10);
            // 确保分数不为负
            score = Math.max(score, 0);
            desireScores.put(playerId, score);
            log.debug("[中央调度器] 重置玩家 {} 的欲望值为: {}", getCharacterName(playerId), score);
        }
        log.info("[中央调度器] 已重置 {} 个玩家的欲望值，随机选择的发言者初始值为: {}", playerIds.size(), baseScore);
    }
    
    /**
     * 计算玩家的发言欲望值
     * @param playerId 玩家ID
     * @return 发言欲望值
     */
    private int calculateDesireScore(Long playerId) {
        int score = 0;
        
        // 被提及 (+50)
        if (checkMentioned(playerId)) {
            score += 50;
//            log.debug("[欲望值计算] 玩家 {} 被提及，+50", getCharacterName(playerId));
        }
        
        // 话题相关 (+30)
        int topicScore = calculateTopicRelevance(playerId);
        score += topicScore;
//        log.debug("[欲望值计算] 玩家 {} 话题相关度，+{}", getCharacterName(playerId), topicScore);
        
        // 沉默时间补偿 (+2/sec，最高120)
        int silenceScore = calculateSilenceCompensation(playerId);
        score += silenceScore;
//        log.debug("[欲望值计算] 玩家 {} 沉默时间补偿，+{}", getCharacterName(playerId), silenceScore);
        
        // 性格因子
        int personalityScore = getPersonalityFactor(playerId);
        score += personalityScore;
//        log.debug("[欲望值计算] 玩家 {} 性格因子，+{}", getCharacterName(playerId), personalityScore);
        
//        log.info("[欲望值计算] 玩家 {} 最终欲望值: {}", getCharacterName(playerId), score);
        return score;
    }
    
    /**
     * 检查玩家是否被提及
     * @param playerId 玩家ID
     * @return 是否被提及
     */
    private boolean checkMentioned(Long playerId) {
        try {
            // 获取最近的讨论消息
            List<Map<String, Object>> recentMessages = messageAccumulator.getDiscussionMessages(gameId, 5);
            if (!recentMessages.isEmpty()) {
                Map<String, Object> lastMessageMap = recentMessages.get(recentMessages.size() - 1);
                String lastMessage = (String) lastMessageMap.get("content");
                String playerName = getCharacterName(playerId);
                
                // 检查消息是否包含玩家名称或@玩家
                return lastMessage.contains(playerName) || lastMessage.contains("@" + playerName);
            }
        } catch (Exception e) {
            log.warn("检查玩家被提及失败: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * 计算话题相关度
     * @param playerId 玩家ID
     * @return 话题相关度得分
     */
    private int calculateTopicRelevance(Long playerId) {
        try {
            // 获取玩家角色信息
            Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
            if (gamePlayerOpt.isPresent()) {
                GamePlayer gamePlayer = gamePlayerOpt.get();
                Character character = gamePlayer.getCharacter();
                if (character != null) {
                    // 这里应该使用EmbeddingService计算话题相关度
                    // 简化实现，返回固定值
                    return 30;
                }
            }
        } catch (Exception e) {
            log.warn("计算话题相关度失败: {}", e.getMessage());
        }
        return 20; // 即使获取失败，也给予基础分数，确保玩家能够更容易地开始发言
    }
    
    /**
     * 计算沉默时间补偿
     * @param playerId 玩家ID
     * @return 沉默时间补偿得分
     */
    private int calculateSilenceCompensation(Long playerId) {
        try {
            LocalDateTime lastSpeakTime = lastSpeakTimes.getOrDefault(playerId, LocalDateTime.now());
            Duration duration = Duration.between(lastSpeakTime, LocalDateTime.now());
            long seconds = duration.getSeconds();
            
            // 每秒钟增加2分，最多增加120分
            int compensation = Math.min((int) seconds * 2, 120);
            
            // 初始状态下，给予基础补偿，确保玩家能够更容易地开始发言
            if (seconds < 5) { // 前5秒
                compensation = Math.max(compensation, 15); // 给予至少15分的基础补偿
            }
            
            log.debug("[沉默时间补偿] 玩家 {} 沉默时间: {}秒，补偿: {}分", getCharacterName(playerId), seconds, compensation);
            return compensation;
        } catch (Exception e) {
            log.warn("计算沉默时间补偿失败: {}", e.getMessage());
        }
        return 15; // 异常情况下，给予基础补偿
    }
    
    /**
     * 获取性格因子
     * @param playerId 玩家ID
     * @return 性格因子得分
     */
    private int getPersonalityFactor(Long playerId) {
        try {
            // 获取玩家角色信息
            Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
            if (gamePlayerOpt.isPresent()) {
                GamePlayer gamePlayer = gamePlayerOpt.get();
                Character character = gamePlayer.getCharacter();
                if (character != null) {
                    // 这里应该根据角色性格返回不同的因子
                    // 简化实现，返回随机值，但调整范围，避免过度降低欲望值
                    return (int) (Math.random() * 15 - 5); // -5到10之间，更倾向于正数
                }
            }
        } catch (Exception e) {
            log.warn("获取性格因子失败: {}", e.getMessage());
        }
        return 5; // 异常情况下，给予正数基础分，确保玩家能够更容易地开始发言
    }
    
    /**
     * 中央调度器的tick方法，每5秒执行一次
     */
    private void tick() {
        try {
            // 检查讨论是否已完成
            if (discussionCompleted) {
                log.debug("中央调度器跳过执行：讨论已完成={}", discussionCompleted);
                return;
            }
            
            // 检查当前是否为自由讨论阶段
            if (!"FREE_DISCUSSION".equals(currentPhase)) {
                log.debug("中央调度器跳过执行：当前阶段不是自由讨论，而是: {}", currentPhase);
                return;
            }
            
            // 检查是否有玩家
            if (playerIds == null || playerIds.isEmpty()) {
                log.warn("中央调度器跳过执行：没有玩家");
                return;
            }
            
            log.info("[中央调度器] 执行tick方法，当前时间: {}", java.time.LocalDateTime.now());
            
            // 计算所有玩家的欲望值
            Map<Long, Integer> currentScores = new HashMap<>();
            for (Long playerId : playerIds) {
                int score = calculateDesireScore(playerId);
                currentScores.put(playerId, score);
                desireScores.put(playerId, score);
                log.info("[中央调度器] 玩家 {} 的发言欲望值: {}", getCharacterName(playerId), score);
            }
            
            // 选择下一个发言的玩家
            Long nextSpeakerId = selectNextSpeaker(currentScores);
            if (nextSpeakerId != null) {
                log.info("[中央调度器] 选择玩家 {} 发言，欲望值: {}, 发言阈值: {}", 
                        getCharacterName(nextSpeakerId), currentScores.get(nextSpeakerId), SPEAK_THRESHOLD);
                
                // 执行发言（移除发言锁定，根据用户建议）
                executorService.submit(() -> {
                    try {
                        log.info("[中央调度器] 开始处理玩家 {} 的发言", getCharacterName(nextSpeakerId));
                        // 生成讨论内容
                        String discussionContent = executeThinkingAndGenerateResponse(
                                nextSpeakerId, "分析当前讨论并生成回应"
                        );
                        
                        // 发送讨论消息
                        if (discussionContent != null && !discussionContent.isEmpty()) {
                            log.info("[中央调度器] 玩家 {} 生成发言内容，长度: {}", getCharacterName(nextSpeakerId), discussionContent.length());
                            sendDiscussionMessage(nextSpeakerId, discussionContent);
                        } else {
                            log.warn("[中央调度器] 玩家 {} 未能生成发言内容，尝试备用方案", getCharacterName(nextSpeakerId));
                            // 备用方案：使用讨论推理管理器
                            String backupContent = discussionReasoningManager.processReasoningAndDiscussion(gameId, nextSpeakerId);
                            if (!backupContent.isEmpty()) {
                                log.info("[中央调度器] 玩家 {} 使用备用方案生成发言内容，长度: {}", getCharacterName(nextSpeakerId), backupContent.length());
                                sendDiscussionMessage(nextSpeakerId, backupContent);
                            } else {
                                log.warn("[中央调度器] 玩家 {} 备用方案也未能生成发言内容", getCharacterName(nextSpeakerId));
                            }
                        }
                    } catch (Exception e) {
                        log.error("[中央调度器] 处理AI玩家发言失败: {}", e.getMessage(), e);
                    }
                });
            } else {
                log.debug("[中央调度器] 没有玩家的发言欲望值超过阈值 {}", SPEAK_THRESHOLD);
                // 打印所有玩家的欲望值，以便调试
                for (Long playerId : playerIds) {
//                    log.debug("[中央调度器] 玩家 {} 的欲望值: {}", getCharacterName(playerId), currentScores.get(playerId));
                }
            }
        } catch (Exception e) {
            log.error("[中央调度器] tick执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 选择下一个发言的玩家
     * @param scores 所有玩家的欲望值
     * @return 选中的玩家ID，若没有则返回null
     */
    private Long selectNextSpeaker(Map<Long, Integer> scores) {
        // 找出欲望值最高的玩家
        Long selectedPlayerId = null;
        int maxScore = 0;
        
        for (Map.Entry<Long, Integer> entry : scores.entrySet()) {
            Long playerId = entry.getKey();
            int score = entry.getValue();
            
            if (score > maxScore) {
                maxScore = score;
                selectedPlayerId = playerId;
            }
        }
        
        // 检查是否有玩家
        if (selectedPlayerId == null) {
            log.warn("[中央调度器] 没有玩家可以选择发言");
            return null;
        }
        
        // 降低初始发言的阈值要求，确保玩家能够更容易地开始发言
        // 对于第一轮发言，降低阈值要求
        boolean hasAnyPlayerSpoken = false;
        for (LocalDateTime time : lastSpeakTimes.values()) {
            if (time != null) {
                hasAnyPlayerSpoken = true;
                break;
            }
        }
        
        // 初始发言阈值降低到10，后续发言保持20
        int effectiveThreshold = hasAnyPlayerSpoken ? SPEAK_THRESHOLD : 10;
        
        // 如果最高分数超过有效阈值或没有玩家发言过，选择该玩家
        if (maxScore >= effectiveThreshold || !hasAnyPlayerSpoken) {
            log.info("[中央调度器] 选择玩家 {} 发言，欲望值: {}, 有效发言阈值: {}", 
                    getCharacterName(selectedPlayerId), maxScore, effectiveThreshold);
            return selectedPlayerId;
        }
        
        log.debug("[中央调度器] 没有玩家的发言欲望值超过阈值 {}，最高分数: {}", effectiveThreshold, maxScore);
        return null;
    }

    /**
     * 获取讨论完成状态
     */
    public boolean isDiscussionCompleted() {
        return discussionCompleted;
    }

    @Override
    public Map<String, Object> verifyAnswerPhase(Long gameId, List<Long> playerIds, Long dmId, Long judgeId) {
        log.info("开始单独验证答题环节，游戏ID: {}, 玩家数量: {}, DM ID: {}, Judge ID: {}", gameId, playerIds.size(), dmId, judgeId);

        // 初始化游戏信息
        this.gameId = gameId;
        this.playerIds = playerIds;
        this.dmId = dmId;
        this.judgeId = judgeId;

        // 重置状态
        discussionState.clear();
        discussionState.put("gameId", gameId);
        discussionState.put("playerIds", playerIds);
        discussionState.put("dmId", dmId);
        discussionState.put("judgeId", judgeId);
        discussionState.put("startTime", LocalDateTime.now());
        discussionState.put("currentPhase", "VERIFICATION");

        playerAnswers.clear();
        discussionCompleted = false;

        // 直接进入答题阶段
        startAnswerPhase();

        // 等待答题完成（模拟）
        try {
            Thread.sleep(30000); // 等待30秒，让AI有时间生成答案
        } catch (InterruptedException e) {
            log.error("等待答题完成时被中断: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }

        // 结束讨论，获取结果
        Map<String, Object> result = endDiscussion();
        log.info("答题环节验证完成，游戏ID: {}", gameId);

        return result;
    }

    @Override
    public String testDMScore(Long dmId, List<Map<String, Object>> answers) {
        log.info("开始测试DM评分功能，DM ID: {}, 玩家答案数量: {}", dmId, answers.size());

        // 获取DMAgent实例
        DMAgent dmAgent = aiService.getDMAgent(dmId);
        if (dmAgent == null) {
            log.error("无法获取DM Agent，DM ID: {}", dmId);
            return "{\"error\": \"无法获取DM Agent\"}";
        }

        try {
            // 调用DMAgent的scoreAnswers方法
            String scoreData = dmAgent.scoreAnswers(answers);
            log.info("DM评分测试完成，评分结果长度: {}", scoreData.length());
            log.debug("DM评分结果: {}", scoreData);

            return scoreData;
        } catch (Exception e) {
            log.error("测试DM评分功能失败: {}", e.getMessage(), e);
            return "{\"error\": \"测试DM评分功能失败: " + e.getMessage() + "\"}";
        }
    }
}
