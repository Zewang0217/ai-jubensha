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

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

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
        try {
            return new MessagesStateGraph<String>()
                    .addNode("script_generator", ScriptGeneratorNode.create())
                    .addNode("player_allocator", PlayerAllocatorNode.create())
                    .addNode("scene_loader", SceneLoaderNode.create())
                    .addNode("script_reader", ScriptReaderNode.create())
                    .addNode("first_investigation", FirstInvestigationNode.create())
                    .addEdge("__START__", "script_generator")
                    .addEdge("script_generator", "player_allocator")
                    .addEdge("player_allocator", "script_reader")
                    .addEdge("player_allocator", "scene_loader")
                    .addEdge("script_reader", "first_investigation")
                    .addEdge("scene_loader", "first_investigation")
                    .addEdge("first_investigation", "__END__")
                    .compile();
        } catch (GraphStateException e) {
            // TODO: 替换为自定义的事务异常
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行并发工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();
        // 生成唯一的游戏ID
        Long gameId = System.currentTimeMillis();
        WorkflowContext initialContext = WorkflowContext.builder()
            .originalPrompt(originalPrompt)
            .currentStep("初始化")
            .gameId(gameId)
            .build();
        GraphRepresentation graph = workflow.getGraph(Type.MERMAID);
        log.info("并发工作流图：\n{}", graph.content());
        log.info("开始执行并发工作流...");
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
                // 确保gameId在整个工作流中传递
                if (finalContext.getGameId() == null) {
                    finalContext.setGameId(gameId);
                }
                log.info("当前上下文：{}", currentContext);
            }
            stepCounter++;
        }
        log.info("并发工作流执行完成");
        return finalContext;
    }
}