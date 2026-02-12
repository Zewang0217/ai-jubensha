package org.jubensha.aijubenshabackend.ai.workflow;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.jubensha.aijubenshabackend.ai.workflow.node.*;
import org.springframework.stereotype.Service;

/**
 * 完整的剧本杀工作流
 * 集成新的剧本生成工作流和现有的工作流节点
 */
@Slf4j
@Service
public class JubenshaFullWorkflow {

    /**
     * 构建完整的工作流图
     * @return 构建好的工作流图
     */
    public StateGraph<MessagesState<String>> buildGraph() {
        try {
            MessagesStateGraph<String> workflow = new MessagesStateGraph<>();

            // 添加剧本生成相关节点
            workflow.addNode("outline_node", OutlineNode.create());
            workflow.addNode("character_gen_node", CharacterGenNode.create());
            workflow.addNode("scene_clue_node", SceneClueNode.create());
            workflow.addNode("assembly_node", AssemblyNode.create());

            // 添加现有工作流节点
            workflow.addNode("player_allocator", PlayerAllocatorNode.create());
            workflow.addNode("scene_loader", SceneLoaderNode.create());
            workflow.addNode("script_reader", ScriptReaderNode.create());
            workflow.addNode("first_investigation", FirstInvestigationNode.create());
            workflow.addNode("discussion", DiscussionNode.create());

            // 定义节点流转关系
            // 剧本生成流程
            workflow.addEdge("__START__", "outline_node");
            workflow.addEdge("outline_node", "character_gen_node");
            workflow.addEdge("character_gen_node", "scene_clue_node");
            workflow.addEdge("scene_clue_node", "assembly_node");

            // 剧本杀游戏流程
            workflow.addEdge("assembly_node", "player_allocator");
            workflow.addEdge("player_allocator", "script_reader");
            workflow.addEdge("player_allocator", "scene_loader");
            workflow.addEdge("script_reader", "first_investigation");
            workflow.addEdge("scene_loader", "first_investigation");
            workflow.addEdge("first_investigation", "discussion");
            workflow.addEdge("discussion", "__END__");

            log.info("完整的剧本杀工作流构建完成");
            return workflow;
        } catch (Exception e) {
            log.error("构建工作流图时发生错误", e);
            return null;
        }
    }
    
    /**
     * 构建使用现有剧本的工作流图
     * @return 构建好的工作流图
     */
    public StateGraph<MessagesState<String>> buildGraphWithExistingScript() {
        try {
            MessagesStateGraph<String> workflow = new MessagesStateGraph<>();

            // 添加现有工作流节点
            workflow.addNode("player_allocator", PlayerAllocatorNode.create());
            workflow.addNode("scene_loader", SceneLoaderNode.create());
            workflow.addNode("script_reader", ScriptReaderNode.create());
            workflow.addNode("first_investigation", FirstInvestigationNode.create());
            workflow.addNode("discussion", DiscussionNode.create());

            // 定义节点流转关系
            workflow.addEdge("__START__", "player_allocator");
            workflow.addEdge("player_allocator", "script_reader");
            workflow.addEdge("player_allocator", "scene_loader");
            workflow.addEdge("script_reader", "first_investigation");
            workflow.addEdge("scene_loader", "first_investigation");
            workflow.addEdge("first_investigation", "discussion");
            workflow.addEdge("discussion", "__END__");

            log.info("使用现有剧本的工作流构建完成");
            return workflow;
        } catch (Exception e) {
            log.error("构建工作流图时发生错误", e);
            return null;
        }
    }
}
