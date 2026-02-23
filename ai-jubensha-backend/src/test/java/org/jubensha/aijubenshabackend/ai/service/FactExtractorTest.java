package org.jubensha.aijubenshabackend.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * FactExtractor单元测试
 */
@Slf4j
@SpringBootTest
public class FactExtractorTest {

    @Autowired
    private FactExtractor factExtractor;

    private String testContent = "昨晚8点，我在花园里看到了张三，他手里拿着一把刀，看起来很紧张。后来我听说李四下半夜在自己房间被杀害了，现场有一把带血的刀。我觉得张三很可疑，因为他之前和李四有过激烈的争吵。";

    @Test
    void testExtractFacts() {
        log.info("测试提取事实");
        
        List<Map<String, Object>> facts = factExtractor.extractFacts(testContent);
        
        assert facts != null;
        assert !facts.isEmpty();
        
        log.info("提取事实成功，数量: {}", facts.size());
        
        for (Map<String, Object> fact : facts) {
            String type = (String) fact.get("type");
            String content = (String) fact.get("content");
            log.info("提取事实: 类型={}, 内容={}", type, content);
        }
        
        // 验证提取结果包含关键事实类型
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
    void testExtractFactsWithSpecificTypes() {
        log.info("测试提取指定类型的事实");
        
        List<String> specificTypes = List.of("时间", "地点", "人物");
        List<Map<String, Object>> facts = factExtractor.extractFacts(testContent, specificTypes);
        
        assert facts != null;
        
        log.info("提取指定类型事实成功，数量: {}", facts.size());
        
        for (Map<String, Object> fact : facts) {
            String type = (String) fact.get("type");
            String content = (String) fact.get("content");
            log.info("提取事实: 类型={}, 内容={}", type, content);
            
            // 验证类型在指定范围内
            assert specificTypes.contains(type);
        }
    }

    @Test
    void testBatchExtractFacts() {
        log.info("测试批量提取事实");
        
        List<String> testContents = List.of(
                testContent,
                "今天早上，王五在厨房发现了一把钥匙，上面有血迹。",
                "赵六说他昨晚10点就睡觉了，没有人可以证明。"
        );
        
        List<List<Map<String, Object>>> batchResults = factExtractor.batchExtractFacts(testContents);
        
        assert batchResults != null;
        assert batchResults.size() == testContents.size();
        
        log.info("批量提取事实成功，批次数量: {}", batchResults.size());
        
        for (int i = 0; i < batchResults.size(); i++) {
            List<Map<String, Object>> facts = batchResults.get(i);
            log.info("批次 {} 提取事实数量: {}", i + 1, facts.size());
        }
    }

    @Test
    void testValidateFactsQuality() {
        log.info("测试验证事实质量");
        
        List<Map<String, Object>> facts = factExtractor.extractFacts(testContent);
        
        assert facts != null;
        assert !facts.isEmpty();
        
        int qualityScore = factExtractor.validateFactsQuality(facts);
        
        log.info("事实质量评分: {}", qualityScore);
        
        assert qualityScore >= 0;
        assert qualityScore <= 100;
        assert qualityScore > 50; // 质量评分应高于50
    }
}
