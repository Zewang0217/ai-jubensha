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
import org.jubensha.aijubenshabackend.ai.service.MemoryHierarchyService;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.ai.tools.GetDiscussionHistoryTool;
import org.jubensha.aijubenshabackend.ai.tools.GetPlayerStatusTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiscussionReasoningManagerTest {

    @Mock
    private AIService aiService;

    @Mock
    private GetDiscussionHistoryTool getDiscussionHistoryTool;

    @Mock
    private GetPlayerStatusTool getPlayerStatusTool;

    @Mock
    private TurnManager turnManager;

    @Mock
    private MessageAccumulator messageAccumulator;

    @Mock
    private MemoryHierarchyService memoryHierarchyService;

    @Mock
    private PlayerAgent playerAgent;

    @InjectMocks
    private DiscussionReasoningManager discussionReasoningManager;

    private Long gameId;
    private Long playerId;
    private String currentPhase;

    @BeforeEach
    void setUp() {
        gameId = 1L;
        playerId = 101L;
        currentPhase = "STATEMENT";
    }

    @Test
    void testProcessReasoningAndDiscussion() {
        // 模拟依赖行为
        when(turnManager.getCurrentPhase(gameId)).thenReturn(currentPhase);
        
        // 模拟记忆检索结果
        List<Map<String, Object>> memoryResults = new ArrayList<>();
        Map<String, Object> memory1 = new HashMap<>();
        memory1.put("content", "Test discussion content 1");
        memory1.put("player_name", "Player1");
        memory1.put("timestamp", System.currentTimeMillis());
        memoryResults.add(memory1);
        
        when(memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "讨论历史", 50))
                .thenReturn(memoryResults);
        
        // 模拟Player Agent
        when(aiService.getPlayerAgent(playerId)).thenReturn(playerAgent);
        when(playerAgent.reasonAndDiscuss(
                eq(gameId.toString()),
                eq(playerId.toString()),
                eq(currentPhase),
                anyString()
        )).thenReturn("Test reasoning result");

        // 执行测试
        String result = discussionReasoningManager.processReasoningAndDiscussion(gameId, playerId);

        // 验证结果
        assertEquals("Test reasoning result", result);
        verify(aiService).getPlayerAgent(playerId);
        verify(playerAgent).reasonAndDiscuss(
                eq(gameId.toString()),
                eq(playerId.toString()),
                eq(currentPhase),
                anyString()
        );
    }

    @Test
    void testProcessReasoningAndDiscussionWithEmptyMemory() {
        // 模拟依赖行为
        when(turnManager.getCurrentPhase(gameId)).thenReturn(currentPhase);
        
        // 模拟记忆检索返回空结果
        when(memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "讨论历史", 50))
                .thenReturn(new ArrayList<>());
        
        // 模拟讨论历史工具返回结果
        List<Map<String, Object>> historyResults = new ArrayList<>();
        Map<String, Object> history1 = new HashMap<>();
        history1.put("content", "Fallback discussion content");
        history1.put("player_name", "Player1");
        history1.put("timestamp", System.currentTimeMillis());
        historyResults.add(history1);
        
        when(getDiscussionHistoryTool.execute(gameId, 50)).thenReturn(historyResults);
        
        // 模拟Player Agent
        when(aiService.getPlayerAgent(playerId)).thenReturn(playerAgent);
        when(playerAgent.reasonAndDiscuss(
                eq(gameId.toString()),
                eq(playerId.toString()),
                eq(currentPhase),
                anyString()
        )).thenReturn("Fallback reasoning result");

        // 执行测试
        String result = discussionReasoningManager.processReasoningAndDiscussion(gameId, playerId);

        // 验证结果
        assertEquals("Fallback reasoning result", result);
        verify(getDiscussionHistoryTool).execute(gameId, 50);
    }

    @Test
    void testProcessReasoningAndDiscussionWithNullPlayerAgent() {
        // 模拟依赖行为
        when(turnManager.getCurrentPhase(gameId)).thenReturn(currentPhase);
        
        // 模拟记忆检索结果
        List<Map<String, Object>> memoryResults = new ArrayList<>();
        when(memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "讨论历史", 50))
                .thenReturn(memoryResults);
        
        // 模拟Player Agent不存在
        when(aiService.getPlayerAgent(playerId)).thenReturn(null);

        // 执行测试
        String result = discussionReasoningManager.processReasoningAndDiscussion(gameId, playerId);

        // 验证结果
        assertEquals("无法获取AI玩家实例", result);
    }

    @Test
    void testProcessReasoningAndDiscussionWithException() {
        // 模拟依赖行为
        when(turnManager.getCurrentPhase(gameId)).thenReturn(currentPhase);
        
        // 模拟记忆检索抛出异常
        when(memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "讨论历史", 50))
                .thenThrow(new RuntimeException("Memory retrieval error"));

        // 执行测试
        String result = discussionReasoningManager.processReasoningAndDiscussion(gameId, playerId);

        // 验证结果
        assertEquals("推理过程中出现错误", result);
    }

    @Test
    void testAnalyzeDiscussionTopic() {
        // 模拟讨论话题
        String topic = " murder case";
        
        // 模拟记忆检索结果
        List<Map<String, Object>> memoryResults = new ArrayList<>();
        Map<String, Object> memory1 = new HashMap<>();
        memory1.put("content", "Test topic content 1");
        memory1.put("player_name", "Player1");
        memory1.put("score", 0.8);
        memoryResults.add(memory1);
        
        when(memoryHierarchyService.multiLevelRetrieval(gameId, playerId, topic, 30))
                .thenReturn(memoryResults);
        
        // 模拟Player Agent
        when(aiService.getPlayerAgent(playerId)).thenReturn(playerAgent);
        when(playerAgent.analyzeTopic(
                gameId.toString(),
                playerId.toString(),
                topic
        )).thenReturn("Test topic analysis result");

        // 执行测试
        String result = discussionReasoningManager.analyzeDiscussionTopic(gameId, playerId, topic);

        // 验证结果
        assertEquals("Test topic analysis result", result);
        verify(playerAgent).analyzeTopic(
                gameId.toString(),
                playerId.toString(),
                topic
        );
    }

    @Test
    void testDecidePrivateChat() {
        // 模拟单聊决策
        when(turnManager.requestPrivateChat(gameId, playerId, playerId)).thenReturn(true);
        
        // 模拟记忆检索结果
        List<Map<String, Object>> memoryResults = new ArrayList<>();
        Map<String, Object> memory1 = new HashMap<>();
        memory1.put("content", "Test player info 1");
        memory1.put("player_name", "Player2");
        memory1.put("score", 0.7);
        memoryResults.add(memory1);
        
        when(memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "其他玩家", 30))
                .thenReturn(memoryResults);
        
        // 模拟Player Agent
        when(aiService.getPlayerAgent(playerId)).thenReturn(playerAgent);
        when(playerAgent.decidePrivateChat(
                gameId.toString(),
                playerId.toString()
        )).thenReturn("Test private chat decision");

        // 执行测试
        String result = discussionReasoningManager.decidePrivateChat(gameId, playerId);

        // 验证结果
        assertEquals("Test private chat decision", result);
        verify(playerAgent).decidePrivateChat(
                gameId.toString(),
                playerId.toString()
        );
    }

    @Test
    void testDecidePrivateChatWithChatNotAllowed() {
        // 模拟单聊不被允许
        when(turnManager.requestPrivateChat(gameId, playerId, playerId)).thenReturn(false);

        // 执行测试
        String result = discussionReasoningManager.decidePrivateChat(gameId, playerId);

        // 验证结果
        assertEquals("当前阶段或次数限制不允许发起单聊", result);
    }

    @Test
    void testProcessReasoningAsync() {
        // 模拟同步推理方法的行为
        when(turnManager.getCurrentPhase(gameId)).thenReturn(currentPhase);
        
        List<Map<String, Object>> memoryResults = new ArrayList<>();
        when(memoryHierarchyService.multiLevelRetrieval(gameId, playerId, "讨论历史", 50))
                .thenReturn(memoryResults);
        
        when(aiService.getPlayerAgent(playerId)).thenReturn(playerAgent);
        when(playerAgent.reasonAndDiscuss(
                eq(gameId.toString()),
                eq(playerId.toString()),
                eq(currentPhase),
                anyString()
        )).thenReturn("Async reasoning result");

        // 执行异步测试
        CompletableFuture<String> future = discussionReasoningManager.processReasoningAsync(gameId, playerId);
        String result = future.join();

        // 验证结果
        assertEquals("Async reasoning result", result);
    }

    @Test
    void testShutdown() {
        // 执行关闭方法
        discussionReasoningManager.shutdown();
        
        // 验证线程池被关闭（通过验证没有异常抛出）
        assertDoesNotThrow(() -> discussionReasoningManager.shutdown());
    }
}
