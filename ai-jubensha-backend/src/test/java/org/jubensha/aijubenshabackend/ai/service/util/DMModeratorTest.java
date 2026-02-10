package org.jubensha.aijubenshabackend.ai.service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.tools.SendDiscussionMessageTool;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.service.player.PlayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DMModeratorTest {

    @Mock
    private AIService aiService;

    @Mock
    private PlayerService playerService;

    @Mock
    private SendDiscussionMessageTool sendDiscussionMessageTool;

    @Mock
    private TurnManager turnManager;

    @Mock
    private DiscussionHelper discussionHelper;

    @Mock
    private DMAgent dmAgent;

    @InjectMocks
    private DMModerator dmModerator;

    private Long gameId;
    private List<Long> playerIds;
    private Long dmId;
    private List<Long> characterIds;
    private Long scriptId;

    @BeforeEach
    void setUp() {
        gameId = 1L;
        playerIds = new ArrayList<>();
        playerIds.add(101L);
        playerIds.add(102L);
        playerIds.add(103L);
        dmId = 201L;
        characterIds = new ArrayList<>();
        characterIds.add(301L);
        characterIds.add(302L);
        characterIds.add(303L);
        scriptId = 401L;
    }

    @Test
    void testStartDiscussion() throws InterruptedException {
        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.startDiscussion(anyString())).thenReturn("讨论开始消息");
        
        // 模拟玩家信息
        for (Long playerId : playerIds) {
            Player player = new Player();
            player.setId(playerId);
            player.setNickname("Player" + playerId);
            when(playerService.getPlayerById(playerId)).thenReturn(Optional.of(player));
        }

        // 执行测试
        dmModerator.startDiscussion(gameId, playerIds, dmId, characterIds, scriptId);

        // 等待异步执行完成
        Thread.sleep(1000);

        // 验证行为
        verify(turnManager).initializeGameTurn(gameId, playerIds, dmId);
        verify(dmAgent).startDiscussion(anyString());
        verify(sendDiscussionMessageTool).executeSendDiscussionMessage("讨论开始消息", gameId, dmId, playerIds);
        
        // 验证阶段转换（由于是异步执行，只验证初始化和第一个阶段切换）
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_STATEMENT);
    }

    @Test
    void testStartDiscussionWithNullDMAgent() throws InterruptedException {
        // 模拟DM Agent不存在
        when(aiService.getDMAgent(dmId)).thenReturn(null);

        // 执行测试
        dmModerator.startDiscussion(gameId, playerIds, dmId, characterIds, scriptId);

        // 等待异步执行完成
        Thread.sleep(1000);

        // 验证行为
        verify(turnManager).initializeGameTurn(gameId, playerIds, dmId);
        verify(sendDiscussionMessageTool, never()).executeSendDiscussionMessage(anyString(), anyLong(), anyLong(), anyList());
    }

    @Test
    void testHandlePrivateChatRequest() throws InterruptedException {
        // 模拟依赖行为
        when(turnManager.requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1))).thenReturn(true);

        // 执行测试
        dmModerator.handlePrivateChatRequest(gameId, playerIds.get(0), playerIds.get(1), "单聊请求消息");

        // 等待异步执行完成
        Thread.sleep(500);

        // 验证行为
        verify(turnManager).requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1));
    }

    @Test
    void testHandlePrivateChatRequestWithChatNotAllowed() throws InterruptedException {
        // 模拟单聊请求被拒绝
        when(turnManager.requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1))).thenReturn(false);

        // 执行测试
        dmModerator.handlePrivateChatRequest(gameId, playerIds.get(0), playerIds.get(1), "单聊请求消息");

        // 等待异步执行完成
        Thread.sleep(500);

        // 验证行为
        verify(turnManager).requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1));
        // 验证没有进一步的操作
        // 这里主要验证方法能够正常执行，不会抛出异常
    }

    @Test
    void testShutdown() {
        // 执行关闭方法
        dmModerator.shutdown();
        
        // 验证线程池被关闭（通过验证没有异常抛出）
        assertDoesNotThrow(() -> dmModerator.shutdown());
    }

    @Test
    void testStartDiscussionWithException() throws InterruptedException {
        // 模拟依赖行为抛出异常
        when(aiService.getDMAgent(dmId)).thenThrow(new RuntimeException("DMAgent error"));

        // 执行测试
        dmModerator.startDiscussion(gameId, playerIds, dmId, characterIds, scriptId);

        // 等待异步执行完成
        Thread.sleep(1000);

        // 验证方法能够正常执行（异常被捕获）
        // 这里主要验证方法不会因为异常而崩溃
        assertDoesNotThrow(() -> dmModerator.startDiscussion(gameId, playerIds, dmId, characterIds, scriptId));
    }

    @Test
    void testHandlePrivateChatRequestWithException() throws InterruptedException {
        // 模拟依赖行为抛出异常
        when(turnManager.requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1)))
                .thenThrow(new RuntimeException("TurnManager error"));

        // 执行测试
        dmModerator.handlePrivateChatRequest(gameId, playerIds.get(0), playerIds.get(1), "单聊请求消息");

        // 等待异步执行完成
        Thread.sleep(500);

        // 验证方法能够正常执行（异常被捕获）
        // 这里主要验证方法不会因为异常而崩溃
        assertDoesNotThrow(() -> dmModerator.handlePrivateChatRequest(gameId, playerIds.get(0), playerIds.get(1), "单聊请求消息"));
    }
}
