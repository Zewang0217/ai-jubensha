package org.jubensha.aijubenshabackend.ai.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import org.jubensha.aijubenshabackend.core.config.ai.MilvusSchemaConfig;
import org.jubensha.aijubenshabackend.memory.MilvusCollectionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RAGService集成测试
 * 测试真实的RAGServiceImpl和EmbeddingModel功能
 * 使用本地运行的Milvus服务进行真实操作
 */
@SpringBootTest
class RAGServiceIntegrationTest {

    @Autowired
    private RAGService ragService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private MilvusClientV2 milvusClientV2;

    @Autowired
    private MilvusCollectionManager collectionManager;

    @Autowired
    private MilvusSchemaConfig schemaConfig;

    private static final Long TEST_GAME_ID = 999L;
    private static final Long TEST_PLAYER_ID = 1001L;
    private static final Long TEST_SCRIPT_ID = 1L;
    private static final Long TEST_CHARACTER_ID = 201L;

    @BeforeAll
    static void beforeAll() {
        // 全局准备工作
        System.out.println("开始RAGService集成测试...");
    }

    @BeforeEach
    void setUp() {
        // 测试前清理测试数据
        cleanupTestData();
        System.out.println("测试环境准备完成");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("RAGService集成测试完成");
    }

    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        try {
            // 清理测试游戏的对话记忆集合
            String conversationCollection = schemaConfig.getConversationCollectionName(TEST_GAME_ID);
            if (collectionManager.collectionExists(conversationCollection)) {
                milvusClientV2.dropCollection(DropCollectionReq.builder()
                        .collectionName(conversationCollection)
                        .build());
                System.out.println("已清理测试对话记忆集合: " + conversationCollection);
            }
        } catch (Exception e) {
            System.out.println("清理测试数据时发生错误: " + e.getMessage());
        }
    }

    // ==================== EmbeddingModel测试 ====================

    @Test
    void testGenerateEmbedding() {
        System.out.println("测试: 生成单个文本嵌入");
        
        // 测试数据
        String testText = "这是一个测试文本，用于生成嵌入向量";
        
        // 执行
        List<Float> embedding = embeddingService.generateEmbedding(testText);
        
        // 验证
        assertNotNull(embedding, "嵌入向量不应为null");
        assertFalse(embedding.isEmpty(), "嵌入向量不应为空");
        assertEquals(1024, embedding.size(), "嵌入向量维度应为1024");
        
        // 验证嵌入向量的值范围
        for (Float value : embedding) {
            assertNotNull(value, "嵌入向量中的值不应为null");
        }
        
        System.out.println("✓ 单个文本嵌入生成成功，维度: " + embedding.size());
    }

    @Test
    void testGenerateEmbeddings() {
        System.out.println("测试: 生成批量文本嵌入");
        
        // 测试数据
        List<String> testTexts = List.of(
                "第一个测试文本",
                "第二个测试文本",
                "第三个测试文本"
        );
        
        // 执行
        List<List<Float>> embeddings = embeddingService.generateEmbeddings(testTexts);
        
        // 验证
        assertNotNull(embeddings, "嵌入向量列表不应为null");
        assertFalse(embeddings.isEmpty(), "嵌入向量列表不应为空");
        assertEquals(testTexts.size(), embeddings.size(), "嵌入向量数量应与文本数量一致");
        
        // 验证每个嵌入向量
        for (int i = 0; i < embeddings.size(); i++) {
            List<Float> embedding = embeddings.get(i);
            assertNotNull(embedding, "第" + i + "个嵌入向量不应为null");
            assertFalse(embedding.isEmpty(), "第" + i + "个嵌入向量不应为空");
            assertEquals(1024, embedding.size(), "第" + i + "个嵌入向量维度应为1024");
        }
        
        System.out.println("✓ 批量文本嵌入生成成功，数量: " + embeddings.size());
    }

    // ==================== RAGServiceImpl测试 ====================

    @Test
    void testInitializeConversationCollection() {
        System.out.println("测试: 初始化对话记忆集合");
        
        // 执行
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 验证集合是否存在
        String collectionName = schemaConfig.getConversationCollectionName(TEST_GAME_ID);
        boolean exists = collectionManager.collectionExists(collectionName);
        
        assertTrue(exists, "对话记忆集合应被创建");
        System.out.println("✓ 对话记忆集合初始化成功: " + collectionName);
        
        // 验证集合结构（可选）
        // 这里可以添加更多验证，比如检查集合的字段结构
    }

    @Test
    void testSearchConversationMemory() {
        System.out.println("测试: 搜索对话记忆");
        
        // 1. 确保对话记忆集合存在
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 2. 先插入一些测试数据
        System.out.println("插入测试对话记忆数据...");
        ragService.insertConversationMemory(TEST_GAME_ID, TEST_PLAYER_ID, "测试玩家1", "这是第一条测试对话内容，包含测试关键词");
        ragService.insertConversationMemory(TEST_GAME_ID, TEST_PLAYER_ID, "测试玩家2", "这是第二条测试对话内容，也包含测试关键词");
        ragService.insertConversationMemory(TEST_GAME_ID, 1002L, "其他玩家", "这是第三条对话内容，不包含测试关键词");
        System.out.println("测试数据插入完成");
        
        // 3. 测试搜索
        String query = "测试对话内容";
        int topK = 5;
        
        List<Map<String, Object>> results = ragService.searchConversationMemory(
                TEST_GAME_ID, TEST_PLAYER_ID, query, topK
        );
        
        // 验证
        assertNotNull(results, "搜索结果不应为null");
        System.out.println("✓ 对话记忆搜索测试完成，返回结果数量: " + results.size());
        // 打印搜索结果，方便查看
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> result = results.get(i);
            System.out.println("  结果 " + (i+1) + ": 内容='" + result.get("content") + "'，相似度='" + result.get("score") + "'");
        }
    }

    @Test
    void testSearchGlobalClueMemory() {
        System.out.println("测试: 搜索全局线索记忆");
        
        // 测试搜索
        String query = "谋杀线索";
        int topK = 5;
        
        List<Map<String, Object>> results = ragService.searchGlobalClueMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, query, topK
        );
        
        // 验证
        assertNotNull(results, "搜索结果不应为null");
        System.out.println("✓ 全局线索记忆搜索测试完成，返回结果数量: " + results.size());
    }

    @Test
    void testSearchGlobalTimelineMemory() {
        System.out.println("测试: 搜索全局时间线记忆");
        
        // 测试搜索
        String query = "案发时间";
        int topK = 5;
        
        List<Map<String, Object>> results = ragService.searchGlobalTimelineMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, query, topK
        );
        
        // 验证
        assertNotNull(results, "搜索结果不应为null");
        System.out.println("✓ 全局时间线记忆搜索测试完成，返回结果数量: " + results.size());
    }

    @Test
    void testFilterByDiscoveredClues() {
        System.out.println("测试: 基于已发现线索的过滤查询");
        
        // 1. 确保对话记忆集合存在
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 2. 测试过滤查询
        List<Long> discoveredClueIds = List.of(1L, 2L, 3L);
        String query = "测试查询";
        int topK = 5;
        
        List<Map<String, Object>> results = ragService.filterByDiscoveredClues(
                TEST_GAME_ID, TEST_PLAYER_ID, discoveredClueIds, query, topK
        );
        
        // 验证
        assertNotNull(results, "过滤查询结果不应为null");
        System.out.println("✓ 基于已发现线索的过滤查询测试完成，返回结果数量: " + results.size());
    }

    @Test
    void testCalculateClueRelationStrength() {
        System.out.println("测试: 计算线索关联强度");
        
        // 1. 先插入两条相关的线索数据
        System.out.println("插入测试线索数据...");
        Long clueId1 = ragService.insertGlobalClueMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, "这是一条关于谋杀案的线索，死者是被毒死的"
        );
        Long clueId2 = ragService.insertGlobalClueMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, "这是一条关于毒药的线索，现场发现了毒瓶"
        );
        Long clueId3 = ragService.insertGlobalClueMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, "这是一条无关的线索，天气很好"
        );
        System.out.println("测试线索数据插入完成，clueId1: " + clueId1 + ", clueId2: " + clueId2 + ", clueId3: " + clueId3);
        
        // 2. 测试相关线索之间的关联强度
        System.out.println("计算相关线索之间的关联强度...");
        int strength1 = ragService.calculateClueRelationStrength(
                TEST_GAME_ID, clueId1, clueId2
        );
        
        // 3. 测试无关线索之间的关联强度
        System.out.println("计算无关线索之间的关联强度...");
        int strength2 = ragService.calculateClueRelationStrength(
                TEST_GAME_ID, clueId1, clueId3
        );
        
        // 验证
        assertTrue(strength1 >= 0 && strength1 <= 100, "关联强度应在0-100之间");
        assertTrue(strength2 >= 0 && strength2 <= 100, "关联强度应在0-100之间");
        // 验证相关线索的关联强度应高于无关线索
        assertTrue(strength1 > strength2, "相关线索的关联强度应高于无关线索");
        
        System.out.println("✓ 线索关联强度计算测试完成");
        System.out.println("  相关线索关联强度: " + strength1);
        System.out.println("  无关线索关联强度: " + strength2);
    }

    /**
     * 验证全局记忆集合是否存在
     */
    @Test
    void testGlobalMemoryCollectionExists() {
        System.out.println("测试: 验证全局记忆集合存在");
        
        String globalCollection = schemaConfig.getGlobalMemoryCollectionName();
        boolean exists = collectionManager.collectionExists(globalCollection);
        
        assertTrue(exists, "全局记忆集合应存在");
        System.out.println("✓ 全局记忆集合存在验证成功: " + globalCollection);
    }

    @Test
    void testInsertConversationMemory() {
        System.out.println("测试: 插入对话记忆");
        
        // 1. 确保对话记忆集合存在
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 2. 测试插入
        String content = "这是一条测试对话内容，用于测试插入功能";
        Long recordId = ragService.insertConversationMemory(
                TEST_GAME_ID, TEST_PLAYER_ID, "测试玩家", content
        );
        
        // 验证
        assertNotNull(recordId, "插入的记录ID不应为null");
        assertTrue(recordId > 0, "插入的记录ID应大于0");
        System.out.println("✓ 对话记忆插入成功，记录ID: " + recordId);
    }

    @Test
    void testBatchInsertConversationMemory() {
        System.out.println("测试: 批量插入对话记忆");
        
        // 1. 确保对话记忆集合存在
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 2. 准备测试数据
        List<Map<String, Object>> records = List.of(
                Map.of(
                        "playerId", TEST_PLAYER_ID,
                        "playerName", "测试玩家1",
                        "content", "批量测试对话1"
                ),
                Map.of(
                        "playerId", TEST_PLAYER_ID,
                        "playerName", "测试玩家2",
                        "content", "批量测试对话2"
                )
        );
        
        // 3. 测试批量插入
        List<Long> recordIds = ragService.batchInsertConversationMemory(TEST_GAME_ID, records);
        
        // 验证
        assertNotNull(recordIds, "插入的记录ID列表不应为null");
        assertFalse(recordIds.isEmpty(), "插入的记录ID列表不应为空");
        assertEquals(records.size(), recordIds.size(), "插入的记录数量应与输入一致");
        System.out.println("✓ 批量插入对话记忆成功，插入数量: " + recordIds.size());
    }

    @Test
    void testInsertGlobalClueMemory() {
        System.out.println("测试: 插入全局线索记忆");
        
        // 1. 全局记忆集合在应用启动时会自动初始化
        
        // 2. 测试插入
        String content = "这是一条测试线索，用于测试全局线索记忆插入功能";
        Long recordId = ragService.insertGlobalClueMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, content
        );
        
        // 验证
        assertNotNull(recordId, "插入的记录ID不应为null");
        assertTrue(recordId > 0, "插入的记录ID应大于0");
        System.out.println("✓ 全局线索记忆插入成功，记录ID: " + recordId);
    }

    @Test
    void testInsertGlobalTimelineMemory() {
        System.out.println("测试: 插入全局时间线记忆");
        
        // 1. 全局记忆集合在应用启动时会自动初始化
        
        // 2. 测试插入
        String content = "这是一条测试时间线，用于测试全局时间线记忆插入功能";
        String timestamp = "2026-02-02 12:00:00";
        Long recordId = ragService.insertGlobalTimelineMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, content, timestamp
        );
        
        // 验证
        assertNotNull(recordId, "插入的记录ID不应为null");
        assertTrue(recordId > 0, "插入的记录ID应大于0");
        System.out.println("✓ 全局时间线记忆插入成功，记录ID: " + recordId);
    }

    @Test
    void testBatchInsertGlobalMemory() {
        System.out.println("测试: 批量插入全局记忆");
        
        // 1. 全局记忆集合在应用启动时会自动初始化
        
        // 2. 准备测试数据
        List<Map<String, Object>> records = List.of(
                Map.of(
                        "scriptId", TEST_SCRIPT_ID,
                        "characterId", TEST_CHARACTER_ID,
                        "type", "clue",
                        "content", "批量测试线索1"
                ),
                Map.of(
                        "scriptId", TEST_SCRIPT_ID,
                        "characterId", TEST_CHARACTER_ID,
                        "type", "timeline",
                        "content", "批量测试时间线1",
                        "timestamp", "2026-02-02 13:00:00"
                )
        );
        
        // 3. 测试批量插入
        List<Long> recordIds = ragService.batchInsertGlobalMemory(records);
        
        // 验证
        assertNotNull(recordIds, "插入的记录ID列表不应为null");
        assertFalse(recordIds.isEmpty(), "插入的记录ID列表不应为空");
        assertEquals(records.size(), recordIds.size(), "插入的记录数量应与输入一致");
        System.out.println("✓ 批量插入全局记忆成功，插入数量: " + recordIds.size());
    }

    @Test
    void testUpdateConversationMemory() {
        System.out.println("测试: 更新对话记忆");
        
        // 1. 确保对话记忆集合存在
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 2. 先插入一条记录
        String originalContent = "原始对话内容，用于测试更新功能";
        Long recordId = ragService.insertConversationMemory(
                TEST_GAME_ID, TEST_PLAYER_ID, "测试玩家", originalContent
        );
        assertNotNull(recordId, "插入的记录ID不应为null");
        
        // 3. 测试更新
        String updatedContent = "更新后的对话内容，用于测试更新功能";
        boolean updated = ragService.updateConversationMemory(
                TEST_GAME_ID, recordId, updatedContent
        );
        
        // 验证
        assertTrue(updated, "更新应成功");
        System.out.println("✓ 对话记忆更新成功，记录ID: " + recordId);
    }

    @Test
    void testUpdateGlobalMemory() {
        System.out.println("测试: 更新全局记忆");
        
        // 1. 全局记忆集合在应用启动时会自动初始化
        
        // 2. 先插入一条记录
        String originalContent = "原始全局记忆内容，用于测试更新功能";
        Long recordId = ragService.insertGlobalClueMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, originalContent
        );
        assertNotNull(recordId, "插入的记录ID不应为null");
        
        // 3. 测试更新
        String updatedContent = "更新后的全局记忆内容，用于测试更新功能";
        boolean updated = ragService.updateGlobalMemory(recordId, updatedContent);
        
        // 验证
        assertTrue(updated, "更新应成功");
        System.out.println("✓ 全局记忆更新成功，记录ID: " + recordId);
    }

    @Test
    void testDeleteConversationMemory() {
        System.out.println("测试: 删除对话记忆");
        
        // 1. 确保对话记忆集合存在
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 2. 先插入一条记录
        String content = "用于测试删除功能的对话内容";
        Long recordId = ragService.insertConversationMemory(
                TEST_GAME_ID, TEST_PLAYER_ID, "测试玩家", content
        );
        assertNotNull(recordId, "插入的记录ID不应为null");
        
        // 3. 测试删除
        boolean deleted = ragService.deleteConversationMemory(TEST_GAME_ID, recordId);
        
        // 验证
        assertTrue(deleted, "删除应成功");
        System.out.println("✓ 对话记忆删除成功，记录ID: " + recordId);
    }

    @Test
    void testDeleteGlobalMemory() {
        System.out.println("测试: 删除全局记忆");
        
        // 1. 全局记忆集合在应用启动时会自动初始化
        
        // 2. 先插入一条记录
        String content = "用于测试删除功能的全局记忆内容";
        Long recordId = ragService.insertGlobalClueMemory(
                TEST_SCRIPT_ID, TEST_CHARACTER_ID, content
        );
        assertNotNull(recordId, "插入的记录ID不应为null");
        
        // 3. 测试删除
        boolean deleted = ragService.deleteGlobalMemory(recordId);
        
        // 验证
        assertTrue(deleted, "删除应成功");
        System.out.println("✓ 全局记忆删除成功，记录ID: " + recordId);
    }

    @Test
    void testBatchDeleteConversationMemory() {
        System.out.println("测试: 批量删除对话记忆");
        
        // 1. 确保对话记忆集合存在
        collectionManager.initializeConversationCollection(TEST_GAME_ID);
        
        // 2. 先批量插入记录
        List<Map<String, Object>> records = List.of(
                Map.of(
                        "playerId", TEST_PLAYER_ID,
                        "playerName", "测试玩家1",
                        "content", "用于测试批量删除的对话1"
                ),
                Map.of(
                        "playerId", TEST_PLAYER_ID,
                        "playerName", "测试玩家2",
                        "content", "用于测试批量删除的对话2"
                )
        );
        List<Long> recordIds = ragService.batchInsertConversationMemory(TEST_GAME_ID, records);
        assertNotNull(recordIds, "插入的记录ID列表不应为null");
        assertFalse(recordIds.isEmpty(), "插入的记录ID列表不应为空");
        
        // 3. 测试批量删除
        int deletedCount = ragService.batchDeleteConversationMemory(TEST_GAME_ID, recordIds);
        
        // 验证
        assertTrue(deletedCount > 0, "删除的记录数量应大于0");
        assertEquals(recordIds.size(), deletedCount, "删除的记录数量应与输入一致");
        System.out.println("✓ 批量删除对话记忆成功，删除数量: " + deletedCount);
    }

    @Test
    void testBatchDeleteGlobalMemory() {
        System.out.println("测试: 批量删除全局记忆");
        
        // 1. 全局记忆集合在应用启动时会自动初始化
        
        // 2. 先批量插入记录
        List<Map<String, Object>> records = List.of(
                Map.of(
                        "scriptId", TEST_SCRIPT_ID,
                        "characterId", TEST_CHARACTER_ID,
                        "type", "clue",
                        "content", "用于测试批量删除的线索1"
                ),
                Map.of(
                        "scriptId", TEST_SCRIPT_ID,
                        "characterId", TEST_CHARACTER_ID,
                        "type", "clue",
                        "content", "用于测试批量删除的线索2"
                )
        );
        List<Long> recordIds = ragService.batchInsertGlobalMemory(records);
        assertNotNull(recordIds, "插入的记录ID列表不应为null");
        assertFalse(recordIds.isEmpty(), "插入的记录ID列表不应为空");
        
        // 3. 测试批量删除
        int deletedCount = ragService.batchDeleteGlobalMemory(recordIds);
        
        // 验证
        assertTrue(deletedCount > 0, "删除的记录数量应大于0");
        assertEquals(recordIds.size(), deletedCount, "删除的记录数量应与输入一致");
        System.out.println("✓ 批量删除全局记忆成功，删除数量: " + deletedCount);
    }
}
