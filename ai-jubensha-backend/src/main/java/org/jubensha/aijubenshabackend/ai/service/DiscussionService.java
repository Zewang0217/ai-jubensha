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
}
