package org.jubensha.aijubenshabackend.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.ai.service.util.DMModerator;
import org.jubensha.aijubenshabackend.ai.service.util.DiscussionHelper;
import org.jubensha.aijubenshabackend.ai.service.util.DiscussionReasoningManager;
import org.jubensha.aijubenshabackend.ai.service.util.MessageAccumulator;
import org.jubensha.aijubenshabackend.ai.service.util.TurnManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiscussionServiceImplTest {

    @Mock
    private AIService aiService;

    @Mock
    private MessageQueueService messageQueueService;

    @Mock
    private TimerService timerService;

    @Mock
    private MessageAccumulator messageAccumulator;

    @Mock
    private TurnManager turnManager;

    @Mock
    private DiscussionHelper discussionHelper;

    @Mock
    private DMModerator dmModerator;

    @Mock
    private DiscussionReasoningManager discussionReasoningManager;

    @Mock
    private DMAgent dmAgent;

    @Mock
    private JudgeAgent judgeAgent;

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    private Long gameId;
    private List<Long> playerIds;
    private Long dmId;
    private Long judgeId;

    @BeforeEach
    void setUp() {
        gameId = 1L;
        playerIds = new ArrayList<>();
        playerIds.add(101L);
        playerIds.add(102L);
        playerIds.add(103L);
        dmId = 201L;
        judgeId = 202L;
    }

    @Test
    void testStartDiscussion() {
        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.startDiscussion(anyString())).thenReturn("讨论开始消息");

        // 执行测试
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 验证行为
        verify(dmModerator).startDiscussion(eq(gameId), eq(playerIds), eq(dmId), anyList(), anyLong());

        // 验证状态
        Map<String, Object> discussionState = discussionService.getDiscussionState();
        assertEquals(gameId, discussionState.get("gameId"));
        assertEquals(playerIds, discussionState.get("playerIds"));
        assertEquals(dmId, discussionState.get("dmId"));
        assertEquals(judgeId, discussionState.get("judgeId"));
        assertEquals(1, discussionState.get("discussionRound"));
        assertEquals("INITIALIZED", discussionState.get("currentPhase"));
    }

    @Test
    void testStartStatementPhase() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.moderateDiscussion(anyString())).thenReturn("陈述阶段开始");
        when(discussionReasoningManager.processReasoningAndDiscussion(eq(gameId), anyLong()))
                .thenReturn("AI玩家陈述内容");

        // 执行测试
        discussionService.startStatementPhase();

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_STATEMENT);
        verify(dmAgent).moderateDiscussion("开始陈述阶段，每位玩家有5分钟时间");
        verify(timerService).startTimer(eq("STATEMENT"), eq(300L), any(Runnable.class));

        // 验证状态
        Map<String, Object> discussionState = discussionService.getDiscussionState();
        assertEquals("STATEMENT", discussionState.get("currentPhase"));
    }

    @Test
    void testStartFreeDiscussionPhase() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.moderateDiscussion(anyString())).thenReturn("自由讨论阶段开始");
        when(discussionReasoningManager.processReasoningAsync(eq(gameId), anyLong()))
                .thenReturn(CompletableFuture.completedFuture("AI玩家自由讨论内容"));

        // 执行测试
        discussionService.startFreeDiscussionPhase();

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_FREE_DISCUSSION);
        verify(dmAgent).moderateDiscussion("开始自由讨论阶段，大家可以畅所欲言");
        verify(timerService).startTimer(eq("FREE_DISCUSSION"), eq(1800L), any(Runnable.class));

        // 验证状态
        Map<String, Object> discussionState = discussionService.getDiscussionState();
        assertEquals("FREE_DISCUSSION", discussionState.get("currentPhase"));
    }

    @Test
    void testStartPrivateChatPhase() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.moderateDiscussion(anyString())).thenReturn("单聊阶段开始");
        when(discussionReasoningManager.decidePrivateChat(eq(gameId), anyLong()))
                .thenReturn("AI玩家单聊决策");

        // 执行测试
        discussionService.startPrivateChatPhase();

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_PRIVATE_CHAT);
        verify(dmAgent).moderateDiscussion("开始单聊阶段，每位玩家有2次单聊机会，每次3分钟");
        verify(timerService).startTimer(eq("PRIVATE_CHAT"), eq(1200L), any(Runnable.class));

        // 验证状态
        Map<String, Object> discussionState = discussionService.getDiscussionState();
        assertEquals("PRIVATE_CHAT", discussionState.get("currentPhase"));
    }

    @Test
    void testStartAnswerPhase() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.moderateDiscussion(anyString())).thenReturn("答题阶段开始");

        // 执行测试
        discussionService.startAnswerPhase();

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_ANSWER);
        verify(dmAgent).moderateDiscussion("开始答题阶段，请每位玩家给出你的答案");
        verify(timerService).startTimer(eq("ANSWER"), eq(600L), any(Runnable.class));

        // 验证状态
        Map<String, Object> discussionState = discussionService.getDiscussionState();
        assertEquals("ANSWER", discussionState.get("currentPhase"));
    }

    @Test
    void testSendPrivateChatInvitation() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(turnManager.requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1))).thenReturn(true);

        // 执行测试
        discussionService.sendPrivateChatInvitation(playerIds.get(0), playerIds.get(1));

        // 验证行为
        verify(turnManager).requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1));
        verify(messageQueueService).sendPrivateChatMessage("单聊邀请", playerIds.get(0), playerIds.get(1));
        verify(timerService).startTimer(eq("PRIVATE_CHAT_" + playerIds.get(0) + "_" + playerIds.get(1)), eq(180L), any(Runnable.class));
    }

    @Test
    void testSendPrivateChatInvitationWithChatNotAllowed() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(turnManager.requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1))).thenReturn(false);

        // 执行测试
        discussionService.sendPrivateChatInvitation(playerIds.get(0), playerIds.get(1));

        // 验证行为
        verify(turnManager).requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1));
        verify(messageQueueService, never()).sendPrivateChatMessage(anyString(), anyLong(), anyLong());
        verify(timerService, never()).startTimer(anyString(), anyLong(), any(Runnable.class));
    }

    @Test
    void testSubmitAnswer() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);

        // 执行测试
        String answer = "这是一个测试答案";
        discussionService.submitAnswer(playerIds.get(0), answer);

        // 验证行为
        verify(messageQueueService).sendAnswerMessage(answer, playerIds.get(0), dmId);
    }

    @Test
    void testEndDiscussion() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 提交答案
        discussionService.submitAnswer(playerIds.get(0), "答案1");
        discussionService.submitAnswer(playerIds.get(1), "答案2");

        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.scoreAnswers(anyList())).thenReturn("评分结果");
        when(aiService.getJudgeAgent(judgeId)).thenReturn(judgeAgent);
        when(judgeAgent.summarizeDiscussion(anyString())).thenReturn("讨论总结");

        // 执行测试
        Map<String, Object> result = discussionService.endDiscussion();

        // 验证行为
        verify(dmAgent).scoreAnswers(anyList());
        verify(judgeAgent).summarizeDiscussion(anyString());

        // 验证结果
        assertNotNull(result);
        assertEquals("评分结果", result.get("scoreResponse"));
        assertEquals("讨论总结", result.get("judgeSummary"));
    }

    @Test
    void testSendDiscussionMessage() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(aiService.getJudgeAgent(judgeId)).thenReturn(judgeAgent);
        when(judgeAgent.monitorDiscussion(anyString())).thenReturn(true);

        // 执行测试
        String message = "测试讨论消息";
        discussionService.sendDiscussionMessage(playerIds.get(0), message);

        // 验证行为
        verify(messageQueueService).sendDiscussionMessage(message, playerIds);
        verify(judgeAgent).monitorDiscussion(message);
    }

    @Test
    void testSendPrivateChatMessage() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 执行测试
        String message = "测试单聊消息";
        discussionService.sendPrivateChatMessage(playerIds.get(0), playerIds.get(1), message);

        // 验证行为
        verify(messageQueueService).sendPrivateChatMessage(message, playerIds.get(0), playerIds.get(1));
    }

    @Test
    void testStartSecondDiscussion() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 模拟依赖行为
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(dmAgent.startDiscussion(anyString())).thenReturn("第二轮讨论开始");

        // 执行测试
        discussionService.startSecondDiscussion();

        // 验证行为
        verify(dmAgent).startDiscussion(anyString());

        // 验证状态
        Map<String, Object> discussionState = discussionService.getDiscussionState();
        assertEquals(2, discussionState.get("discussionRound"));
    }

    @Test
    void testGetDiscussionState() {
        // 初始化讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 执行测试
        Map<String, Object> state = discussionService.getDiscussionState();

        // 验证结果
        assertNotNull(state);
        assertEquals(gameId, state.get("gameId"));
        assertEquals(playerIds, state.get("playerIds"));
        assertEquals(dmId, state.get("dmId"));
        assertEquals(judgeId, state.get("judgeId"));
    }
}
