package org.jubensha.aijubenshabackend.ai.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EmbeddingService测试类
 * 测试文本向量嵌入生成功能
 */
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 手动设置embeddingModel，因为EmbeddingService使用@Resource注入
        embeddingService = new EmbeddingService();
        // 使用反射设置私有字段
        try {
            var field = EmbeddingService.class.getDeclaredField("embeddingModel");
            field.setAccessible(true);
            field.set(embeddingService, embeddingModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateEmbedding_shouldReturnNull_whenTextIsEmpty() {
        // 执行
        List<Float> result = embeddingService.generateEmbedding("");

        // 验证
        assertNull(result);
        verify(embeddingModel, never()).embed(any(TextSegment.class));
    }

    @Test
    void generateEmbedding_shouldReturnNull_whenTextIsNull() {
        // 执行
        List<Float> result = embeddingService.generateEmbedding(null);

        // 验证
        assertNull(result);
        verify(embeddingModel, never()).embed(any(TextSegment.class));
    }

    @Test
    void generateEmbeddings_shouldReturnNull_whenTextsIsEmpty() {
        // 执行
        List<List<Float>> result = embeddingService.generateEmbeddings(List.of());

        // 验证
        assertNull(result);
        verify(embeddingModel, never()).embedAll(anyList());
    }

    @Test
    void generateEmbeddings_shouldReturnNull_whenTextsIsNull() {
        // 执行
        List<List<Float>> result = embeddingService.generateEmbeddings(null);

        // 验证
        assertNull(result);
        verify(embeddingModel, never()).embedAll(anyList());
    }

    @Test
    void generateEmbeddingsFromTextSegments_shouldReturnNull_whenSegmentsIsEmpty() {
        // 执行
        List<List<Float>> result = embeddingService.generateEmbeddingsFromTextSegments(List.of());

        // 验证
        assertNull(result);
        verify(embeddingModel, never()).embedAll(anyList());
    }

    @Test
    void generateEmbeddingsFromTextSegments_shouldReturnNull_whenSegmentsIsNull() {
        // 执行
        List<List<Float>> result = embeddingService.generateEmbeddingsFromTextSegments(null);

        // 验证
        assertNull(result);
        verify(embeddingModel, never()).embedAll(anyList());
    }
}

