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
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
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
public class DiscussionFlowIntegrationTest {

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

    @Mock
    private PlayerAgent playerAgent1;

    @Mock
    private PlayerAgent playerAgent2;

    @Mock
    private PlayerAgent playerAgent3;

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

        // 模拟Agent获取
        when(aiService.getDMAgent(dmId)).thenReturn(dmAgent);
        when(aiService.getJudgeAgent(judgeId)).thenReturn(judgeAgent);
        when(aiService.getPlayerAgent(101L)).thenReturn(playerAgent1);
        when(aiService.getPlayerAgent(102L)).thenReturn(playerAgent2);
        when(aiService.getPlayerAgent(103L)).thenReturn(playerAgent3);

        // 模拟DM行为
        when(dmAgent.startDiscussion(anyString())).thenReturn("讨论开始");
        when(dmAgent.moderateDiscussion(anyString())).thenReturn("阶段引导消息");
        when(dmAgent.scoreAnswers(anyList())).thenReturn("评分结果");

        // 模拟Judge行为
        when(judgeAgent.monitorDiscussion(anyString())).thenReturn(true);
        when(judgeAgent.summarizeDiscussion(anyString())).thenReturn("讨论总结");

        // 模拟Player行为
        when(playerAgent1.reasonAndDiscuss(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("玩家1的陈述");
        when(playerAgent2.reasonAndDiscuss(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("玩家2的陈述");
        when(playerAgent3.reasonAndDiscuss(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("玩家3的陈述");

        when(playerAgent1.decidePrivateChat(anyString(), anyString()))
                .thenReturn("玩家1的单聊决策");
        when(playerAgent2.decidePrivateChat(anyString(), anyString()))
                .thenReturn("玩家2的单聊决策");
        when(playerAgent3.decidePrivateChat(anyString(), anyString()))
                .thenReturn("玩家3的单聊决策");

        // 模拟推理管理器行为
        when(discussionReasoningManager.processReasoningAndDiscussion(anyLong(), anyLong()))
                .thenReturn("AI推理结果");
        when(discussionReasoningManager.processReasoningAsync(anyLong(), anyLong()))
                .thenReturn(CompletableFuture.completedFuture("异步推理结果"));
        when(discussionReasoningManager.decidePrivateChat(anyLong(), anyLong()))
                .thenReturn("单聊决策结果");

        // 模拟TurnManager行为
        when(turnManager.getCurrentPhase(anyLong())).thenReturn(TurnManager.PHASE_STATEMENT);
        when(turnManager.requestPrivateChat(anyLong(), anyLong(), anyLong())).thenReturn(true);
    }

    @Test
    void testCompleteDiscussionFlow() {
        // 1. 开始讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 验证初始化
        Map<String, Object> initialState = discussionService.getDiscussionState();
        assertEquals("INITIALIZED", initialState.get("currentPhase"));
        assertEquals(1, initialState.get("discussionRound"));

        // 2. 开始陈述阶段
        discussionService.startStatementPhase();

        // 验证陈述阶段状态
        Map<String, Object> statementState = discussionService.getDiscussionState();
        assertEquals("STATEMENT", statementState.get("currentPhase"));

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_STATEMENT);
        verify(dmAgent).moderateDiscussion("开始陈述阶段，每位玩家有5分钟时间");
        verify(timerService).startTimer(eq("STATEMENT"), eq(300L), any(Runnable.class));

        // 3. 开始自由讨论阶段
        discussionService.startFreeDiscussionPhase();

        // 验证自由讨论阶段状态
        Map<String, Object> freeDiscussionState = discussionService.getDiscussionState();
        assertEquals("FREE_DISCUSSION", freeDiscussionState.get("currentPhase"));

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_FREE_DISCUSSION);
        verify(dmAgent).moderateDiscussion("开始自由讨论阶段，大家可以畅所欲言");
        verify(timerService).startTimer(eq("FREE_DISCUSSION"), eq(1800L), any(Runnable.class));

        // 4. 发送讨论消息
        discussionService.sendDiscussionMessage(playerIds.get(0), "测试讨论消息");

        // 验证行为
        verify(messageQueueService).sendDiscussionMessage(eq("测试讨论消息"), eq(playerIds));
        verify(judgeAgent).monitorDiscussion(eq("测试讨论消息"));

        // 5. 发送单聊邀请
        discussionService.sendPrivateChatInvitation(playerIds.get(0), playerIds.get(1));

        // 验证行为
        verify(turnManager).requestPrivateChat(gameId, playerIds.get(0), playerIds.get(1));
        verify(messageQueueService).sendPrivateChatMessage("单聊邀请", playerIds.get(0), playerIds.get(1));

        // 6. 开始单聊阶段
        discussionService.startPrivateChatPhase();

        // 验证单聊阶段状态
        Map<String, Object> privateChatState = discussionService.getDiscussionState();
        assertEquals("PRIVATE_CHAT", privateChatState.get("currentPhase"));

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_PRIVATE_CHAT);
        verify(dmAgent).moderateDiscussion("开始单聊阶段，每位玩家有2次单聊机会，每次3分钟");
        verify(timerService).startTimer(eq("PRIVATE_CHAT"), eq(1200L), any(Runnable.class));

        // 7. 发送单聊消息
        discussionService.sendPrivateChatMessage(playerIds.get(0), playerIds.get(1), "测试单聊消息");

        // 验证行为
        verify(messageQueueService).sendPrivateChatMessage("测试单聊消息", playerIds.get(0), playerIds.get(1));

        // 8. 开始答题阶段
        discussionService.startAnswerPhase();

        // 验证答题阶段状态
        Map<String, Object> answerState = discussionService.getDiscussionState();
        assertEquals("ANSWER", answerState.get("currentPhase"));

        // 验证行为
        verify(turnManager).switchPhase(gameId, TurnManager.PHASE_ANSWER);
        verify(dmAgent).moderateDiscussion("开始答题阶段，请每位玩家给出你的答案");
        verify(timerService).startTimer(eq("ANSWER"), eq(600L), any(Runnable.class));

        // 9. 提交答案
        discussionService.submitAnswer(playerIds.get(0), "答案1");
        discussionService.submitAnswer(playerIds.get(1), "答案2");
        discussionService.submitAnswer(playerIds.get(2), "答案3");

        // 验证行为
        verify(messageQueueService, times(3)).sendAnswerMessage(anyString(), anyLong(), eq(dmId));

        // 10. 结束讨论
        Map<String, Object> endResult = discussionService.endDiscussion();

        // 验证结束讨论状态
        assertNotNull(endResult);
        assertNotNull(endResult.get("endTime"));
        assertNotNull(endResult.get("playerAnswers"));
        assertNotNull(endResult.get("scoreResponse"));
        assertNotNull(endResult.get("judgeSummary"));

        // 验证行为
        verify(dmAgent).scoreAnswers(anyList());
        verify(judgeAgent).summarizeDiscussion(anyString());
    }

    @Test
    void testTwoRoundDiscussionFlow() {
        // 1. 开始第一轮讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 2. 完成第一轮讨论流程
        discussionService.startStatementPhase();
        discussionService.startFreeDiscussionPhase();
        discussionService.startPrivateChatPhase();
        discussionService.startAnswerPhase();
        discussionService.submitAnswer(playerIds.get(0), "答案1");
        discussionService.endDiscussion();

        // 3. 开始第二轮讨论
        discussionService.startSecondDiscussion();

        // 验证第二轮讨论状态
        Map<String, Object> secondRoundState = discussionService.getDiscussionState();
        assertEquals(2, secondRoundState.get("discussionRound"));

        // 验证行为
        verify(dmAgent).startDiscussion(anyString());

        // 4. 完成第二轮讨论流程
        discussionService.startFreeDiscussionPhase();
        discussionService.startPrivateChatPhase();
        discussionService.startAnswerPhase();
        discussionService.submitAnswer(playerIds.get(0), "第二轮答案1");
        discussionService.endDiscussion();

        // 验证第二轮讨论完成
        verify(dmAgent, times(2)).scoreAnswers(anyList());
        verify(judgeAgent, times(2)).summarizeDiscussion(anyString());
    }

    @Test
    void testDiscussionWithMessageSending() {
        // 开始讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 发送多条讨论消息
        String[] messages = {
                "大家好，我是玩家1",
                "我发现了一个重要线索",
                "让我分析一下时间线",
                "我认为凶手是..."
        };

        for (int i = 0; i < messages.length; i++) {
            long senderId = playerIds.get(i % playerIds.size());
            discussionService.sendDiscussionMessage(senderId, messages[i]);
        }

        // 验证消息发送行为
        verify(messageQueueService, times(messages.length)).sendDiscussionMessage(anyString(), eq(playerIds));
        verify(judgeAgent, times(messages.length)).monitorDiscussion(anyString());
    }

    @Test
    void testDiscussionWithPrivateChats() {
        // 开始讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 开始自由讨论阶段（单聊只能在自由讨论阶段发起）
        discussionService.startFreeDiscussionPhase();

        // 发起多次单聊
        discussionService.sendPrivateChatInvitation(playerIds.get(0), playerIds.get(1));
        discussionService.sendPrivateChatInvitation(playerIds.get(0), playerIds.get(2));
        discussionService.sendPrivateChatInvitation(playerIds.get(1), playerIds.get(0));

        // 验证单聊邀请行为
        verify(turnManager, times(3)).requestPrivateChat(anyLong(), anyLong(), anyLong());
        verify(messageQueueService, times(3)).sendPrivateChatMessage(eq("单聊邀请"), anyLong(), anyLong());
        verify(timerService, times(3)).startTimer(anyString(), eq(180L), any(Runnable.class));

        // 发送单聊消息
        discussionService.sendPrivateChatMessage(playerIds.get(0), playerIds.get(1), "私下告诉你一个秘密");
        discussionService.sendPrivateChatMessage(playerIds.get(1), playerIds.get(0), "我也有一个线索");

        // 验证单聊消息发送行为（包括邀请和消息）
        verify(messageQueueService, atLeast(2)).sendPrivateChatMessage(anyString(), anyLong(), anyLong());
    }

    @Test
    void testDiscussionStateManagement() {
        // 开始讨论
        discussionService.startDiscussion(gameId, playerIds, dmId, judgeId);

        // 验证初始状态
        Map<String, Object> state1 = discussionService.getDiscussionState();
        assertEquals(gameId, state1.get("gameId"));
        assertEquals(playerIds, state1.get("playerIds"));
        assertEquals(dmId, state1.get("dmId"));
        assertEquals(judgeId, state1.get("judgeId"));
        assertEquals(1, state1.get("discussionRound"));
        assertEquals("INITIALIZED", state1.get("currentPhase"));

        // 切换到陈述阶段
        discussionService.startStatementPhase();
        Map<String, Object> state2 = discussionService.getDiscussionState();
        assertEquals("STATEMENT", state2.get("currentPhase"));

        // 切换到自由讨论阶段
        discussionService.startFreeDiscussionPhase();
        Map<String, Object> state3 = discussionService.getDiscussionState();
        assertEquals("FREE_DISCUSSION", state3.get("currentPhase"));

        // 切换到单聊阶段
        discussionService.startPrivateChatPhase();
        Map<String, Object> state4 = discussionService.getDiscussionState();
        assertEquals("PRIVATE_CHAT", state4.get("currentPhase"));

        // 切换到答题阶段
        discussionService.startAnswerPhase();
        Map<String, Object> state5 = discussionService.getDiscussionState();
        assertEquals("ANSWER", state5.get("currentPhase"));

        // 提交答案
        discussionService.submitAnswer(playerIds.get(0), "答案1");
        Map<String, Object> state6 = discussionService.getDiscussionState();
        // playerAnswers只在endDiscussion()时添加到结果中，不在discussionState中

        // 结束讨论
        Map<String, Object> state7 = discussionService.endDiscussion();
        assertNotNull(state7.get("endTime"));
        assertNotNull(state7.get("scoreResponse"));
        assertNotNull(state7.get("judgeSummary"));
    }
}
