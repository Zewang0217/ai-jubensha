# AI剧本杀项目 - Claude 开发指南

> 本文档为 Claude Code 提供项目上下文和开发规范，确保高效的 vibe coding 体验。
> 文档版本: 2.0 | 最后更新: 2026-03-05

---

## 📋 项目概览

**AI剧本杀**是一个基于多Agent架构的智能推理游戏系统，核心特点：
- 单人游戏模式，AI补齐其他角色（1真人 + N AI）
- LangChain4j + LangGraph4j 实现AI工作流编排
- 向量数据库 (Milvus) 实现语义记忆检索
- WebSocket 实时通信
- 流式剧本生成与组装

---

## 🏗️ 架构核心

### 1. 分层架构

```
ai-jubensha-backend/
├── ai/                          # AI层 - 核心智能逻辑
│   ├── agent/                   # Agent接口定义 (DMAgent, PlayerAgent, JudgeAgent)
│   ├── factory/                 # 服务工厂 (ScriptGenerateServiceFactory)
│   ├── guardrail/               # 输入安全检测 (PromptSafetyInputGuardrail)
│   ├── service/                 # AI服务层
│   │   ├── AIService.java       # Agent生命周期管理
│   │   ├── DiscussionService.java    # 讨论环节调度
│   │   ├── RAGService.java      # 向量检索服务
│   │   ├── MemoryHierarchyService.java  # 三级记忆管理
│   │   ├── ScriptGenerateService.java   # 剧本生成接口
│   │   └── util/                # 工具类
│   │       ├── DMModerator.java     # DM主导逻辑
│   │       ├── TurnManager.java     # 发言轮次管理
│   │       ├── DiscussionHelper.java # 讨论辅助
│   │       └── JsonValidationUtil.java # JSON验证与重试
│   ├── tools/                   # Agent可调用的工具
│   │   ├── BaseTool.java        # 工具基类
│   │   ├── ToolManager.java     # 工具注册中心
│   │   ├── permission/          # 工具权限管理
│   │   │   ├── AgentToolManager.java
│   │   │   ├── DMAgentToolManager.java
│   │   │   └── PlayerAgentToolManager.java
│   │   ├── SendDiscussionMessageTool.java
│   │   ├── GetClueTool.java
│   │   └── ...
│   └── workflow/                # LangGraph工作流
│       ├── jubenshaWorkflow.java        # 主游戏工作流
│       ├── ScriptCreationWorkflow.java  # 剧本生成子图
│       ├── node/                # 工作流节点
│       │   ├── ScriptCreationWorkflowNode.java  # 新剧本生成节点
│       │   ├── DiscussionNode.java      # 讨论环节节点
│       │   ├── PlayerAllocatorNode.java # 玩家分配节点
│       │   ├── OutlineNode.java         # 大纲生成节点
│       │   ├── CharacterGenNode.java    # 角色生成节点
│       │   └── ...
│       └── state/               # 工作流状态
│           ├── WorkflowContext.java     # 主上下文
│           └── ScriptCreationState.java # 剧本生成状态
│
├── controller/          # 控制器层 - HTTP API & WebSocket
├── service/             # 业务层 - 游戏核心逻辑
│   ├── game/            # 游戏服务
│   ├── investigation/   # 搜证服务
│   ├── script/          # 剧本服务
│   └── task/            # 任务管理
├── models/              # 模型层 - Entity, DTO, Enum
├── repository/          # 数据层 - Spring Data JPA
├── memory/              # 记忆层 - Milvus向量操作
│   ├── MemoryService.java
│   ├── MemoryServiceImpl.java
│   ├── MilvusCollectionManager.java
│   └── shortterm/       # 短期记忆服务
├── websocket/           # 通信层 - WebSocket配置和处理器
└── core/                # 基础设施
    ├── config/          # 配置类
    │   ├── ai/          # AI配置 (AIConfig, MilvusConfig)
    │   ├── redis/
    │   └── rabbitmq/
    ├── exception/       # 异常体系
    ├── response/        # 统一响应
    └── util/            # 工具类
```

### 2. 关键设计模式

| 模式 | 应用位置 | 说明 |
|------|----------|------|
| **Agent模式** | `ai/agent/` | LangChain4j AiServices 构建的AI角色 |
| **工作流模式** | `ai/workflow/` | LangGraph4j 状态机编排复杂业务流程 |
| **工厂模式** | `ai/factory/` | ScriptGenerateServiceFactory 创建服务实例 |
| **策略模式** | `ai/tools/permission/` | 不同Agent类型有不同的工具权限 |
| **分层记忆** | `ai/service/MemoryHierarchyService.java` | 短期/中期/长期记忆分层管理 |
| **缓存模式** | Caffeine Cache | Agent实例、服务实例、记忆缓存 |

---

## 🔧 核心系统详解

### 2.1 AI工作流系统 (LangGraph4j)

#### 主游戏工作流 (`jubenshaWorkflow`)

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   START     │────>│ script_generator │────>│ player_allocator│
└─────────────┘     └──────────────────┘     └────────┬────────┘
                                                      │
                           ┌──────────────────────────┘
                           │
                           ▼
              ┌─────────────────────┐
              │   script_reader     │
              │   scene_loader      │  (并行执行)
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │  first_investigation │  ← 搜证阶段
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │     discussion      │  ← 讨论阶段 (可阻塞2小时)
              └──────────┬──────────┘
                         │
                         ▼
              ┌─────────────────────┐
              │        END          │
              └─────────────────────┘
```

#### 剧本生成子图 (`ScriptCreationWorkflow`)

```
START ──> outline_node ──> character_gen_node ──> scene_clue_node ──> assembly_node ──> cover_image_generator_node ──> END
```

**节点说明：**
- `outline_node`: 生成剧本大纲（核心诡计、角色列表、时间线）
- `character_gen_node`: 生成角色详细设定（背景故事、秘密、时间线）
- `scene_clue_node`: 设计场景和线索
- `assembly_node`: 组装完整剧本数据
- `cover_image_generator_node`: 生成剧本封面图片

**代码示例：**
```java
// 执行工作流
WorkflowContext context = WorkflowContext.builder()
    .originalPrompt("民国豪门谋杀案")
    .createNewScript(true)
    .realPlayerCount(1)
    .build();

CompiledGraph<MessagesState<String>> workflow = jubenshaWorkflow.createWorkflow(true, true);
for (NodeOutput<MessagesState<String>> step : workflow.stream(
    Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, context)
)) {
    log.info("步骤完成: {}", step.node());
}
```

### 2.2 Agent系统 (LangChain4j)

#### Agent类型与职责

| Agent | 接口 | 系统提示词 | 核心能力 |
|-------|------|-----------|---------|
| **DMAgent** | `ai/service/agent/DMAgent.java` | `prompt/dm-system-prompt.txt` | 游戏主持、阶段推进、评分 |
| **PlayerAgent** | `ai/service/agent/PlayerAgent.java` | 内联@SystemMessage | 角色扮演、推理、发言生成 |
| **JudgeAgent** | `ai/service/agent/JudgeAgent.java` | `prompt/judge-system-prompt.txt` | 逻辑校验、一致性检查 |

#### Agent创建与缓存

```java
// AIService.java - Agent工厂方法
public Player createAIPlayer(String name) { ... }

public Player createDMAgent() {
    // 检查缓存
    String cacheKey = "dm:" + dm.getId();
    agentCache.get(cacheKey, key -> createDMAgentInstance(dm.getId()));
}

// 创建DM Agent实例
private Object createDMAgentInstance(Long dmId) {
    return AiServices.builder(DMAgent.class)
        .chatModel(chatModel)
        .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
        .tools(dmAgentToolManager.getAvailableTools())  // 注入工具
        .maxSequentialToolsInvocations(20)
        .build();
}
```

**Agent缓存策略：**
- 最大缓存 100 个实例
- 写入后 30 分钟过期
- 访问后 10 分钟过期
- 使用 Caffeine Cache 实现

### 2.3 工具系统

#### 工具架构

```
┌─────────────────────────────────────────────────────────┐
│                    Agent (DMAgent/PlayerAgent)          │
└──────────────────────┬──────────────────────────────────┘
                       │ 调用工具
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  AgentToolManager                       │
│  ┌─────────────────┐  ┌─────────────────┐              │
│  │ DMAgentToolManager│  │ PlayerAgentToolManager│        │
│  │ - 游戏控制工具    │  │ - 推理工具      │              │
│  │ - 消息广播工具    │  │ - 沟通工具      │              │
│  └─────────────────┘  └─────────────────┘              │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                    ToolManager                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐    │
│  │ SendMessage  │ │   GetClue    │ │  GetTimeline │    │
│  └──────────────┘ └──────────────┘ └──────────────┘    │
└─────────────────────────────────────────────────────────┘
```

#### 工具注册与发现

```java
// ToolManager.java - 工具注册中心
@Component
public class ToolManager {
    private final Map<String, BaseTool> toolMap = new HashMap<>();

    @Resource
    private BaseTool[] tools;  // Spring自动注入所有BaseTool实现

    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {} -> {}", tool.getToolName(), tool.getDisplayName());
        }
    }
}
```

#### 工具权限控制

```java
// AgentToolManager.java - 基类
public abstract class AgentToolManager {
    protected List<BaseTool> getToolsByPermission(ToolPermissionLevel level) {
        return Arrays.stream(toolManager.getAllTools())
            .filter(tool -> tool.getPermissionLevel() == level)
            .collect(Collectors.toList());
    }
}

// DMAgentToolManager.java - DM专用工具
@Component
public class DMAgentToolManager extends AgentToolManager {
    public DMAgentToolManager(ToolManager toolManager) {
        super(toolManager, AgentType.DM);
    }

    public List<BaseTool> getAvailableTools() {
        // DM可以访问所有工具
        return Arrays.asList(toolManager.getAllTools());
    }
}
```

### 2.4 RAG与记忆系统

#### 记忆分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                      MemoryHierarchyService                  │
├─────────────────────────────────────────────────────────────┤
│  短期记忆 (Caffeine Cache)                                   │
│  - 最近15分钟的对话                                          │
│  - 最大5000条记录                                            │
│  - 用于快速上下文检索                                        │
├─────────────────────────────────────────────────────────────┤
│  重要记忆 (Caffeine Cache)                                   │
│  - 关键线索、时间线                                          │
│  - 最大1000条记录                                            │
│  - 30分钟过期                                                │
├─────────────────────────────────────────────────────────────┤
│  中期记忆 (ConcurrentHashMap)                                │
│  - 游戏会话期间的索引                                        │
│  - 玩家ID -> 记忆ID列表映射                                   │
├─────────────────────────────────────────────────────────────┤
│  长期记忆 (Milvus Vector DB)                                 │
│  - conversation_{gameId}: 每局游戏的对话历史                 │
│  - global_memory_clue: 全局线索库                            │
│  - global_memory_timeline: 全局时间线库                      │
└─────────────────────────────────────────────────────────────┘
```

#### Milvus集合设计

```java
// RAGServiceImpl.java
/**
 * 集合命名规范：
 * - 对话记忆: conversation_{gameId}
 *   * game_id: 游戏ID
 *   * player_id: 玩家ID
 *   * message: 消息内容
 *   * timestamp: 时间戳
 *
 * - 全局线索: global_memory_clue
 *   * script_id: 剧本ID
 *   * character_id: 角色ID (0表示公开)
 *   * content: 线索内容
 *   * player_id: 发现者ID
 *
 * - 全局时间线: global_memory_timeline
 *   * script_id: 剧本ID
 *   * character_id: 角色ID
 *   * content: 时间线内容
 *   * timestamp: 时间点
 */
```

#### 智能检索流程

```java
// MemoryHierarchyService.java
public List<Map<String, Object>> intelligentRetrieval(Long gameId, Long playerId, String query, int topK) {
    // 1. 分析查询意图
    QueryIntent intent = analyzeQueryIntent(query, context);

    // 2. 根据意图选择检索策略
    switch (intent) {
        case CLUE_FINDING:
            // 线索查询 - 结合对话记忆和线索记忆
            results.addAll(ragService.searchConversationMemory(gameId, playerId, query, topK));
            results.addAll(ragService.searchGlobalClueMemory(null, playerId, query, topK));
            break;
        case TIMELINE_ANALYSIS:
            // 时间线分析
            results.addAll(ragService.searchGlobalTimelineMemory(null, playerId, query, topK));
            break;
        // ...
    }

    // 3. 合并和去重结果
    return mergeAndDeduplicateResults(results);
}
```

### 2.5 讨论系统

#### 讨论阶段流程

```
讨论开始
    │
    ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  陈述阶段    │────>│ 自由讨论阶段  │────>│  答题阶段    │
│  (5分钟/人)  │     │  (30分钟)    │     │  (10分钟)   │
└──────────────┘     └──────────────┘     └──────────────┘
    │                       │                    │
    ▼                       ▼                    ▼
  轮流发言              中央调度器            收集答案
  角色背景陈述          - 发言欲望值          DM评分
  时间线陈述            - 单聊管理            生成结局
```

#### 讨论服务核心逻辑

```java
// DiscussionServiceImpl.java
@Slf4j
@Service
public class DiscussionServiceImpl implements DiscussionService {

    // 游戏讨论状态映射
    private final Map<Long, DiscussionState> gameDiscussions = new ConcurrentHashMap<>();

    // 调度器线程池
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    /**
     * 启动讨论环节
     */
    public void startDiscussion(Long gameId, List<Long> playerIds, Long dmId, Long judgeId) {
        // 1. 初始化讨论状态
        DiscussionState state = new DiscussionState(gameId, playerIds);
        gameDiscussions.put(gameId, state);

        // 2. 启动DM主导逻辑
        dmModerator.startDiscussion(gameId, playerIds, dmId, characterIds, scriptId);

        // 3. 启动中央调度器（自由讨论阶段）
        startCentralScheduler(gameId);

        // 4. 设置总时间限制（30分钟）
        scheduler.schedule(() -> endDiscussion(gameId), 30, TimeUnit.MINUTES);
    }

    /**
     * 中央调度器 - 管理AI发言
     */
    private void startCentralScheduler(Long gameId) {
        scheduler.scheduleAtFixedRate(() -> {
            DiscussionState state = gameDiscussions.get(gameId);

            // 1. 检查发言欲望值
            for (Long playerId : state.getPlayerIds()) {
                double desire = calculateSpeakingDesire(playerId, state);
                if (desire > SPEAKING_THRESHOLD) {
                    // 2. 生成发言
                    String statement = generatePlayerStatement(gameId, playerId);
                    // 3. 广播消息
                    broadcastMessage(gameId, playerId, statement);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
```

#### DM主导逻辑

```java
// DMModerator.java - 讨论阶段控制
@Component
public class DMModerator {

    /**
     * 开始陈述阶段
     */
    private void startStatementPhase(Long gameId, List<Long> playerIds, DMAgent dmAgent) {
        // 切换到陈述阶段
        turnManager.switchPhase(gameId, TurnManager.PHASE_STATEMENT);

        // 依次让每位玩家发言
        for (int i = 0; i < playerIds.size(); i++) {
            Long playerId = playerIds.get(i);
            Long characterId = characterIds.get(i);

            // 获取PlayerAgent并生成陈述
            PlayerAgent playerAgent = aiService.getPlayerAgent(playerId);

            // 调用generateStatement，带JSON验证和重试
            String statement = JsonValidationUtil.generateWithRetry(() -> {
                return playerAgent.generateStatement(
                    gameId.toString(),
                    playerId.toString(),
                    characterId.toString(),
                    character.getName(),
                    scriptId.toString(),
                    character.getBackgroundStory(),
                    character.getSecret(),
                    character.getTimeline()
                );
            });

            // 解析JSON，提取content字段
            String content = extractContentFromJson(statement);

            // 发送陈述消息
            sendDiscussionMessageTool.executeSendDiscussionMessage(content, gameId, playerId, playerIds);
        }

        // 陈述阶段结束，进入自由讨论阶段
        startFreeDiscussionPhase(gameId, playerIds, dmAgent);
    }
}
```

### 2.6 搜证系统

#### 搜证流程

```
玩家请求搜证
    │
    ▼
┌───────────────────┐
│ InvestigationServiceImpl
│ 1. 验证游戏状态    │
│ 2. 验证玩家次数    │
│ 3. 获取线索       │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ 更新线索状态      │
│ 保存到向量数据库   │
│ - 重要线索: PUBLIC│
│ - 普通线索: PRIVATE│
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ 扣减搜证次数      │
│ 记录搜证历史      │
│ WebSocket通知     │
└───────────────────┘
```

#### 搜证次数管理

```java
// WorkflowContext.java
public static final int DEFAULT_INVESTIGATION_LIMIT = 3;  // 每轮3次

private Map<Long, Integer> playerInvestigationCounts;      // 剩余次数
private Map<Long, List<Map<String, Object>>> playerInvestigationHistories;  // 历史
private Map<Long, Boolean> playerInvestigationCompleted;   // 完成状态

public boolean consumeInvestigationChance(Long playerId) {
    if (!hasInvestigationChance(playerId)) {
        return false;
    }
    int remaining = this.playerInvestigationCounts.get(playerId);
    this.playerInvestigationCounts.put(playerId, remaining - 1);
    return true;
}
```

---

## 📊 实体关系

```
┌──────────────┐       ┌──────────────┐       ┌─────────────┐
│    Game      │──────<│  GamePlayer  │>──────│   Player    │
│   (游戏)      │       │  (游戏玩家)   │       │  (用户/AI)   │
│  - status    │       │  - role      │       │ - nickname  │
│  - phase     │       │  - status    │       │ - username  │
│  - scriptId  │       │  - characterId│       │ - role      │
└──────┬───────┘       └──────┬───────┘       └─────────────┘
       │                      │
       │                ┌─────┴─────┐
       │                ↓           ↓
       │          ┌─────────┐  ┌─────────┐
       │          │Character│  │  Clue   │
       │          │ (角色)   │  │ (线索)  │
       │          │ - secret│  │ - type  │
       │          │ - timeline│ │ - visibility│
       │          └────┬────┘  └─────────┘
       │               │
       └───────────────┘
                       │
                       ▼
              ┌───────────────┐
              │    Script     │
              │    (剧本)      │
              │  - title      │
              │  - difficulty │
              │  - coverImage │
              └───────┬───────┘
                      │
                      ▼
              ┌───────────────┐
              │     Scene     │
              │    (场景)      │
              │  - name       │
              │  - description│
              └───────────────┘
```

---

## 🔄 游戏流程状态机

```
┌─────────────────┐
│   INITIALIZING  │ 初始化
│   (游戏创建)     │
└────────┬────────┘
         │ ALL_PLAYERS_READY
         ▼
┌─────────────────┐
│ ROLE_ASSIGNMENT │ 角色分配
│ (AI补齐玩家)     │
└────────┬────────┘
         │ ROLES_ASSIGNED
         ▼
┌─────────────────┐     ┌─────────────┐
│ SCRIPT_READING  ├──→  │    INTRO    │ 开场介绍
│  (AI读剧本)      │     │  (DM介绍)    │
└────────┬────────┘     └──────┬──────┘
         │ SCRIPTS_READ         │ INTRO_FINISHED
         └──────────────────────┘
                            ▼
               ┌─────────────────────┐
               │ ROUND_1_INVESTIGATION│ 第一轮搜证
               │   (AI自动搜证)       │
               └──────────┬──────────┘
                          │ INVESTIGATION_FINISHED
                          ▼
               ┌─────────────────────┐
               │   ROUND_1_STATEMENT  │ 第一轮陈述
               │   (轮流发言)         │
               └──────────┬──────────┘
                          │
                          ▼
               ┌─────────────────────┐
               │ ROUND_1_DISCUSSION   │ 第一轮讨论
               │   (自由讨论30分钟)    │
               └──────────┬──────────┘
                          │ DISCUSSION_FINISHED
                          │ (循环2轮)
                          ▼
               ┌─────────────────────┐
               │    FINAL_DISCUSSION  │ 最终讨论
               └──────────┬──────────┘
                          │
                          ▼
               ┌─────────────────────┐
               │       ANSWER         │ 答题阶段
               │   (提交案件答案)     │
               └──────────┬──────────┘
                          │
                          ▼
               ┌─────────────────────┐
               │      SCORING         │ 评分阶段
               │   (DM评分+结局)      │
               └──────────┬──────────┘
                          │
                          ▼
               ┌─────────────────────┐
               │        END          │ 游戏结束
               └─────────────────────┘
```

---

## 💬 WebSocket 消息规范

### 消息格式

```java
// WebSocketMessage.java
public class WebSocketMessage<T> {
    private MessageType type;      // 消息类型
    private Long gameId;           // 游戏ID
    private Long senderId;         // 发送者ID
    private T payload;             // 消息内容
    private Long timestamp;        // 时间戳
}

public enum MessageType {
    // 游戏控制
    GAME_START("GAME_START"),
    PHASE_CHANGE("PHASE_CHANGE"),

    // 讨论相关
    DISCUSSION_MESSAGE("DISCUSSION_MESSAGE"),
    AI_STATEMENT("AI_STATEMENT"),
    PRIVATE_CHAT_REQUEST("PRIVATE_CHAT_REQUEST"),

    // 搜证相关
    INVESTIGATION_START("INVESTIGATION_START"),
    CLUE_DISCOVERED("CLUE_DISCOVERED"),

    // 投票相关
    VOTE_CAST("VOTE_CAST"),
    VOTING_COMPLETE("VOTING_COMPLETE"),

    // 系统
    ERROR("ERROR"),
    NOTIFICATION("NOTIFICATION")
}
```

### 典型消息流

```
搜证阶段:
[Server] ──INVESTIGATION_START────────> [Client]
[Client] ──EXPLORE_SCENE─────────────> [Server]
[Server] ──SCENE_DESCRIPTION─────────> [Client]
[Client] ──SEARCH_ACTION─────────────> [Server]
[Server] ──CLUE_DISCOVERED───────────> [Client]

讨论阶段:
[Client] ──DISCUSSION_MESSAGE────────> [Server]
[Server] ──AI_STATEMENT─────────────> [Client] (广播)

投票阶段:
[Client] ──VOTE_CAST────────────────> [Server]
[Server] ──VOTING_COMPLETE──────────> [Client] (广播结果)
```

---

## 🛠️ 开发规范

### 代码风格

```java
// 1. 类命名: 大驼峰，语义清晰
public class DiscussionServiceImpl implements DiscussionService { }

// 2. 方法命名: 小驼峰，动词开头
public String generatePlayerStatement(Long gameId, Long characterId) { }

// 3. 常量命名: 大写下划线
private static final int MAX_TURNS = 10;
private static final long STATEMENT_TIME_PER_PLAYER = 300;

// 4. 使用 Lombok 减少样板代码
@Slf4j
@Service
@RequiredArgsConstructor
public class MyService {
    private final Dependency dependency;
}

// 5. 依赖注入使用构造函数注入
@Service
public class MyService {
    private final RAGService ragService;
    private final AIService aiService;

    public MyService(RAGService ragService, AIService aiService) {
        this.ragService = ragService;
        this.aiService = aiService;
    }
}
```

### 错误处理

```java
// 统一使用自定义异常体系
throw new BusinessException(ErrorCodeEnum.GAME_NOT_FOUND);
throw new NoInvestigationChanceException(playerId, remaining);

// 异常枚举定义
public enum ErrorCodeEnum {
    GAME_NOT_FOUND(40401, "游戏不存在"),
    PLAYER_NOT_IN_GAME(40001, "玩家不在游戏中"),
    INVALID_PHASE_TRANSITION(40002, "无效的阶段转换"),
    AI_SERVICE_ERROR(50001, "AI服务异常");

    private final int code;
    private final String message;
}

// 全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getErrorCode(), e.getMessage());
    }
}
```

### JSON验证与重试

```java
// 使用JsonValidationUtil进行AI生成内容的验证和重试
String outlineJson = JsonValidationUtil.generateWithRetry(() -> {
    AtomicReference<String> outlineBuilder = new AtomicReference<>("");
    CompletableFuture<Void> streamFuture = new CompletableFuture<>();

    generateService.generateWorldOutline(userPrompt)
        .doOnNext(chunk -> outlineBuilder.updateAndGet(current -> current + chunk))
        .doOnComplete(() -> streamFuture.complete(null))
        .subscribe();

    streamFuture.orTimeout(300, TimeUnit.SECONDS).join();
    return outlineBuilder.get();
});
```

---

## 📦 关键配置

### application.yml 重要配置项

```yaml
# AI 模型配置
ai:
  model: deepseek-chat                    # 主模型
  api-key: ${AI_API_KEY}                  # API密钥
  base-url: https://api.deepseek.com
  embedding-model: BAAI/bge-large-zh-v1.5 # 嵌入模型
  embedding-base-url: https://api.siliconflow.cn
  embedding-api-key: ${EMBEDDING_API_KEY}

# Milvus 向量数据库配置
milvus:
  host: localhost
  port: 19530
  token: root:Milvus
  embedding-dimension: 1024
  metric-type: L2

# 游戏配置
game:
  max-players-per-game: 8
  default-game-duration: 120              # 分钟
  investigation-chances: 3                # 每轮搜证次数
  discussion-timeout: 30                  # 讨论阶段时长(分钟)

# 缓存配置
spring:
  cache:
    caffeine:
      spec: maximumSize=100,expireAfterWrite=30m
```

---

## 🧪 测试策略

### 测试目录结构

```
src/test/java/org/jubensha/aijubenshabackend/
├── ai/
│   ├── service/
│   │   ├── DiscussionServiceImplTest.java
│   │   ├── RAGServiceImplTest.java
│   │   └── MemoryHierarchyServiceTest.java
│   ├── workflow/
│   │   └── JubenshaWorkflowTest.java
│   └── tools/
│       └── ToolManagerTest.java
├── memory/
│   └── MilvusCollectionManagerTest.java
└── ContextLoadTest.java
```

### Mock AI 模式

```yaml
# application.yml 中开启 Mock AI
spring:
  profiles:
    active: dev, mock-ai   # mock-ai 使用模拟响应，不调用真实API
```

```java
// Mock剧本生成服务
@Profile("mock-ai")
@Component
public class MockScriptGenerateService implements ScriptGenerateService {
    @Override
    public String generateScript(String userMessage) {
        return loadMockScript("mock-script.json");
    }
}
```

---

## 🚀 常用开发任务

### 添加新的 Agent 工具

```java
// 1. 在 ai/tools/ 下创建工具类
@Component
public class MyTool implements BaseTool {
    @Override
    public String getToolName() { return "myTool"; }

    @Override
    public String getDescription() { return "工具描述"; }

    @Override
    public ToolPermissionLevel getPermissionLevel() {
        return ToolPermissionLevel.PLAYER;  // 指定权限级别
    }

    @Override
    public String execute(Map<String, Object> params) {
        // 实现工具逻辑
        return result;
    }
}

// 2. 自动注册 - ToolManager会通过Spring自动发现并注册
// 3. 在Agent权限管理器中添加访问权限（如需要）
```

### 添加新的工作流节点

```java
// 1. 实现 Node 接口
@Slf4j
public class MyNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：MyNode");

            try {
                // 节点逻辑
                String result = doSomething();

                context.setCurrentStep("MyNode");
                context.setModelOutput(result);
                context.setSuccess(true);

            } catch (Exception e) {
                log.error("节点执行失败: {}", e.getMessage(), e);
                context.setErrorMessage(e.getMessage());
                context.setSuccess(false);
            }

            return WorkflowContext.saveContext(context);
        });
    }
}

// 2. 在工作流中注册节点
workflow.addNode("my_node", MyNode.create());
workflow.addEdge("previous_node", "my_node");
workflow.addEdge("my_node", "next_node");
```

### 添加新的 API 接口

```java
// 1. 在 controller/ 下创建或修改 Controller
@RestController
@RequestMapping("/api/my-feature")
@RequiredArgsConstructor
public class MyFeatureController {
    private final MyFeatureService myFeatureService;

    @PostMapping
    public ApiResponse<MyResponse> myEndpoint(@RequestBody MyRequest request) {
        MyResponse result = myFeatureService.process(request);
        return ApiResponse.success(result);
    }
}

// 2. 在 models/dto/ 中定义请求/响应对象
@Data
public class MyRequest {
    private Long gameId;
    private String content;
}

// 3. 在 service/ 中实现业务逻辑
```

---

## 📚 参考资源

### 关键文档位置

| 文档 | 路径 |
|------|------|
| API接口文档 | `ai-jubensha-frontend/design/api_doc/` |
| 游戏流程设计 | `ai-jubensha-frontend/design/game-flow-design.md` |
| WebSocket文档 | `ai-jubensha-backend/src/main/java/org/jubensha/aijubenshabackend/websocket/doc/` |
| 数据库设计 | `ai-jubensha-backend/doc/` |

### 技术文档链接

- [LangChain4j Docs](https://docs.langchain4j.dev/)
- [LangGraph4j GitHub](https://github.com/bsorrentino/langgraph4j)
- [Milvus Docs](https://milvus.io/docs)
- [Spring State Machine](https://docs.spring.io/spring-statemachine/docs/current/reference/)

---

## 💡 常见开发场景

### 场景1: 调试AI对话

```yaml
# 开启 DEBUG 日志查看完整Prompt和响应
logging:
  level:
    org.jubensha: debug
    dev.langchain4j: debug

# 或使用 mock-ai profile 避免调用真实API
spring:
  profiles:
    active: dev, mock-ai
```

### 场景2: 排查向量检索问题

```java
// 检查向量写入
ragService.addToMemory(content, gameId, playerId);

// 检查向量检索
List<SearchResult> results = ragService.searchRelevantContext(
    query, gameId, playerId, 10
);

// 在 Milvus Dashboard 中查看集合数据
// http://localhost:8000 (Milvus Attu)
```

### 场景3: 讨论环节问题排查

```bash
# 检查讨论服务状态
curl http://localhost:8080/api/debug/discussion/{gameId}

# 检查工作流上下文
curl http://localhost:8080/api/debug/workflow/{gameId}

# 检查玩家发言状态
curl http://localhost:8080/api/debug/turn/{gameId}
```

### 场景4: 剧本生成超时问题

```java
// 增加超时时间
OpenAiChatModel chatModel = OpenAiChatModel.builder()
    .timeout(Duration.ofSeconds(600))  // 10分钟
    .maxTokens(8192)
    .build();

// 使用流式生成并添加进度回调
generateService.generateWorldOutline(topic)
    .doOnNext(chunk -> logProgress(chunk))
    .timeout(Duration.ofMinutes(5))
    .subscribe();
```

---

## ⚠️ 注意事项

1. **AI Token 消耗**:
   - 剧本生成消耗较大（约5-10k tokens/剧本）
   - 讨论阶段频繁调用（每轮约500-1k tokens/发言）
   - 使用 mock-ai profile 开发时节省费用

2. **向量数据库**:
   - 每局游戏创建独立的 conversation_{gameId} 集合
   - 游戏结束后可选择清理或归档
   - 定期监控Milvus存储使用情况

3. **并发处理**:
   - 游戏状态更新使用乐观锁
   - DiscussionService 使用 ConcurrentHashMap 管理多游戏状态
   - WebSocket 消息广播使用线程池

4. **内存泄漏**:
   - 游戏结束后清理 WorkflowContext
   - 定期清理 Agent 缓存（Caffeine自动过期）
   - 讨论结束后关闭调度器线程

5. **超时处理**:
   - 剧本生成设置5-10分钟超时
   - 讨论阶段设置30分钟总时间限制
   - API调用设置合适的超时时间

---

## 🔗 关键代码入口

| 功能 | 入口类 |
|------|--------|
| 工作流执行 | `jubenshaWorkflow.executeWorkflow()` |
| Agent创建 | `AIService.createPlayerAgent()` |
| 讨论启动 | `DiscussionServiceImpl.startDiscussion()` |
| 向量检索 | `RAGServiceImpl.searchConversationMemory()` |
| 记忆存储 | `MemoryServiceImpl.storeConversationMemory()` |
| 搜证处理 | `InvestigationServiceImpl.investigate()` |
| WebSocket广播 | `WebSocketServiceImpl.broadcastChatMessage()` |

---

> 📌 **Version**: 2.0
> 📅 **Last Updated**: 2026-03-05
>
> 本文档与项目代码同步更新，确保信息准确性。如发现过时内容，请及时更新。
