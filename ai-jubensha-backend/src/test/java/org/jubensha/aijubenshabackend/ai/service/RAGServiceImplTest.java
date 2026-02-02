package org.jubensha.aijubenshabackend.ai.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.response.SearchResp;
import org.jubensha.aijubenshabackend.core.config.ai.MilvusSchemaConfig;
import org.jubensha.aijubenshabackend.memory.MilvusCollectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RAGServiceImpl测试类
 * 测试语义搜索和基于线索的过滤查询功能
 */
class RAGServiceImplTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private MilvusClientV2 milvusClientV2;

    @Mock
    private MilvusCollectionManager collectionManager;

    @Mock
    private MilvusSchemaConfig schemaConfig;

    @InjectMocks
    private RAGServiceImpl ragService;

    private List<Float> mockEmbedding;
    private SearchResp mockSearchResp;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化模拟数据
        mockEmbedding = List.of(0.1f, 0.2f, 0.3f);
        
        // 模拟集合名称
        when(schemaConfig.getConversationCollectionName(1L)).thenReturn("conversation_1");
        when(schemaConfig.getGlobalMemoryCollectionName()).thenReturn("global_memory");
        
        // 模拟搜索响应
        mockSearchResp = mock(SearchResp.class);
        List<List<SearchResp.SearchResult>> searchResults = new ArrayList<>();
        List<SearchResp.SearchResult> queryResults = new ArrayList<>();
        
        // 创建模拟搜索结果
        SearchResp.SearchResult result1 = mock(SearchResp.SearchResult.class);
        when(result1.getScore()).thenReturn(0.1f);
        Map<String, Object> entity1 = new HashMap<>();
        entity1.put("id", 1L);
        entity1.put("player_id", 101L);
        entity1.put("player_name", "玩家1");
        entity1.put("content", "测试对话内容1");
        entity1.put("timestamp", 1234567890L);
        when(result1.getEntity()).thenReturn(entity1);
        
        SearchResp.SearchResult result2 = mock(SearchResp.SearchResult.class);
        when(result2.getScore()).thenReturn(0.2f);
        Map<String, Object> entity2 = new HashMap<>();
        entity2.put("id", 2L);
        entity2.put("player_id", 102L);
        entity2.put("player_name", "玩家2");
        entity2.put("content", "测试对话内容2");
        entity2.put("timestamp", 1234567891L);
        when(result2.getEntity()).thenReturn(entity2);
        
        queryResults.add(result1);
        queryResults.add(result2);
        searchResults.add(queryResults);
        
        when(mockSearchResp.getSearchResults()).thenReturn(searchResults);
    }

    @Test
    void searchConversationMemory_shouldReturnResults_whenCollectionExistsAndEmbeddingSuccess() {
        // 准备
        when(collectionManager.collectionExists("conversation_1")).thenReturn(true);
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行
        List<Map<String, Object>> results = ragService.searchConversationMemory(1L, 101L, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("玩家1", results.get(0).get("player_name"));
        assertEquals("玩家2", results.get(1).get("player_name"));
        assertTrue((Double) results.get(0).get("score") > (Double) results.get(1).get("score"));
        
        verify(collectionManager, times(1)).collectionExists("conversation_1");
        verify(embeddingService, times(1)).generateEmbedding("测试查询");
        verify(milvusClientV2, times(1)).search(any());
    }

    @Test
    void searchConversationMemory_shouldReturnEmptyList_whenCollectionDoesNotExist() {
        // 准备
        when(collectionManager.collectionExists("conversation_1")).thenReturn(false);

        // 执行
        List<Map<String, Object>> results = ragService.searchConversationMemory(1L, 101L, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(collectionManager, times(1)).collectionExists("conversation_1");
        verify(embeddingService, never()).generateEmbedding(anyString());
        verify(milvusClientV2, never()).search(any());
    }

    @Test
    void searchConversationMemory_shouldReturnEmptyList_whenEmbeddingFails() {
        // 准备
        when(collectionManager.collectionExists("conversation_1")).thenReturn(true);
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(null);

        // 执行
        List<Map<String, Object>> results = ragService.searchConversationMemory(1L, 101L, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(collectionManager, times(1)).collectionExists("conversation_1");
        verify(embeddingService, times(1)).generateEmbedding("测试查询");
        verify(milvusClientV2, never()).search(any());
    }

    @Test
    void searchConversationMemory_shouldReturnEmptyList_whenSearchRespIsNull() {
        // 准备
        when(collectionManager.collectionExists("conversation_1")).thenReturn(true);
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(null);

        // 执行
        List<Map<String, Object>> results = ragService.searchConversationMemory(1L, 101L, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(milvusClientV2, times(1)).search(any());
    }

    @Test
    void searchGlobalClueMemory_shouldReturnResults_whenEmbeddingSuccess() {
        // 准备
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行
        List<Map<String, Object>> results = ragService.searchGlobalClueMemory(1L, 201L, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(embeddingService, times(1)).generateEmbedding("测试查询");
        verify(milvusClientV2, times(1)).search(any());
    }

    @Test
    void searchGlobalTimelineMemory_shouldReturnResults_whenEmbeddingSuccess() {
        // 准备
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行
        List<Map<String, Object>> results = ragService.searchGlobalTimelineMemory(1L, 201L, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(embeddingService, times(1)).generateEmbedding("测试查询");
        verify(milvusClientV2, times(1)).search(any());
    }

    @Test
    void filterByDiscoveredClues_shouldReturnCombinedResults() {
        // 准备
        when(collectionManager.collectionExists("conversation_1")).thenReturn(true);
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行
        List<Long> discoveredClueIds = List.of(1L, 2L, 3L);
        List<Map<String, Object>> results = ragService.filterByDiscoveredClues(1L, 101L, discoveredClueIds, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
        // 验证结果按分数降序排列
        assertTrue((Double) results.get(0).get("score") > (Double) results.get(1).get("score"));
    }

    @Test
    void calculateClueRelationStrength_shouldReturnDefaultValue() {
        // 执行
        int strength = ragService.calculateClueRelationStrength(1L, 1L, 2L);

        // 验证
        assertEquals(50, strength);
    }

    @Test
    void filterByDiscoveredClues_shouldLimitResults() {
        // 准备
        when(collectionManager.collectionExists("conversation_1")).thenReturn(true);
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行 - 限制返回1条结果
        List<Long> discoveredClueIds = List.of(1L, 2L, 3L);
        List<Map<String, Object>> results = ragService.filterByDiscoveredClues(1L, 101L, discoveredClueIds, "测试查询", 1);

        // 验证
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void searchConversationMemory_shouldHandleNullPlayerId() {
        // 准备
        when(collectionManager.collectionExists("conversation_1")).thenReturn(true);
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行 - 传入null作为playerId
        List<Map<String, Object>> results = ragService.searchConversationMemory(1L, null, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void searchGlobalClueMemory_shouldHandleNullCharacterId() {
        // 准备
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行 - 传入null作为characterId
        List<Map<String, Object>> results = ragService.searchGlobalClueMemory(1L, null, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void searchGlobalTimelineMemory_shouldHandleNullCharacterId() {
        // 准备
        when(embeddingService.generateEmbedding("测试查询")).thenReturn(mockEmbedding);
        when(milvusClientV2.search(any())).thenReturn(mockSearchResp);

        // 执行 - 传入null作为characterId
        List<Map<String, Object>> results = ragService.searchGlobalTimelineMemory(1L, null, "测试查询", 5);

        // 验证
        assertNotNull(results);
        assertEquals(2, results.size());
    }
}
