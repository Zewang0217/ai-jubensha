package org.jubensha.aijubenshabackend.core.config.ai;


import lombok.extern.slf4j.Slf4j;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import org.jubensha.aijubenshabackend.ai.service.AIService;
import org.jubensha.aijubenshabackend.ai.service.ScriptGenerateService;
import org.jubensha.aijubenshabackend.ai.service.agent.DMAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.JudgeAgent;
import org.jubensha.aijubenshabackend.ai.service.agent.PlayerAgent;
import org.jubensha.aijubenshabackend.models.entity.Player;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.service.player.PlayerService;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Mock AI 配置类
 * 在 mock-ai profile 下，所有大模型调用将被拦截并返回模拟数据
 * 用于测试 WebSocket 通信的前后端交互，避免大模型 token 消耗
 *
 * @author Zewang
 * @version 2.0
 * @date 2026-03-04
 * @since 2026
 */

@Slf4j
@Configuration
@Profile("mock-ai")
public class MockAiConfig {

    private final AtomicLong responseCounter = new AtomicLong(0);

    /**
     * Mock AIService
     * 提供完整的 AI 服务 Mock 实现
     *
     * @param playerService 玩家服务，用于持久化玩家数据
     * @return Mock 的 AIService
     */
    @Bean
    @Primary
    public AIService mockAiService(PlayerService playerService) {
        log.warn("=======================================================");
        log.warn("警告：当前系统运行在 Mock AI 模式！所有大模型调用将被拦截并返回假数据！");
        log.warn("=======================================================");

        AIService mockAiService = Mockito.mock(AIService.class);

        // ========== 1. Mock DMAgent ==========
        DMAgent mockDmAgent = createMockDmAgent();

        // ========== 2. Mock PlayerAgent ==========
        PlayerAgent mockPlayerAgent = createMockPlayerAgent();

        // ========== 3. Mock JudgeAgent ==========
        JudgeAgent mockJudgeAgent = createMockJudgeAgent();

        // ========== 4. Mock AIService 方法 ==========

        // 创建AI玩家 - 使用 PlayerService 持久化
        Mockito.when(mockAiService.createAIPlayer(anyString()))
            .thenAnswer(invocation -> {
                String name = invocation.getArgument(0);
                Player player = new Player();
                player.setNickname(name);
                player.setUsername(name);
                player.setEmail(name + "@mock-ai.example.com");
                player.setPassword("mock-password");
                player.setStatus(PlayerStatus.ONLINE);
                player.setRole(PlayerRole.USER);
                Player savedPlayer = playerService.createPlayer(player);
                log.info("[Mock AI] 创建AI玩家: {} (ID: {})", name, savedPlayer.getId());
                return savedPlayer;
            });

        // 创建DM Agent - 使用 PlayerService 持久化
        Mockito.when(mockAiService.createDMAgent())
            .thenAnswer(invocation -> {
                Optional<Player> existingDM = playerService.getPlayerByUsername("DM");
                if (existingDM.isPresent()) {
                    log.info("[Mock AI] 使用已存在的DM玩家，ID: {}", existingDM.get().getId());
                    return existingDM.get();
                }
                Player dm = new Player();
                dm.setNickname("DM");
                dm.setUsername("DM");
                dm.setEmail("DM@mock-ai.example.com");
                dm.setPassword("mock-password");
                dm.setStatus(PlayerStatus.ONLINE);
                dm.setRole(PlayerRole.DM);
                Player savedDM = playerService.createPlayer(dm);
                log.info("[Mock AI] 创建DM Agent，ID: {}", savedDM.getId());
                return savedDM;
            });

        // 创建Judge Agent - 使用 PlayerService 持久化
        Mockito.when(mockAiService.createJudgeAgent())
            .thenAnswer(invocation -> {
                Optional<Player> existingJudge = playerService.getPlayerByUsername("Judge");
                if (existingJudge.isPresent()) {
                    log.info("[Mock AI] 使用已存在的Judge玩家，ID: {}", existingJudge.get().getId());
                    return existingJudge.get();
                }
                Player judge = new Player();
                judge.setNickname("Judge");
                judge.setUsername("Judge");
                judge.setEmail("Judge@mock-ai.example.com");
                judge.setPassword("mock-password");
                judge.setStatus(PlayerStatus.ONLINE);
                judge.setRole(PlayerRole.USER);
                Player savedJudge = playerService.createPlayer(judge);
                log.info("[Mock AI] 创建Judge Agent，ID: {}", savedJudge.getId());
                return savedJudge;
            });

        // 创建Player Agent
        Mockito.doNothing().when(mockAiService).createPlayerAgent(anyLong(), anyLong());

        // 获取DM Agent
        Mockito.when(mockAiService.getDMAgent(anyLong())).thenReturn(mockDmAgent);

        // 获取Judge Agent
        Mockito.when(mockAiService.getJudgeAgent(anyLong())).thenReturn(mockJudgeAgent);

        // 获取Player Agent
        Mockito.when(mockAiService.getPlayerAgent(anyLong())).thenReturn(mockPlayerAgent);

        // 获取无工具的Player Agent
        Mockito.when(mockAiService.getPlayerAgentWithoutTools(anyLong(), anyLong())).thenReturn(mockPlayerAgent);

        // 批量获取Player Agent
        Mockito.when(mockAiService.getPlayerAgents(anyList()))
            .thenAnswer(invocation -> {
                List<Long> playerIds = invocation.getArgument(0);
                Map<Long, PlayerAgent> agents = new HashMap<>();
                for (Long playerId : playerIds) {
                    agents.put(playerId, mockPlayerAgent);
                }
                return agents;
            });

        // 预创建Player Agent
        Mockito.doNothing().when(mockAiService).preCreatePlayerAgent(anyLong(), anyLong());

        // 通知AI玩家读取剧本
        Mockito.doNothing().when(mockAiService).notifyAIPlayerReadScript(anyLong(), anyLong());

        // 通知AI玩家开始搜证
        Mockito.doNothing().when(mockAiService).notifyAIPlayerStartInvestigation(anyLong(), anyList());

        // 让AI玩家参与讨论，传递角色信息
        Mockito.when(mockAiService.discussWithCharacterInfo(anyLong(), anyLong(), anyLong(), anyString()))
            .thenReturn("{\"content\": \"【Mock AI玩家】基于我的角色信息，我认为凶手一定是与死者有深仇大恨的人！\"}");

        return mockAiService;
    }

    /**
     * 创建 Mock PlayerAgent
     * 覆盖所有 PlayerAgent 接口方法
     *
     * @return Mock 的 PlayerAgent
     */
    private PlayerAgent createMockPlayerAgent() {
        PlayerAgent mockPlayerAgent = Mockito.mock(PlayerAgent.class);

        // speak - 基础发言
        Mockito.when(mockPlayerAgent.speak(anyString(), anyString()))
            .thenAnswer(invocation -> {
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock发言%d】我经过分析，认为需要进一步调查案发现场的线索。\"}",
                    count
                );
            });

        // respondToClue - 回应线索
        Mockito.when(mockPlayerAgent.respondToClue(anyString(), anyString()))
            .thenAnswer(invocation -> {
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock回应线索%d】这个线索很关键，我认为需要进一步分析。\"}",
                    count
                );
            });

        // discuss - 讨论话题
        Mockito.when(mockPlayerAgent.discuss(anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String topic = invocation.getArgument(2);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock讨论%d】关于%s这个话题，我认为需要从多个角度分析。\"}",
                    count, topic
                );
            });

        // vote - 投票
        Mockito.when(mockPlayerAgent.vote(anyString(), anyString()))
            .thenAnswer(invocation -> {
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock投票%d】我投票给嫌疑人，因为有多项证据指向他。\"}",
                    count
                );
            });

        // privateChat - 单聊
        Mockito.when(mockPlayerAgent.privateChat(anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String targetPlayerId = invocation.getArgument(2);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock单聊%d】私下和玩家%s聊聊，有些话不方便公开说。\"}",
                    count, targetPlayerId
                );
            });

        // answerQuestion - 回答问题
        Mockito.when(mockPlayerAgent.answerQuestion(anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String question = invocation.getArgument(2);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock回答%d】关于问题'%s'，我认为答案是...\"}",
                    count, question.length() > 20 ? question.substring(0, 20) + "..." : question
                );
            });

        // reasonAndDiscuss - 推理讨论（核心方法）
        Mockito.when(mockPlayerAgent.reasonAndDiscuss(anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock推理讨论%d】根据目前的讨论，我认为有几个疑点需要关注：第一，毒药的来源；第二，每个人案发时的行踪；第三，修改遗嘱的动机。\"}",
                    count
                );
            });

        // analyzeTopic - 分析话题
        Mockito.when(mockPlayerAgent.analyzeTopic(anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String topic = invocation.getArgument(2);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock分析话题%d】对话题'%s'的深入分析...\"}",
                    count, topic.length() > 20 ? topic.substring(0, 20) + "..." : topic
                );
            });

        // decidePrivateChat - 决定单聊
        Mockito.when(mockPlayerAgent.decidePrivateChat(anyString(), anyString()))
            .thenReturn("{\"targetPlayerId\": \"2\", \"reason\": \"需要了解更多信息\", \"shouldChat\": true}");

        // generateStatement - 生成陈述（陈述阶段核心）
        Mockito.when(mockPlayerAgent.generateStatement(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String characterName = invocation.getArgument(3);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock陈述%d】我是%s，案发当天晚上我在准备晚宴。18点左右我开始在厨房忙碌，19点半服侍大家用餐。老爷当时精神很好，还和我聊了几句。20点老爷回书房后，我就回自己房间休息了。22点听到尖叫声才出来。我对老爷一直忠心耿耿，绝不可能害他。\"}",
                    count, characterName
                );
            });

        // readScript - 读取剧本
        Mockito.when(mockPlayerAgent.readScript(anyString(), anyString(), anyString(),
            anyString(), anyString()))
            .thenAnswer(invocation -> {
                String characterName = invocation.getArgument(3);
                return String.format("【Mock AI玩家】%s已阅读完剧本，准备好了。", characterName);
            });

        // investigate - 搜证决策（搜证阶段核心）
        Mockito.when(mockPlayerAgent.investigate(anyString(), anyString(), anyString(),
            anyList(), anyInt()))
            .thenAnswer(invocation -> {
                @SuppressWarnings("unchecked")
                List<String> sceneIds = invocation.getArgument(3);
                int maxChances = invocation.getArgument(4);
                int selectCount = Math.min(2, Math.min(maxChances, sceneIds.size()));
                List<String> selectedIds = new ArrayList<>(sceneIds);
                Collections.shuffle(selectedIds);
                selectedIds = selectedIds.subList(0, selectCount);

                StringBuilder sb = new StringBuilder("{\"investigationRequests\": [");
                for (int i = 0; i < selectedIds.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append("{\"clueId\": \"").append(selectedIds.get(i)).append("\"}");
                }
                sb.append("]}");
                log.info("[Mock AI] 搜证决策: {}", sb);
                return sb.toString();
            });

        // decideToReveal - 决定是否公开线索
        Mockito.when(mockPlayerAgent.decideToReveal(anyString(), anyString(), anyString(),
            anyString(), anyString()))
            .thenAnswer(invocation -> {
                String clueContent = invocation.getArgument(4);
                boolean reveal = Math.random() < 0.7;
                String contentPreview = clueContent.length() > 20 ? clueContent.substring(0, 20) : clueContent;
                return String.format(
                    "{\"decision\": \"%s\", \"reason\": \"%s\", \"analysis\": \"该线索'%s...'对案件有重要参考价值\"}",
                    reveal ? "公开" : "不公开",
                    reveal ? "对案件很重要" : "需要进一步确认",
                    contentPreview
                );
            });

        // answer - 答题（答题阶段核心）
        Mockito.when(mockPlayerAgent.answer(anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String characterName = invocation.getArgument(2);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "【Mock答案%d】%s认为凶手是侄子张明。动机：觊觎遗产。手法：趁人不注意在老爷的茶里下了毒。关键线索：修改的遗嘱对侄子不利，而且有人看到他案发前在厨房附近徘徊。",
                    count, characterName
                );
            });

        // answerWithContext - 带上下文答题
        Mockito.when(mockPlayerAgent.answerWithContext(anyString(), anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String characterName = invocation.getArgument(2);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "【Mock带上下文答案%d】%s认为凶手是侄子张明。动机：觊觎遗产。手法：趁人不注意在老爷的茶里下了毒。",
                    count, characterName
                );
            });

        // speakWithReasoning - 带推理的发言
        Mockito.when(mockPlayerAgent.speakWithReasoning(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String characterName = invocation.getArgument(3);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock推理发言%d】%s，经过分析，我认为凶手应该是与死者有利益冲突的人。根据目前的线索，我怀疑侄子的动机最大。\"}",
                    count, characterName
                );
            });

        // discussWithCharacterInfo - 带角色信息的讨论
        Mockito.when(mockPlayerAgent.discussWithCharacterInfo(anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                String characterName = invocation.getArgument(3);
                String topic = invocation.getArgument(2);
                long count = responseCounter.incrementAndGet();
                return String.format(
                    "{\"content\": \"【Mock角色讨论%d】作为%s，关于%s这个话题，我认为需要从我的角色背景来分析。\"}",
                    count, characterName, topic.length() > 20 ? topic.substring(0, 20) + "..." : topic
                );
            });

        return mockPlayerAgent;
    }

    /**
     * 创建 Mock DMAgent
     * 覆盖所有 DMAgent 接口方法
     *
     * @return Mock 的 DMAgent
     */
    private DMAgent createMockDmAgent() {
        DMAgent mockDmAgent = Mockito.mock(DMAgent.class);

        // introduceGame - 游戏介绍
        Mockito.when(mockDmAgent.introduceGame(anyString()))
            .thenReturn("【Mock DM】欢迎来到剧本杀游戏！今天我们将一起揭开迷雾庄园谋杀案的真相。请各位玩家认真阅读剧本，注意每一个细节。");

        // presentClue - 展示线索
        Mockito.when(mockDmAgent.presentClue(anyString()))
            .thenReturn("【Mock DM】现在展示一条新线索，请大家注意观察。");

        // advancePhase - 推进阶段
        Mockito.when(mockDmAgent.advancePhase(anyString()))
            .thenReturn("【Mock DM】现在进入下一阶段，请大家做好准备。");

        // respondToPlayer - 回应玩家
        Mockito.when(mockDmAgent.respondToPlayer(anyString(), anyString()))
            .thenAnswer(invocation -> {
                String playerMessage = invocation.getArgument(0);
                return String.format("【Mock DM】收到你的问题：%s。我会尽快回复。", 
                    playerMessage.length() > 30 ? playerMessage.substring(0, 30) + "..." : playerMessage);
            });

        // startDiscussion - 开始讨论
        Mockito.when(mockDmAgent.startDiscussion(anyString()))
            .thenReturn("【Mock DM】讨论环节开始，请大家依次陈述自己的观点，每人有5分钟时间。");

        // moderateDiscussion - 主持讨论
        Mockito.when(mockDmAgent.moderateDiscussion(anyString()))
            .thenAnswer(invocation -> {
                String discussionState = invocation.getArgument(0);
                return String.format("【Mock DM】收到讨论状态：%s。请大家有序发言，注意时间控制。", 
                    discussionState.length() > 30 ? discussionState.substring(0, 30) + "..." : discussionState);
            });

        // scoreAnswers - 评分（评分阶段核心）
        Mockito.when(mockDmAgent.scoreAnswers(anyList()))
            .thenAnswer(invocation -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> answers = invocation.getArgument(0);
                StringBuilder scoresJson = new StringBuilder("[");
                for (int i = 0; i < answers.size(); i++) {
                    if (i > 0) scoresJson.append(",");
                    Object playerId = answers.get(i).get("playerId");
                    int score = 70 + (int)(Math.random() * 25);
                    scoresJson.append(String.format(
                        "{\"playerId\": \"%s\", \"score\": %d, \"breakdown\": {\"motive\": %d, \"method\": %d, \"clues\": %d, \"accuracy\": %d}, \"comment\": \"表现不错，推理逻辑清晰\"}",
                        playerId,
                        score,
                        15 + (int)(Math.random() * 5),
                        15 + (int)(Math.random() * 5),
                        15 + (int)(Math.random() * 5),
                        25 + (int)(Math.random() * 10)
                    ));
                }
                scoresJson.append("]");
                return String.format(
                    "{\"scores\": %s, \"summary\": \"整体游戏表现良好，大家都很投入，推理过程精彩。\", \"ending\": \"案发当晚，侄子张明趁人不备，在叔叔的茶中下了毒。他觊觎遗产已久，得知叔叔要修改遗嘱后，决定先下手为强。真相大白，正义终将到来。\"}",
                    scoresJson.toString()
                );
            });

        return mockDmAgent;
    }

    /**
     * 创建 Mock JudgeAgent
     * 覆盖所有 JudgeAgent 接口方法
     *
     * @return Mock 的 JudgeAgent
     */
    private JudgeAgent createMockJudgeAgent() {
        JudgeAgent mockJudgeAgent = Mockito.mock(JudgeAgent.class);

        // validateMessage - 验证消息
        Mockito.when(mockJudgeAgent.validateMessage(anyString()))
            .thenReturn(true);

        // validateAction - 验证行为
        Mockito.when(mockJudgeAgent.validateAction(anyString(), anyString()))
            .thenReturn(true);

        // generateSummary - 生成摘要
        Mockito.when(mockJudgeAgent.generateSummary(anyString()))
            .thenReturn("【Mock Judge】游戏摘要：经过激烈的讨论和推理，玩家们逐渐揭开了案件的真相。");

        // monitorDiscussion - 监控讨论
        Mockito.when(mockJudgeAgent.monitorDiscussion(anyString()))
            .thenReturn(true);

        // summarizeDiscussion - 总结讨论
        Mockito.when(mockJudgeAgent.summarizeDiscussion(anyString()))
            .thenAnswer(invocation -> {
                String discussionContent = invocation.getArgument(0);
                return String.format("【Mock Judge】经过激烈的讨论，各方观点已经充分表达。主要争议集中在凶手的动机和作案手法上。希望大家在投票时能够综合考虑所有线索。讨论内容长度：%d字符。",
                    discussionContent.length());
            });

        return mockJudgeAgent;
    }

    /**
     * Mock ScriptGenerateService
     * 提供剧本生成服务的 Mock 实现
     *
     * @return Mock 的 ScriptGenerateService
     */
    @Bean
    @Primary
    public ScriptGenerateService mockScriptGenerateService() {
        log.warn("=======================================================");
        log.warn("警告：当前系统运行在 Mock AI 模式！ScriptGenerateService 将返回模拟数据！");
        log.warn("=======================================================");

        ScriptGenerateService mockService = Mockito.mock(ScriptGenerateService.class);

        // generateScript - 生成剧本
        Mockito.when(mockService.generateScript(anyString()))
            .thenReturn("{\"title\": \"Mock剧本\", \"background\": \"这是一个模拟的剧本背景\", \"characters\": [{\"name\": \"角色1\", \"background\": \"角色1背景\", \"secret\": \"角色1秘密\"}]}");

        // generateScriptStream - 流式生成剧本
        Mockito.when(mockService.generateScriptStream(anyString()))
            .thenReturn(Flux.just(
                "{",
                "  \"title\": \"Mock剧本\",",
                "  \"background\": \"这是一个模拟的剧本背景\",",
                "  \"characters\": [",
                "    {\"name\": \"角色1\", \"background\": \"角色1背景\", \"secret\": \"角色1秘密\"}",
                "  ]",
                "}"
            ));

        // generateWorldOutline - 生成世界大纲
        Mockito.when(mockService.generateWorldOutline(anyString()))
            .thenReturn(Flux.just(
                "{",
                "  \"title\": \"迷雾庄园谋杀案\",",
                "  \"background\": \"一个风雨交加的夜晚，庄园主人被发现死在书房...\",",
                "  \"truth\": \"凶手是管家用毒药下毒，伪造了心脏病发作的假象\",",
                "  \"timeline\": [",
                "    {\"time\": \"18:00\", \"event\": \"晚宴开始\"},",
                "    {\"time\": \"20:00\", \"event\": \"主人回书房\"},",
                "    {\"time\": \"22:00\", \"event\": \"发现尸体\"}",
                "  ],",
                "  \"characters\": [",
                "    {\"name\": \"管家王叔\", \"role\": \"庄园管家\", \"archetype\": \"忠诚隐忍\"},",
                "    {\"name\": \"女主人李婷\", \"role\": \"死者妻子\", \"archetype\": \"表面温柔\"},",
                "    {\"name\": \"侄子张明\", \"role\": \"死者侄子\", \"archetype\": \"贪婪狡猾\"},",
                "    {\"name\": \"医生陈伟\", \"role\": \"家庭医生\", \"archetype\": \"专业冷静\"},",
                "    {\"name\": \"女仆小红\", \"role\": \"年轻女仆\", \"archetype\": \"胆小善良\"}",
                "  ]",
                "}"
            ));

        // generateMechanics - 生成场景和线索
        Mockito.when(mockService.generateMechanics(anyString()))
            .thenReturn(Flux.just(
                "{",
                "  \"scenes\": [",
                "    {\"name\": \"书房\", \"desc\": \"主人的私人空间，发现尸体的地方\"},",
                "    {\"name\": \"厨房\", \"desc\": \"准备食物的地方\"},",
                "    {\"name\": \"卧室\", \"desc\": \"主人的休息空间\"},",
                "    {\"name\": \"花园\", \"desc\": \"庄园的户外区域\"}",
                "  ],",
                "  \"clues\": [",
                "    {\"name\": \"毒药瓶\", \"content\": \"在书房垃圾桶发现的小药瓶\", \"location\": \"书房\"},",
                "    {\"name\": \"遗嘱\", \"content\": \"修改过的遗嘱副本\", \"location\": \"卧室\"},",
                "    {\"name\": \"日记本\", \"content\": \"死者生前的日记\", \"location\": \"书房\"},",
                "    {\"name\": \"钥匙\", \"content\": \"一把不知名的钥匙\", \"location\": \"花园\"}",
                "  ]",
                "}"
            ));

        // generateCharacterMemoir - 生成角色回忆录
        Mockito.when(mockService.generateCharacterMemoir(anyString()))
            .thenReturn(Flux.just(
                "{",
                "  \"bio_story\": \"我叫王叔，在这个庄园工作了三十年。老爷对我有恩，我一直忠心耿耿...\",",
                "  \"personal_timeline\": \"18:00 准备晚宴 | 19:30 服侍老爷用餐 | 20:00 老爷回书房 | 21:00 我回房休息\",",
                "  \"secrets\": \"其实老爷最近想解雇我，但我并不恨他...\"",
                "}"
            ));

        return mockService;
    }
}
