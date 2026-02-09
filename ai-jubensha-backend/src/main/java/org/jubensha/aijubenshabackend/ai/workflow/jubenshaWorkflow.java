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
        return createWorkflow(false); // 默认使用非流式节点
    }
    
    /**
     * 创建并发工作流
     * @param useStreaming 是否使用流式剧本生成节点
     */
    public CompiledGraph<MessagesState<String>> createWorkflow(boolean useStreaming) {
        try {
            MessagesStateGraph<String> graph = new MessagesStateGraph<String>();
            
            // 添加所有节点
            graph.addNode("player_allocator", PlayerAllocatorNode.create());
            graph.addNode("scene_loader", SceneLoaderNode.create());
            graph.addNode("script_reader", ScriptReaderNode.create());
            graph.addNode("first_investigation", FirstInvestigationNode.create());
            graph.addNode("discussion", DiscussionNode.create());
            
            // 根据参数选择使用流式或非流式剧本生成节点
            if (useStreaming) {
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
     * 执行并发工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt, Boolean createNewScript, Long scriptId) {
        return executeWorkflow(originalPrompt, createNewScript, scriptId, false);
    }
    
    /**
     * 执行并发工作流
     */
    public WorkflowContext executeWorkflow(String originalPrompt, Boolean createNewScript, Long scriptId, Boolean useStreaming) {
        CompiledGraph<MessagesState<String>> workflow = createWorkflow(useStreaming);
        // 生成唯一的游戏ID
        Long gameId = System.currentTimeMillis();
        WorkflowContext initialContext = WorkflowContext.builder()
            .originalPrompt(originalPrompt)
            .currentStep("初始化")
            .gameId(gameId)
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
        log.info("游戏ID: {}", gameId);
        log.info("剧本选择: {}", createNewScript ? "创建新剧本" : "使用现有剧本");
        if (!createNewScript) {
            log.info("现有剧本ID: {}", scriptId);
        }
        
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
                // 确保剧本选择参数在整个工作流中传递
                if (finalContext.getCreateNewScript() == null) {
                    finalContext.setCreateNewScript(createNewScript);
                }
                if (finalContext.getExistingScriptId() == null && !createNewScript) {
                    finalContext.setExistingScriptId(scriptId);
                }
//                log.info("当前上下文：{}", currentContext);
            }
            stepCounter++;
        }
        log.info("并发工作流执行完成");
        return finalContext;
    }
    
    /**
     * 执行并发工作流（兼容旧版本）
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        return executeWorkflow(originalPrompt, true, null);
    }
}