package org.jubensha.aijubenshabackend.ai.service;


import java.util.List;
import java.util.Map;

/**
 * 讨论服务接口
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-05 15:30
 * @since 2026
 */
public interface DiscussionService {

    /**
     * 开始讨论
     *
     * @param gameId     游戏ID
     * @param playerIds  玩家ID列表
     * @param dmId       DM ID
     * @param judgeId    Judge ID
     */
    void startDiscussion(Long gameId, List<Long> playerIds, Long dmId, Long judgeId);

    /**
     * 开始陈述阶段
     */
    void startStatementPhase();

    /**
     * 开始自由讨论阶段
     */
    void startFreeDiscussionPhase();

    /**
     * 开始单聊阶段
     */
    void startPrivateChatPhase();

    /**
     * 开始答题阶段
     */
    void startAnswerPhase();

    /**
     * 发送单聊邀请
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     */
    void sendPrivateChatInvitation(Long senderId, Long receiverId);

    /**
     * 提交答案
     *
     * @param playerId 玩家ID
     * @param answer   答案
     */
    void submitAnswer(Long playerId, String answer);

    /**
     * 结束讨论
     *
     * @return 讨论结果
     */
    Map<String, Object> endDiscussion();

    /**
     * 获取讨论状态
     *
     * @return 讨论状态
     */
    Map<String, Object> getDiscussionState();

    /**
     * 发送讨论消息
     *
     * @param playerId 玩家ID
     * @param message  消息内容
     */
    void sendDiscussionMessage(Long playerId, String message);

    /**
     * 发送单聊消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param message    消息内容
     */
    void sendPrivateChatMessage(Long senderId, Long receiverId, String message);

    /**
     * 开始第二轮讨论
     */
    void startSecondDiscussion();

    /**
     * 单独验证答题环节
     *
     * @param gameId     游戏ID
     * @param playerIds  玩家ID列表
     * @param dmId       DM ID
     * @param judgeId    Judge ID
     * @return 验证结果，包含答案生成和评分信息
     */
    Map<String, Object> verifyAnswerPhase(Long gameId, List<Long> playerIds, Long dmId, Long judgeId);

    /**
     * 测试DM评分功能
     *
     * @param dmId    DM ID
     * @param answers 玩家答案列表
     * @return 评分结果，包含每个玩家的评分和评论
     */
    String testDMScore(Long dmId, List<Map<String, Object>> answers);

    /**
     * 停止讨论
     * <p>
     * 用于强制停止当前讨论，清理所有讨论相关的状态和资源。
     * 包括停止中央调度器、清理讨论状态映射、重置讨论完成标记、
     * 清理玩家相关状态以及取消计时器。
     * </p>
     *
     * @param gameId 游戏ID
     */
    void stopDiscussion(Long gameId);

    /**
     * 处理真人玩家投票
     * 当真人玩家通过WebSocket投票时调用
     *
     * @param playerId 玩家ID
     * @param answer   答案
     */
    void onRealPlayerVoteReceived(Long playerId, String answer);
}
