package org.jubensha.aijubenshabackend.ai.workflow;


import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.jubensha.aijubenshabackend.ai.workflow.node.AssemblyNode;
import org.jubensha.aijubenshabackend.ai.workflow.node.CharacterGenNode;
import org.jubensha.aijubenshabackend.ai.workflow.node.OutlineNode;
import org.jubensha.aijubenshabackend.ai.workflow.node.SceneClueNode;
import org.jubensha.aijubenshabackend.service.script.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 剧本生成子图
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-11 15:23
 * @since 2026
 */

@Service
public class ScriptCreationWorkflow {
    @Autowired
    ScriptService scriptService;

    public StateGraph<MessagesState<String>> buildGraph() {
        try {
            MessagesStateGraph<String> workflow = new MessagesStateGraph<>();

            // 添加大纲生成节点
            workflow.addNode("outline_node", OutlineNode.create());

            // 添加角色扩充节点
            workflow.addNode("character_gen_node", CharacterGenNode.create());

            // 添加场景与线索填充节点
            workflow.addNode("scene_clue_node", SceneClueNode.create());

            // 添加组装节点
            workflow.addNode("assembly_node", AssemblyNode.create());

            // 定义节点流转关系
            workflow.addEdge("__START__", "outline_node");
            workflow.addEdge("outline_node", "character_gen_node");
            workflow.addEdge("character_gen_node", "scene_clue_node");
            workflow.addEdge("scene_clue_node", "assembly_node");
            workflow.addEdge("assembly_node", "__END__");

            return workflow;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
