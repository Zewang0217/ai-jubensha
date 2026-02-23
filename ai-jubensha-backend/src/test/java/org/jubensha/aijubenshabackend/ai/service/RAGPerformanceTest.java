package org.jubensha.aijubenshabackend.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RAG性能测试和检索准确性验证
 */
@Slf4j
@SpringBootTest
public class RAGPerformanceTest {

    @Autowired
    private RAGService ragService;

    @Autowired
    private FactExtractor factExtractor;

    private Long testGameId = 1L;
    private Long testPlayerId = 1L;
    private String testPlayerName = "测试玩家";

    @BeforeEach
    void setUp() {
        // 准备测试数据
        prepareTestData();
    }

    private void prepareTestData() {
        // 存储测试对话
        String testContent = "昨晚8点，我在花园里看到了张三，他手里拿着一把刀，看起来很紧张。后来我听说李四下半夜在自己房间被杀害了，现场有一把带血的刀。我觉得张三很可疑，因为他之前和李四有过激烈的争吵。";
        ragService.insertConversationMemory(testGameId, testPlayerId, testPlayerName, testContent);
        log.info("测试数据准备完成");
    }

    @Test
    void testParentChildRetrievalPerformance() {
        log.info("开始测试父子文档检索性能");

        // 测试查询
        String query = "张三在花园里做了什么";

        // 测量检索时间
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> results = ragService.searchConversationMemory(testGameId, null, query, 5);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        log.info("父子文档检索完成，耗时: {}ms, 结果数量: {}", duration, results.size());

        // 验证结果质量
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> result = results.get(i);
            String content = (String) result.get("content");
            Double score = (Double) result.get("score");
            log.info("结果 {}: 分数={}, 内容={}", i + 1, score, content.substring(0, Math.min(100, content.length())));
        }

        // 性能断言
        assert duration < 5000; // 检索时间应小于5秒
        assert !results.isEmpty(); // 应返回结果
    }

    @Test
    void testFactExtractionAccuracy() {
        log.info("开始测试事实提取准确性");

        // 测试内容
        String testContent = "昨晚8点，我在花园里看到了张三，他手里拿着一把刀，看起来很紧张。后来我听说李四下半夜在自己房间被杀害了，现场有一把带血的刀。";

        // 测量提取时间
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> facts = factExtractor.extractFacts(testContent);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        log.info("事实提取完成，耗时: {}ms, 提取事实数量: {}", duration, facts.size());

        // 验证提取结果
        for (Map<String, Object> fact : facts) {
            String type = (String) fact.get("type");
            String content = (String) fact.get("content");
            log.info("提取事实: 类型={}, 内容={}", type, content);
        }

        // 准确性断言
        assert !facts.isEmpty(); // 应提取到事实
        boolean hasTimeFact = facts.stream().anyMatch(f -> "时间".equals(f.get("type")));
        boolean hasLocationFact = facts.stream().anyMatch(f -> "地点".equals(f.get("type")));
        boolean hasPersonFact = facts.stream().anyMatch(f -> "人物".equals(f.get("type")));
        boolean hasEventFact = facts.stream().anyMatch(f -> "事件".equals(f.get("type")));
        boolean hasClueFact = facts.stream().anyMatch(f -> "线索".equals(f.get("type")));

        assert hasTimeFact; // 应提取到时间
        assert hasLocationFact; // 应提取到地点
        assert hasPersonFact; // 应提取到人物
        assert hasEventFact; // 应提取到事件
        assert hasClueFact; // 应提取到线索
    }

    @Test
    void testCombinedRetrievalPerformance() {
        log.info("开始测试联合检索性能");

        // 测试查询
        String query = "张三和刀的关系";

        // 测量联合检索时间
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> results = ragService.searchCombinedMemory(testGameId, null, query, 10);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        log.info("联合检索完成，耗时: {}ms, 结果数量: {}", duration, results.size());

        // 验证结果多样性
        long conversationCount = results.stream().filter(r -> "conversation".equals(r.get("result_type"))).count();
        long factCount = results.stream().filter(r -> "fact".equals(r.get("result_type"))).count();

        log.info("联合检索结果分布: 对话={}, 事实={}", conversationCount, factCount);

        // 性能断言
        assert duration < 8000; // 联合检索时间应小于8秒
        assert !results.isEmpty(); // 应返回结果
        assert conversationCount > 0 || factCount > 0; // 应包含对话或事实结果
    }

    @Test
    void testBatchInsertPerformance() {
        log.info("开始测试批量插入性能");

        // 准备批量测试数据
        List<Map<String, Object>> testRecords = List.of(
                Map.of("playerId", testPlayerId, "playerName", testPlayerName, "content", "测试对话1: 我昨天在图书馆看到了王五"),
                Map.of("playerId", testPlayerId, "playerName", testPlayerName, "content", "测试对话2: 赵六说他有不在场证明"),
                Map.of("playerId", testPlayerId, "playerName", testPlayerName, "content", "测试对话3: 现场发现了一把钥匙")
        );

        // 测量批量插入时间
        long startTime = System.currentTimeMillis();
        List<Long> ids = ragService.batchInsertConversationMemory(testGameId, testRecords);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        log.info("批量插入完成，耗时: {}ms, 插入数量: {}", duration, ids.size());

        // 性能断言
        assert duration < 10000; // 批量插入时间应小于10秒
        assert ids.size() == testRecords.size(); // 应插入所有记录
    }
}
