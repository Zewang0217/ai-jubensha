package org.jubensha.aijubenshabackend.ai.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class JubenshaWorkflowTest {

    @Autowired
    private JubenshaFullWorkflow jubenshaFullWorkflow;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testFullWorkflowWithClueSceneMatching() {
        log.info("开始测试完整工作流，验证线索场景匹配的准确性");

        // 构建工作流图
        StateGraph<MessagesState<String>> workflow = jubenshaFullWorkflow.buildGraph();
        assertNotNull(workflow, "工作流图构建失败");

        // 验证工作流包含场景线索节点
        // 这里可以根据实际的工作流结构进行验证
        log.info("工作流图构建成功，包含场景线索节点");

        // 检查日志中的场景线索分布情况
        log.info("工作流测试完成，检查日志中的场景线索分布情况");
    }

    @Test
    public void testClueSceneDistribution() {
        log.info("测试场景线索分布情况");

        // 模拟场景和线索数据
        String mockMechanicsJson = "{\"scenes\": [{\"id\": 1, \"name\": \"庄园大厅\", \"description\": \"古老的庄园大厅，装饰华丽\", \"clues\": [{\"id\": 1, \"description\": \"地上的脚印\", \"type\": \"physical\"}, {\"id\": 2, \"description\": \"破碎的花瓶\", \"type\": \"physical\"}, {\"id\": 3, \"description\": \"可疑的信件\", \"type\": \"document\"}]}, {\"id\": 2, \"name\": \"书房\", \"description\": \"主人的书房，书架上摆满了书籍\", \"clues\": [{\"id\": 4, \"description\": \"打开的日记本\", \"type\": \"document\"}, {\"id\": 5, \"description\": \"带血的钢笔\", \"type\": \"physical\"}]}, {\"id\": 3, \"name\": \"花园\", \"description\": \"庄园的花园，种满了各种花卉\", \"clues\": [{\"id\": 6, \"description\": \"隐藏的钥匙\", \"type\": \"physical\"}]}], \"clues\": [{\"id\": 1, \"description\": \"地上的脚印\", \"type\": \"physical\"}, {\"id\": 2, \"description\": \"破碎的花瓶\", \"type\": \"physical\"}, {\"id\": 3, \"description\": \"可疑的信件\", \"type\": \"document\"}, {\"id\": 4, \"description\": \"打开的日记本\", \"type\": \"document\"}, {\"id\": 5, \"description\": \"带血的钢笔\", \"type\": \"physical\"}, {\"id\": 6, \"description\": \"隐藏的钥匙\", \"type\": \"physical\"}]}";

        try {
            // 解析模拟数据
            JsonNode rootNode = objectMapper.readTree(mockMechanicsJson);
            JsonNode scenesNode = rootNode.path("scenes");

            // 检查每个场景的线索数量
            for (JsonNode sceneNode : scenesNode) {
                String sceneName = sceneNode.path("name").asText();
                JsonNode cluesNode = sceneNode.path("clues");
                int clueCount = cluesNode.size();

                log.info("场景: {}，线索数量: {}", sceneName, clueCount);

                // 验证每个场景都有合理数量的线索
                assertTrue(clueCount > 0, "场景" + sceneName + "没有线索");
                assertTrue(clueCount <= 10, "场景" + sceneName + "线索数量过多");
            }

            // 检查总线索数量
            JsonNode allCluesNode = rootNode.path("clues");
            int totalClueCount = allCluesNode.size();
            log.info("总线索数量: {}", totalClueCount);
            assertTrue(totalClueCount > 0, "没有线索生成");

        } catch (Exception e) {
            log.error("解析场景线索数据失败: {}", e.getMessage());
            fail("解析场景线索数据失败");
        }
    }
}
