package org.jubensha.aijubenshabackend.ai.service.util;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.tools.SendDiscussionMessageTool;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.GamePlayer;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.game.GamePlayerService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.jubensha.aijubenshabackend.ai.service.RAGService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.jubensha.aijubenshabackend.core.util.JsonValidationUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DM主导逻辑实现
 * 用于引导讨论流程，控制发言顺序和时间，确保讨论的顺利进行
 * 
 * 支持两种游戏模式：
 * 1. 真人模式：真人玩家通过WebSocket发言，AI玩家自动生成发言
 * 2. 观察者模式：所有玩家都是AI，自动完成所有流程
 *
 * @author Zewang
 * @version 1.1
 * @date 2026-02-06
 * @since 2026
 */

@Slf4j
@Component
public class DMModerator {

    @Autowired
    private AIService aiService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SendDiscussionMessageTool sendDiscussionMessageTool;

    @Autowired
    private TurnManager turnManager;

    @Autowired
    private DiscussionHelper discussionHelper;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private RAGService ragService;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private GamePlayerService gamePlayerService;

    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 真人玩家陈述等待锁 - key: gameId, value: 玩家ID到CountDownLatch的映射
    private final Map<Long, Map<Long, CountDownLatch>> statementWaitLatches = new ConcurrentHashMap<>();
    
    // 真人玩家ID集合 - key: gameId, value: 真人玩家ID集合
    private final Map<Long, Set<Long>> realPlayerIdsMap = new ConcurrentHashMap<>();

    // 时间配置（秒）
    private static final long STATEMENT_TIME_PER_PLAYER = 300; // 5分钟/人
    private static final long FREE_DISCUSSION_TIME = 1800; // 30分钟
    private static final long PRIVATE_CHAT_TIME = 180; // 3分钟/次
    private static final long ANSWER_TIME = 600; // 10分钟
    private static final long REAL_PLAYER_STATEMENT_TIMEOUT = 300; // 真人玩家陈述超时时间（秒）

    /**
     * 开始讨论环节
     *
     * @param gameId      游戏ID
     * @param playerIds   玩家ID列表
     * @param dmId        DM ID
     * @param characterIds 角色ID列表
     * @param scriptId    剧本ID
     */
    public void startDiscussion(Long gameId, List<Long> playerIds, Long dmId, List<Long> characterIds, Long scriptId) {
        // 获取真人玩家ID列表
        Set<Long> realPlayerIds = getRealPlayerIds(gameId, playerIds);
        realPlayerIdsMap.put(gameId, realPlayerIds);
        
        log.info("开始讨论环节，游戏ID: {}, 玩家数量: {}, 真人玩家数量: {}", 
                gameId, playerIds.size(), realPlayerIds.size());
        
        // 调用带真人玩家ID的方法
        startDiscussion(gameId, playerIds, dmId, characterIds, scriptId, realPlayerIds);
    }
    
    /**
     * 开始讨论环节（带真人玩家ID）
     *
     * @param gameId        游戏ID
     * @param playerIds     玩家ID列表
     * @param dmId          DM ID
     * @param characterIds  角色ID列表
     * @param scriptId      剧本ID
     * @param realPlayerIds 真人玩家ID集合
     */
    public void startDiscussion(Long gameId, List<Long> playerIds, Long dmId, List<Long> characterIds, 
                                 Long scriptId, Set<Long> realPlayerIds) {
        executorService.submit(() -> {
            try {
                log.info("[讨论开始] 游戏ID: {}, 玩家数量: {}, 真人玩家: {}", 
                        gameId, playerIds.size(), realPlayerIds);

                // 初始化游戏发言状态
                turnManager.initializeGameTurn(gameId, playerIds, dmId);

                // 获取DM Agent
                DMAgent dmAgent = aiService.getDMAgent(dmId);
                if (dmAgent == null) {
                    log.error("DM Agent不存在，DM ID: {}", dmId);
                    return;
                }

                // 宣布讨论开始
                String discussionInfo = String.format("游戏ID: %d, 玩家数量: %d, 剧本ID: %d", gameId, playerIds.size(), scriptId);
                String startMessage = dmAgent.startDiscussion(discussionInfo);

                // 发送讨论开始消息
                sendDiscussionMessageTool.executeSendDiscussionMessage(startMessage, gameId, dmId, playerIds);

                // 开始陈述阶段
                startStatementPhase(gameId, playerIds, dmId, characterIds, scriptId, dmAgent, realPlayerIds);

            } catch (Exception e) {
                log.error("开始讨论环节失败: {}", e.getMessage(), e);
            }
        });
    }
    
    /**
     * 获取真人玩家ID集合
     *
     * @param gameId    游戏ID
     * @param playerIds 玩家ID列表
     * @return 真人玩家ID集合
     */
    private Set<Long> getRealPlayerIds(Long gameId, List<Long> playerIds) {
        Set<Long> realPlayerIds = new HashSet<>();
        for (Long playerId : playerIds) {
            try {
                Optional<GamePlayer> gamePlayerOpt = gamePlayerService.getGamePlayerByGameIdAndPlayerId(gameId, playerId);
                if (gamePlayerOpt.isPresent()) {
                    GamePlayer gamePlayer = gamePlayerOpt.get();
                    // 通过Player实体获取角色类型
                    if (gamePlayer.getPlayer() != null && gamePlayer.getPlayer().getRole() == PlayerRole.REAL) {
                        realPlayerIds.add(playerId);
                        log.info("[玩家识别] 玩家 {} 是真人玩家", playerId);
                    }
                }
            } catch (Exception e) {
                log.warn("[玩家识别] 获取玩家 {} 的角色类型失败: {}", playerId, e.getMessage());
            }
        }
        return realPlayerIds;
    }
    
    /**
     * 判断玩家是否为真人玩家
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     * @return 是否为真人玩家
     */
    public boolean isRealPlayer(Long gameId, Long playerId) {
        Set<Long> realPlayerIds = realPlayerIdsMap.get(gameId);
        return realPlayerIds != null && realPlayerIds.contains(playerId);
    }
    
    /**
     * 处理真人玩家陈述完成
     * 当真人玩家通过WebSocket发送陈述消息时调用
     *
     * @param gameId   游戏ID
     * @param playerId 玩家ID
     */
    public void onRealPlayerStatementReceived(Long gameId, Long playerId) {
        log.info("[真人陈述] 收到真人玩家 {} 的陈述，游戏ID: {}", playerId, gameId);
        
        Map<Long, CountDownLatch> gameLatches = statementWaitLatches.get(gameId);
        if (gameLatches != null) {
            CountDownLatch latch = gameLatches.get(playerId);
            if (latch != null) {
                log.info("[真人陈述] 释放玩家 {} 的等待锁", playerId);
                latch.countDown();
            } else {
                log.warn("[真人陈述] 未找到玩家 {} 的等待锁", playerId);
            }
        } else {
            log.warn("[真人陈述] 未找到游戏 {} 的等待锁映射", gameId);
        }
    }

    /**
     * 开始陈述阶段
     * 区分真人玩家和AI玩家：
     * - 真人玩家：发送等待提示，等待WebSocket消息
     * - AI玩家：自动生成陈述并发送
     */
    private void startStatementPhase(Long gameId, List<Long> playerIds, Long dmId, 
                                      List<Long> characterIds, Long scriptId, DMAgent dmAgent,
                                      Set<Long> realPlayerIds) {
        try {
            log.info("[陈述阶段] 开始陈述阶段，游戏ID: {}, 玩家数量: {}, 真人玩家数量: {}", 
                    gameId, playerIds.size(), realPlayerIds.size());

            // 切换到陈述阶段
            turnManager.switchPhase(gameId, TurnManager.PHASE_STATEMENT);

            // 宣布陈述阶段开始
            String phaseMessage = dmAgent.moderateDiscussion("开始陈述阶段，每位玩家有5分钟时间，请依次发言");
            sendDiscussionMessageTool.executeSendDiscussionMessage(phaseMessage, gameId, dmId, playerIds);

            // 初始化真人玩家等待锁
            Map<Long, CountDownLatch> gameLatches = new ConcurrentHashMap<>();
            statementWaitLatches.put(gameId, gameLatches);

            // 依次让每位玩家发言
            for (int i = 0; i < playerIds.size(); i++) {
                Long playerId = playerIds.get(i);
                Long characterId = characterIds.get(i);

                // 获取角色信息
                Optional<Character> characterOpt = characterService.getCharacterById(characterId);
                Character character = characterOpt.orElse(null);
                String characterName = character != null ? character.getName() : "玩家" + playerId;

                // 宣布当前发言玩家
                String speakerMessage = String.format("现在请 %s 发言", characterName);
                sendDiscussionMessageTool.executeSendDiscussionMessage(speakerMessage, gameId, dmId, playerIds);

                // 判断是否为真人玩家
                boolean isRealPlayer = realPlayerIds.contains(playerId);
                
                if (isRealPlayer) {
                    // 真人玩家：等待WebSocket消息
                    log.info("[陈述阶段] 等待真人玩家 {} ({}) 发言", playerId, characterName);
                    
                    // 创建等待锁
                    CountDownLatch latch = new CountDownLatch(1);
                    gameLatches.put(playerId, latch);
                    
                    // 发送等待提示消息
                    String waitMessage = String.format("等待 %s 发言...", characterName);
                    sendDiscussionMessageTool.executeSendDiscussionMessage(waitMessage, gameId, dmId, playerIds);
                    
                    // 等待真人玩家发送消息（最多等待5分钟）
                    boolean received = latch.await(REAL_PLAYER_STATEMENT_TIMEOUT, TimeUnit.SECONDS);
                    
                    if (received) {
                        log.info("[陈述阶段] 真人玩家 {} ({}) 已完成发言", playerId, characterName);
                    } else {
                        log.warn("[陈述阶段] 真人玩家 {} ({}) 发言超时", playerId, characterName);
                        // 发送超时提示
                        String timeoutMessage = String.format("%s 发言时间已到，跳过该玩家", characterName);
                        sendDiscussionMessageTool.executeSendDiscussionMessage(timeoutMessage, gameId, dmId, playerIds);
                    }
                    
                    // 清理锁
                    gameLatches.remove(playerId);
                    
                } else {
                    // AI玩家：自动生成陈述
                    log.info("[陈述阶段] AI玩家 {} ({}) 开始生成陈述", playerId, characterName);
                    
                    PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
                    if (playerAgent != null && character != null) {
                        String statement = generateAIStatement(playerAgent, gameId, playerId, character, scriptId);
                        
                        if (statement != null && !statement.isEmpty()) {
                            log.info("[陈述阶段] AI玩家 {} ({}) 陈述内容: {}", playerId, characterName, 
                                    statement.length() > 100 ? statement.substring(0, 100) + "..." : statement);
                            sendDiscussionMessageTool.executeSendDiscussionMessage(statement, gameId, playerId, playerIds);
                        } else {
                            // 备用陈述
                            String fallbackStatement = String.format("我是%s，我需要时间整理一下思路。", characterName);
                            sendDiscussionMessageTool.executeSendDiscussionMessage(fallbackStatement, gameId, playerId, playerIds);
                        }
                    } else {
                        log.warn("[陈述阶段] 无法获取AI玩家 {} 的Agent或角色信息", playerId);
                        String fallbackStatement = String.format("我是%s，我需要时间整理一下思路。", characterName);
                        sendDiscussionMessageTool.executeSendDiscussionMessage(fallbackStatement, gameId, playerId, playerIds);
                    }
                }

                // 等待发言完成
                Thread.sleep(2000);

                // 移动到下一个玩家
                turnManager.endCurrentTurn(gameId);
            }

            // 陈述阶段结束，清理等待锁
            statementWaitLatches.remove(gameId);
            
            log.info("[陈述阶段] 陈述阶段结束，游戏ID: {}", gameId);
            
            // 通过ApplicationContext获取DiscussionService实例，启动自由讨论阶段
            try {
                Object discussionServiceBean = applicationContext.getBean("discussionServiceImpl");
                if (discussionServiceBean != null) {
                    java.lang.reflect.Method method = discussionServiceBean.getClass().getMethod("startFreeDiscussionPhase");
                    method.invoke(discussionServiceBean);
                    log.info("[陈述阶段] 已启动自由讨论阶段，游戏ID: {}", gameId);
                } else {
                    log.warn("[陈述阶段] 无法获取DiscussionService实例，自由讨论阶段未启动");
                }
            } catch (Exception e) {
                log.error("[陈述阶段] 启动自由讨论阶段失败: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("[陈述阶段] 开始陈述阶段失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 生成AI玩家陈述
     *
     * @param playerAgent 玩家Agent
     * @param gameId      游戏ID
     * @param playerId    玩家ID
     * @param character   角色
     * @param scriptId    剧本ID
     * @return 陈述内容
     */
    private String generateAIStatement(PlayerAgent playerAgent, Long gameId, Long playerId, 
                                        Character character, Long scriptId) {
        try {
            String jsonStatement = JsonValidationUtil.generateWithRetry(() -> {
                return playerAgent.generateStatement(
                        gameId.toString(),
                        playerId.toString(),
                        character.getId().toString(),
                        character.getName(),
                        scriptId.toString(),
                        character.getBackgroundStory() != null ? character.getBackgroundStory() : "暂无背景故事",
                        character.getSecret() != null ? character.getSecret() : "暂无秘密信息",
                        character.getTimeline() != null ? character.getTimeline() : "暂无时间线信息"
                );
            });
            
            // 解析JSON，提取content字段
            if (jsonStatement != null && !jsonStatement.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonStatement);
                if (rootNode.has("content")) {
                    return rootNode.get("content").asText();
                } else {
                    log.warn("[AI陈述] JSON格式不正确，缺少content字段: {}", jsonStatement);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("[AI陈述] AI玩家陈述生成失败，玩家ID: {}, 角色: {}", playerId, character.getName(), e);
        }
        return null;
    }

    /**
     * 开始自由讨论阶段
     */
    private void startFreeDiscussionPhase(Long gameId, List<Long> playerIds, Long dmId, DMAgent dmAgent) {
        try {
            log.info("开始自由讨论阶段，游戏ID: {}", gameId);

            // 切换到自由讨论阶段
            turnManager.switchPhase(gameId, TurnManager.PHASE_FREE_DISCUSSION);

            // 宣布自由讨论阶段开始
            String phaseMessage = dmAgent.moderateDiscussion("开始自由讨论阶段，大家可以畅所欲言，每位玩家有2次单聊机会，每次3分钟");
            sendDiscussionMessageTool.executeSendDiscussionMessage(phaseMessage, gameId, dmId, playerIds);

            // 调用DiscussionService的自由讨论阶段方法
            // 注意：DiscussionServiceImpl会处理中央调度器的启动和30分钟时间限制
            // 我们不需要在这里处理时间延迟，DiscussionServiceImpl会处理

        } catch (Exception e) {
            log.error("开始自由讨论阶段失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 开始答题阶段
     */
    private void startAnswerPhase(Long gameId, List<Long> playerIds, Long dmId, DMAgent dmAgent) {
        try {
            log.info("开始答题阶段，游戏ID: {}", gameId);

            // 切换到答题阶段
            turnManager.switchPhase(gameId, TurnManager.PHASE_ANSWER);

            // 宣布答题阶段开始
            String phaseMessage = dmAgent.moderateDiscussion("开始答题阶段，请每位玩家给出你的答案");
            sendDiscussionMessageTool.executeSendDiscussionMessage(phaseMessage, gameId, dmId, playerIds);

            // 启动答题计时器
            // 这里可以集成TimerService

            // 模拟答题时间
            Thread.sleep(ANSWER_TIME * 1000);

            // 答题阶段结束，进入评分阶段
            startScoringPhase(gameId, playerIds, dmId, dmAgent);

        } catch (Exception e) {
            log.error("开始答题阶段失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 开始评分阶段
     */
    private void startScoringPhase(Long gameId, List<Long> playerIds, Long dmId, DMAgent dmAgent) {
        try {
            log.info("开始评分阶段，游戏ID: {}", gameId);

            // 获取玩家答案（实际项目中应从数据库获取）
            List<Map<String, Object>> answers = new java.util.ArrayList<>();
            for (Long playerId : playerIds) {
                Map<String, Object> answer = new java.util.HashMap<>();
                answer.put("playerId", playerId);
                Optional<Player> playerOpt = playerService.getPlayerById(playerId);
                Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
                answer.put("playerName", player.getNickname());
                answer.put("answer", "这是一个示例答案"); // 实际项目中应从数据库获取
                answers.add(answer);
            }

            // DM评分
            String scoreMessage = dmAgent.scoreAnswers(answers);
            sendDiscussionMessageTool.executeSendDiscussionMessage(scoreMessage, gameId, dmId, playerIds);

            // 宣布讨论结束
            String endMessage = "讨论环节已结束，感谢大家的参与！";
            sendDiscussionMessageTool.executeSendDiscussionMessage(endMessage, gameId, dmId, playerIds);

            // 清理游戏发言状态
            turnManager.cleanup(gameId);

        } catch (Exception e) {
            log.error("开始评分阶段失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理单聊请求
     */
    public void handlePrivateChatRequest(Long gameId, Long senderId, Long receiverId, String message) {
        executorService.submit(() -> {
            try {
                log.info("处理单聊请求，发送者: {}, 接收者: {}", senderId, receiverId);

                // 检查单聊次数
                if (!turnManager.requestPrivateChat(gameId, senderId, receiverId)) {
                    log.warn("单聊请求被拒绝，发送者: {}", senderId);
                    return;
                }

                // 发送单聊邀请消息
                // 这里可以集成SendPrivateChatRequestTool

                // 启动单聊计时器
                // 这里可以集成TimerService

            } catch (Exception e) {
                log.error("处理单聊请求失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
        log.info("DMModerator线程池已关闭");
    }
}
