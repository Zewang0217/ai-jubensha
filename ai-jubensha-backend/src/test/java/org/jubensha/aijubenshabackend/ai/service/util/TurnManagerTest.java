package org.jubensha.aijubenshabackend.ai.service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.jubensha.aijubenshabackend.ai.service.TimerService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TurnManagerTest {

    @Mock
    private TimerService timerService;

    @InjectMocks
    private TurnManager turnManager;

    private Long gameId;
    private List<Long> playerIds;
    private Long dmId;

    @BeforeEach
    void setUp() {
        gameId = 1L;
        playerIds = new ArrayList<>();
        playerIds.add(101L);
        playerIds.add(102L);
        playerIds.add(103L);
        dmId = 201L;
    }

    @Test
    void testInitializeGameTurn() {
        // 执行初始化
        turnManager.initializeGameTurn(gameId, playerIds, dmId);

        // 验证初始化后的状态
        String currentPhase = turnManager.getCurrentPhase(gameId);
        assertEquals(TurnManager.PHASE_STATEMENT, currentPhase);

        // 验证单聊次数初始化为0
        for (Long playerId : playerIds) {
            int chatCount = turnManager.getPrivateChatCount(gameId, playerId);
            assertEquals(0, chatCount);
        }
    }

    @Test
    void testCurrentSpeaker() {
        // 初始化游戏状态
        turnManager.initializeGameTurn(gameId, playerIds, dmId);

        // 测试第一个发言玩家
        Long firstSpeaker = turnManager.getCurrentSpeaker(gameId);
        assertEquals(playerIds.get(0), firstSpeaker);

        // 结束当前回合，移动到下一个玩家
        Long nextSpeaker = turnManager.endCurrentTurn(gameId);
        assertEquals(playerIds.get(1), nextSpeaker);

        // 再次结束回合
        nextSpeaker = turnManager.endCurrentTurn(gameId);
        assertEquals(playerIds.get(2), nextSpeaker);

        // 所有玩家发言完毕，进入自由讨论阶段
        nextSpeaker = turnManager.endCurrentTurn(gameId);
        assertNull(nextSpeaker);
        assertEquals(TurnManager.PHASE_FREE_DISCUSSION, turnManager.getCurrentPhase(gameId));
    }

    @Test
    void testSwitchPhase() {
        // 初始化游戏状态
        turnManager.initializeGameTurn(gameId, playerIds, dmId);

        // 切换到自由讨论阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_FREE_DISCUSSION);
        assertEquals(TurnManager.PHASE_FREE_DISCUSSION, turnManager.getCurrentPhase(gameId));

        // 切换到单聊阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_PRIVATE_CHAT);
        assertEquals(TurnManager.PHASE_PRIVATE_CHAT, turnManager.getCurrentPhase(gameId));

        // 切换到答题阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_ANSWER);
        assertEquals(TurnManager.PHASE_ANSWER, turnManager.getCurrentPhase(gameId));

        // 切换回陈述阶段，应该重置发言顺序
        turnManager.switchPhase(gameId, TurnManager.PHASE_STATEMENT);
        assertEquals(TurnManager.PHASE_STATEMENT, turnManager.getCurrentPhase(gameId));
        Long currentSpeaker = turnManager.getCurrentSpeaker(gameId);
        assertEquals(playerIds.get(0), currentSpeaker);
    }

    @Test
    void testRequestPrivateChat() {
        // 初始化游戏状态
        turnManager.initializeGameTurn(gameId, playerIds, dmId);

        // 切换到自由讨论阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_FREE_DISCUSSION);

        Long senderId = playerIds.get(0);
        Long receiverId = playerIds.get(1);

        // 第一次发起单聊，应该允许
        boolean firstRequest = turnManager.requestPrivateChat(gameId, senderId, receiverId);
        assertTrue(firstRequest);
        assertEquals(1, turnManager.getPrivateChatCount(gameId, senderId));

        // 第二次发起单聊，应该允许
        boolean secondRequest = turnManager.requestPrivateChat(gameId, senderId, playerIds.get(2));
        assertTrue(secondRequest);
        assertEquals(2, turnManager.getPrivateChatCount(gameId, senderId));

        // 第三次发起单聊，应该被拒绝
        boolean thirdRequest = turnManager.requestPrivateChat(gameId, senderId, receiverId);
        assertFalse(thirdRequest);
        assertEquals(2, turnManager.getPrivateChatCount(gameId, senderId));
    }

    @Test
    void testRequestPrivateChatInWrongPhase() {
        // 初始化游戏状态（默认是陈述阶段）
        turnManager.initializeGameTurn(gameId, playerIds, dmId);

        Long senderId = playerIds.get(0);
        Long receiverId = playerIds.get(1);

        // 在陈述阶段发起单聊，应该被拒绝
        boolean request = turnManager.requestPrivateChat(gameId, senderId, receiverId);
        assertFalse(request);
        assertEquals(0, turnManager.getPrivateChatCount(gameId, senderId));
    }

    @Test
    void testCleanup() {
        // 初始化游戏状态
        turnManager.initializeGameTurn(gameId, playerIds, dmId);

        // 验证状态存在
        String currentPhase = turnManager.getCurrentPhase(gameId);
        assertNotNull(currentPhase);

        // 清理状态
        turnManager.cleanup(gameId);

        // 验证状态已被清理
        currentPhase = turnManager.getCurrentPhase(gameId);
        assertNull(currentPhase);
    }

    @Test
    void testStartCurrentTurn() {
        // 初始化游戏状态
        turnManager.initializeGameTurn(gameId, playerIds, dmId);

        // 模拟回调
        Runnable callback = mock(Runnable.class);

        // 开始当前回合
        turnManager.startCurrentTurn(gameId, 300L, callback);

        // 验证TimerService被调用
        verify(timerService).startTimer(eq("TURN_" + gameId + "_" + playerIds.get(0)), eq(300L), any(Runnable.class));
    }
}
