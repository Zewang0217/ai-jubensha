package org.jubensha.aijubenshabackend.ai.workflow;


import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphRepresentation.Type;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.jubensha.aijubenshabackend.ai.workflow.node.*;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import org.jubensha.aijubenshabackend.core.util.SpringContextUtil;
import org.jubensha.aijubenshabackend.models.entity.Game;
import org.jubensha.aijubenshabackend.models.enums.GameStatus;
import org.jubensha.aijubenshabackend.models.enums.GamePhase;
import org.jubensha.aijubenshabackend.service.game.GameService;

/**
 * 剧本杀的工作流
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-01-31 13:41
 * @since 2026
 */

@Slf4j
@Component
public class jubenshaWorkflow {

    /**
     * 创建并发工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        return createWorkflow(false); // 默认使用非流式节点
    }

    /**
     * 创建并发工作流
     * @param useStreaming 是否使用流式剧本生成节点
     * @param useNewWorkflow 是否使用新的剧本生成工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow(boolean useStreaming, boolean useNewWorkflow) {
        try {
            MessagesStateGraph<String> graph = new MessagesStateGraph<String>();

            // 添加所有节点
            graph.addNode("player_allocator", PlayerAllocatorNode.create());
            graph.addNode("scene_loader", SceneLoaderNode.create());
            graph.addNode("script_reader", ScriptReaderNode.create());
            graph.addNode("first_investigation", FirstInvestigationNode.create());
            graph.addNode("discussion", DiscussionNode.create());

            // 根据参数选择使用哪种剧本生成节点
            if (useNewWorkflow) {
                log.info("使用新的剧本生成工作流节点");
                graph.addNode("script_generator", ScriptCreationWorkflowNode.create());
            } else if (useStreaming) {
                log.info("使用流式剧本生成节点");
                graph.addNode("script_generator", ScriptGeneratorNode.createStreaming());
            } else {
                log.info("使用非流式剧本生成节点");
                graph.addNode("script_generator", ScriptGeneratorNode.create());
            }

            // 添加边
            graph.addEdge("__START__", "script_generator");
            graph.addEdge("script_generator", "player_allocator");
            graph.addEdge("player_allocator", "script_reader");
            graph.addEdge("player_allocator", "scene_loader");
            graph.addEdge("script_reader", "first_investigation");
            graph.addEdge("scene_loader", "first_investigation");
            graph.addEdge("first_investigation", "discussion");
            graph.addEdge("discussion", "__END__");

            return graph.compile();
        } catch (GraphStateException e) {
            // TODO: 替换为自定义的事务异常
            throw new RuntimeException(e);
        }
    }


    /**
     * 创建并发工作流
     * @param useStreaming 是否使用流式剧本生成节点
     */
    public CompiledGraph<MessagesState<String>> createWorkflow(boolean useStreaming) {
        return createWorkflow(useStreaming, false); // 默认不使用新的剧本生成工作流
    }

    /**
     * 执行并发工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt, Boolean createNewScript, Long scriptId) {
        return executeWorkflow(originalPrompt, createNewScript, scriptId, false, false, null);
    }

    /**
     * 执行并发工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt, Boolean createNewScript, Long scriptId, Boolean useStreaming) {
        return executeWorkflow(originalPrompt, createNewScript, scriptId, useStreaming, false, null);
    }

    /**
     * 执行并发工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt, Boolean createNewScript, Long scriptId, Boolean useStreaming, Long gameId) {
        return executeWorkflow(originalPrompt, createNewScript, scriptId, useStreaming, true, gameId);
    }

    /**
     * 执行并发工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt, Boolean createNewScript, Long scriptId, Boolean useStreaming, Boolean useNewWorkflow, Long gameId) {
        CompiledGraph<MessagesState<String>> workflow = createWorkflow(useStreaming, useNewWorkflow);

        // 根据是否提供gameId来决定是使用现有游戏还是创建新游戏
        try {
            // 获取游戏服务
            GameService gameService = SpringContextUtil.getBean(GameService.class);

            if (gameId != null) {
                // 使用现有游戏
                var gameOpt = gameService.getGameById(gameId);
                if (gameOpt.isPresent()) {
                    log.info("使用现有游戏，游戏ID: {}", gameId);
                    // 如果使用现有剧本，更新游戏的剧本ID
                    if (!createNewScript && scriptId != null && gameOpt.get().getScriptId() == null) {
                        Game game = gameOpt.get();
                        game.setScriptId(scriptId);
                        gameService.updateGame(gameId, game);
                        log.info("更新现有游戏的剧本ID: {}", scriptId);
                    }
                } else {
                    log.error("游戏不存在，游戏ID: {}", gameId);
                    // 游戏不存在，创建新游戏
                    gameId = createNewGame(gameService, createNewScript, scriptId);
                }
            } else {
                // 创建新游戏
                gameId = createNewGame(gameService, createNewScript, scriptId);
            }
        } catch (Exception e) {
            log.error("处理游戏失败：{}", e.getMessage(), e);
            // 如果处理游戏失败，创建新游戏
            try {
                GameService gameService = SpringContextUtil.getBean(GameService.class);
                gameId = createNewGame(gameService, createNewScript, scriptId);
            } catch (Exception e2) {
                log.error("创建新游戏也失败：{}", e2.getMessage(), e2);
            }
        }

        // 使用数据库自增的游戏ID
        WorkflowContext initialContext = WorkflowContext.builder()
            .originalPrompt(originalPrompt)
            .currentStep("初始化")
            .gameId(gameId) // 使用数据库自增的游戏ID
            .createNewScript(createNewScript)
            .existingScriptId(createNewScript ? null : scriptId)
            .build();

        // 如果使用现有剧本，直接设置剧本ID
        if (!createNewScript && scriptId != null) {
            initialContext.setScriptId(scriptId);
            initialContext.setCurrentStep("使用现有剧本");
            log.info("使用现有剧本，剧本ID: {}", scriptId);
        }

        GraphRepresentation graph = workflow.getGraph(Type.MERMAID);
        log.info("并发工作流图：\n{}", graph.content());
        log.info("开始执行并发工作流...");
        log.info("剧本选择: {}", createNewScript ? "创建新剧本" : "使用现有剧本");
        if (!createNewScript) {
            log.info("现有剧本ID: {}", scriptId);
        }
        log.info("游戏ID: {}", gameId);

        WorkflowContext finalContext = null;
        int stepCounter = 1;
        // 配置并发执行
        ExecutorService pool = ExecutorBuilder.create()
                .setCorePoolSize(10)
                .setMaxPoolSize(20)
                .setWorkQueue(new LinkedBlockingQueue<>(100))
                .setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("workflow-executor-").build())
                .build();
//        RunnableConfig runnableConfig = RunnableConfig.builder()
//            .addParallelNodeExecutor("") //
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext)
        )) {
            log.info("--- 第{}步：{}完成 ---", stepCounter, step.node());
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                // 确保剧本选择参数在整个工作流中传递
                if (finalContext.getCreateNewScript() == null) {
                    finalContext.setCreateNewScript(createNewScript);
                }
                if (finalContext.getExistingScriptId() == null && !createNewScript) {
                    finalContext.setExistingScriptId(scriptId);
                }
                // 确保游戏ID在整个工作流中传递
                if (finalContext.getGameId() == null && gameId != null) {
                    finalContext.setGameId(gameId);
                }
                // 记录当前游戏ID（如果已设置）
                if (finalContext.getGameId() != null) {
                    log.info("当前游戏ID: {}", finalContext.getGameId());
                }
//                log.info("当前上下文：{}", currentContext);
            }
            stepCounter++;
        }
        // 工作流执行完成后，如果生成了剧本，更新游戏的scriptId
        if (finalContext != null && finalContext.getScriptId() != null && gameId != null) {
            try {
                GameService gameService = SpringContextUtil.getBean(GameService.class);
                var gameOpt = gameService.getGameById(gameId);
                if (gameOpt.isPresent()) {
                    Game game = gameOpt.get();
                    if (game.getScriptId() == null) {
                        game.setScriptId(finalContext.getScriptId());
                        gameService.updateGame(gameId, game);
                        log.info("更新游戏的剧本ID: {}, 游戏ID: {}", finalContext.getScriptId(), gameId);
                    }
                }
            } catch (Exception e) {
                log.error("更新游戏剧本ID失败：{}", e.getMessage(), e);
            }
        }

        log.info("并发工作流执行完成");
        return finalContext;
    }

    /**
     * 创建新游戏
     */
    private Long createNewGame(GameService gameService, Boolean createNewScript, Long scriptId) {
        Game newGame = new Game();
        // 如果使用现有剧本，设置剧本ID
        if (!createNewScript && scriptId != null) {
            newGame.setScriptId(scriptId);
        }
        newGame.setStatus(GameStatus.CREATED);
        // 如果 gameCode 为空，则生成随机游戏码
        if (newGame.getGameCode() == null || newGame.getGameCode().isEmpty()) {
            newGame.setGameCode(generateGameCode());
        }
        newGame.setCurrentPhase(GamePhase.INTRODUCTION);
        newGame.setStartTime(LocalDateTime.now());

        newGame = gameService.createGame(newGame);
        Long gameId = newGame.getId();
        log.info("创建新游戏成功，游戏ID: {}", gameId);
        return gameId;
    }

    /**
     * 生成随机游戏码
     */
    private String generateGameCode() {
        // 生成6位随机数字游戏码
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * 执行并发工作流（兼容旧版本）
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        return executeWorkflow(originalPrompt, true, null);
    }
}