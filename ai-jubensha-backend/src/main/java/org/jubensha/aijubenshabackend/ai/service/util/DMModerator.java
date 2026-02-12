package org.jubensha.aijubenshabackend.ai.service.util;


import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.tools.SendDiscussionMessageTool;
import org.jubensha.aijubenshabackend.models.entity.Character;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.character.CharacterService;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DM主导逻辑实现
 * 用于引导讨论流程，控制发言顺序和时间，确保讨论的顺利进行
 *
 * @author Zewang
 * @version 1.0
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

    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 时间配置（秒）
    private static final long STATEMENT_TIME_PER_PLAYER = 300; // 5分钟/人
    private static final long FREE_DISCUSSION_TIME = 1800; // 30分钟
    private static final long PRIVATE_CHAT_TIME = 180; // 3分钟/次
    private static final long ANSWER_TIME = 600; // 10分钟

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
        executorService.submit(() -> {
            try {
                log.info("开始讨论环节，游戏ID: {}, 玩家数量: {}", gameId, playerIds.size());

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
                startStatementPhase(gameId, playerIds, dmId, characterIds, scriptId, dmAgent);

            } catch (Exception e) {
                log.error("开始讨论环节失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 开始陈述阶段
     */
    private void startStatementPhase(Long gameId, List<Long> playerIds, Long dmId, List<Long> characterIds, Long scriptId, DMAgent dmAgent) {
        try {
            log.info("开始陈述阶段，游戏ID: {}", gameId);

            // 切换到陈述阶段
            turnManager.switchPhase(gameId, TurnManager.PHASE_STATEMENT);

            // 宣布陈述阶段开始
            String phaseMessage = dmAgent.moderateDiscussion("开始陈述阶段，每位玩家有5分钟时间，请依次发言");
            sendDiscussionMessageTool.executeSendDiscussionMessage(phaseMessage, gameId, dmId, playerIds);

            // 依次让每位玩家发言
            for (int i = 0; i < playerIds.size(); i++) {
                Long playerId = playerIds.get(i);
                Long characterId = characterIds.get(i);

                // 宣布当前发言玩家
                Optional<Player> playerOpt = playerService.getPlayerById(playerId);
                Player player = playerOpt.orElseThrow(() -> new RuntimeException("玩家不存在"));
                String speakerMessage = String.format("现在请%s发言", player.getNickname());
                sendDiscussionMessageTool.executeSendDiscussionMessage(speakerMessage, gameId, dmId, playerIds);

                // 获取Player Agent并生成陈述
                PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);
                if (playerAgent != null) {
                    // 获取角色信息
                    Optional<Character> characterOpt = characterService.getCharacterById(characterId);
                    if (characterOpt.isPresent()) {
                        Character character = characterOpt.get();
                        // 调用PlayerAgent的generateStatement方法
                        String statement = playerAgent.generateStatement(
                                gameId.toString(),
                                playerId.toString(),
                                character.getId().toString(),
                                character.getName()
                        );
                        
                        // 发送陈述消息
                        if (statement != null && !statement.isEmpty()) {
                            log.info("AI玩家陈述，玩家ID: {}, 角色: {}, 内容: {}", playerId, character.getName(), statement);
                            sendDiscussionMessageTool.executeSendDiscussionMessage(statement, gameId, playerId, playerIds);
                        }
                    }
                }

                // 等待发言完成
                Thread.sleep(3000); // 等待3秒

                // 移动到下一个玩家
                turnManager.endCurrentTurn(gameId);
            }

            // 陈述阶段结束，进入自由讨论
            startFreeDiscussionPhase(gameId, playerIds, dmId, dmAgent);

        } catch (Exception e) {
            log.error("开始陈述阶段失败: {}", e.getMessage(), e);
        }
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

            // 启动自由讨论计时器
            // 这里可以集成TimerService

            // 模拟自由讨论时间
            Thread.sleep(FREE_DISCUSSION_TIME * 1000);

            // 自由讨论阶段结束，进入答题阶段
            startAnswerPhase(gameId, playerIds, dmId, dmAgent);

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
