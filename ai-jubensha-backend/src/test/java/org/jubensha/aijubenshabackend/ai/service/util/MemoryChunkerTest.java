package org.jubensha.aijubenshabackend.ai.service.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 记忆分块工具类测试
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-03-02
 * @since 2026
 */
@SpringBootTest
public class MemoryChunkerTest {

    @Autowired
    private MemoryChunker memoryChunker;

    @Test
    public void testChunkMemory_ShortText() {
        // 测试短文本，应该不需要分块
        String shortText = "这是一个短文本，不需要分块处理。";
        List<String> chunks = memoryChunker.chunkMemory(shortText, MemoryChunker.MemoryType.CLUE);
        
        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        assertEquals(shortText, chunks.get(0));
    }

    @Test
    public void testChunkMemory_LongText() {
        // 测试长文本，应该进行分块
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("这是一段测试文本，用于测试长文本的分块功能。");
        }
        String text = longText.toString();
        
        List<String> chunks = memoryChunker.chunkMemory(text, MemoryChunker.MemoryType.CLUE);
        
        assertNotNull(chunks);
        assertTrue(chunks.size() > 1, "长文本应该被分块");
        
        // 验证所有分块的总长度与原始文本长度相同
        int totalLength = 0;
        for (String chunk : chunks) {
            assertNotNull(chunk);
            assertFalse(chunk.isEmpty());
            totalLength += chunk.length();
        }
        assertEquals(text.length(), totalLength, "分块后的总长度应该与原始文本长度相同");
    }

    @Test
    public void testChunkMemory_ClueType() {
        // 测试线索类型的分块
        String longText = "这是一个很长的线索文本，包含了很多细节信息，需要被分块处理。";
        for (int i = 0; i < 20; i++) {
            longText += " 这是额外的线索信息。";
        }
        
        List<String> chunks = memoryChunker.chunkMemory(longText, MemoryChunker.MemoryType.CLUE);
        
        assertNotNull(chunks);
        assertTrue(chunks.size() > 1, "线索长文本应该被分块");
    }

    @Test
    public void testChunkMemory_TimelineType() {
        // 测试时间线类型的分块
        String longText = "这是一个很长的时间线文本，包含了很多时间节点和事件，需要被分块处理。";
        for (int i = 0; i < 20; i++) {
            longText += " 10月1日：发生了重要事件。";
        }
        
        List<String> chunks = memoryChunker.chunkMemory(longText, MemoryChunker.MemoryType.TIMELINE);
        
        assertNotNull(chunks);
        assertTrue(chunks.size() > 1, "时间线长文本应该被分块");
    }

    @Test
    public void testNeedsChunking() {
        // 测试短文本不需要分块
        String shortText = "这是一个短文本";
        boolean needsChunking = memoryChunker.needsChunking(shortText, MemoryChunker.MemoryType.CLUE);
        assertFalse(needsChunking, "短文本不应该需要分块");
        
        // 测试长文本需要分块
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("这是一段测试文本。");
        }
        needsChunking = memoryChunker.needsChunking(longText.toString(), MemoryChunker.MemoryType.CLUE);
        assertTrue(needsChunking, "长文本应该需要分块");
    }

    @Test
    public void testChunkMemories() {
        // 测试批量分块
        String shortText = "这是一个短文本";
        StringBuilder longTextBuilder = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longTextBuilder.append("这是一段测试文本。");
        }
        String longText = longTextBuilder.toString();
        
        List<String> texts = List.of(shortText, longText);
        List<String> chunks = memoryChunker.chunkMemories(texts, MemoryChunker.MemoryType.CLUE);
        
        assertNotNull(chunks);
        assertTrue(chunks.size() > 2, "批量分块应该包含短文本和长文本的分块");
    }

    @Test
    public void testGetChunkConfig() {
        // 测试获取分块配置
        int[] clueConfig = memoryChunker.getChunkConfig(MemoryChunker.MemoryType.CLUE);
        assertNotNull(clueConfig);
        assertEquals(2, clueConfig.length);
        assertTrue(clueConfig[0] > 0, "分块大小应该大于0");
        assertTrue(clueConfig[1] > 0, "安全阈值应该大于0");
        
        int[] timelineConfig = memoryChunker.getChunkConfig(MemoryChunker.MemoryType.TIMELINE);
        assertNotNull(timelineConfig);
        assertEquals(2, timelineConfig.length);
        assertTrue(timelineConfig[0] > 0, "分块大小应该大于0");
        assertTrue(timelineConfig[1] > 0, "安全阈值应该大于0");
    }
}
