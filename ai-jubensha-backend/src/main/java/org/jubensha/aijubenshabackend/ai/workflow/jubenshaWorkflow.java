package org.jubensha.aijubenshabackend.ai.workflow;


import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphRepresentation.Type;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.jubensha.aijubenshabackend.ai.workflow.node.PlayerAllocatorNode;
import org.jubensha.aijubenshabackend.ai.workflow.node.ScriptGeneratorNode;
import org.jubensha.aijubenshabackend.ai.workflow.state.WorkflowContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
                .addEdge("__START__", "script_generator")
                .addEdge("script_generator", "player_allocator")
                .addEdge("player_allocator", "__END__")
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
        WorkflowContext initialContext = WorkflowContext.builder()
            .originalPrompt(originalPrompt)
            .currentStep("初始化")
            .build();
        GraphRepresentation graph = workflow.getGraph(Type.MERMAID);
        log.info("并发工作流图：\n{}", graph.content());
        log.info("开始执行并发工作流...");
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
                log.info("当前上下文：{}", currentContext);
            }
            stepCounter++;
        }
        log.info("并发工作流执行完成");
        return finalContext;
    }
}