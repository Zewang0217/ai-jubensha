package org.jubensha.aijubenshabackend.ai.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.util.TokenUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 嵌入服务，用于生成文本向量嵌入
 * <p>
 * 注意：以下方法需要与Milvus向量数据库配合使用：
 * 1. generateEmbedding：生成的向量嵌入会存储到Milvus向量数据库
 * 2. generateEmbeddings：生成的批量向量嵌入会存储到Milvus向量数据库
 * 3. generateEmbeddingsFromTextSegments：生成的文本段向量嵌入会存储到Milvus向量数据库
 */
@Slf4j
@Service
public class EmbeddingService {

    @Resource(name = "embeddingModel")
    private EmbeddingModel embeddingModel;

    @Resource
    private TokenUtils tokenUtils;

//    @Autowired
//    public EmbeddingService(EmbeddingModel embeddingModel) {
//        this.embeddingModel = embeddingModel;
//    }

    /**
     * 生成单个文本的嵌入向量
     * 自动处理token限制，超长文本会被智能截断
     *
     * @param text 输入文本
     * @return 嵌入向量
     */
    public List<Float> generateEmbedding(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        try {
            // 检查并处理超长文本
            String processedText = processTextForEmbedding(text);

            // 生成嵌入向量
            var embedding = embeddingModel.embed(TextSegment.from(processedText)).content();
            return embedding.vectorAsList();
        } catch (Exception e) {
            log.error("生成嵌入向量失败: {}", e.getMessage(), e);
            // 尝试降级策略
            return generateEmbeddingWithFallback(text);
        }
    }

    /**
     * 批量生成文本嵌入向量
     *
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    public List<List<Float>> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return null;
        }

        try {
            // 处理每个文本，确保不超过token限制
            List<String> processedTexts = new ArrayList<>();
            for (String text : texts) {
                processedTexts.add(processTextForEmbedding(text));
            }

            return embeddingModel.embedAll(processedTexts.stream()
                            .map(TextSegment::from)
                            .toList())
                    .content()
                    .stream()
                    .map(embedding -> embedding.vectorAsList())
                    .toList();
        } catch (Exception e) {
            log.error("批量生成嵌入向量失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成文本段的嵌入向量
     *
     * @param textSegments 文本段列表
     * @return 嵌入向量列表
     */
    public List<List<Float>> generateEmbeddingsFromTextSegments(List<TextSegment> textSegments) {
        if (textSegments == null || textSegments.isEmpty()) {
            return null;
        }

        try {
            // 处理每个文本段，确保不超过token限制
            List<TextSegment> processedSegments = new ArrayList<>();
            for (TextSegment segment : textSegments) {
                String processedText = processTextForEmbedding(segment.text());
                processedSegments.add(TextSegment.from(processedText, segment.metadata()));
            }

            return embeddingModel.embedAll(processedSegments)
                    .content()
                    .stream()
                    .map(embedding -> embedding.vectorAsList())
                    .toList();
        } catch (Exception e) {
            log.error("生成文本段嵌入向量失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 为文本块生成嵌入向量
     * 用于处理超长文本的分块嵌入
     *
     * @param chunks 文本块列表
     * @return 嵌入向量列表
     */
    public List<List<Float>> generateEmbeddingsForChunks(List<String> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return null;
        }

        try {
            return generateEmbeddings(chunks);
        } catch (Exception e) {
            log.error("生成文本块嵌入向量失败: {}", e.getMessage(), e);
            // 尝试逐个处理
            List<List<Float>> result = new ArrayList<>();
            for (String chunk : chunks) {
                List<Float> embedding = generateEmbedding(chunk);
                if (embedding != null) {
                    result.add(embedding);
                } else {
                    result.add(null);
                }
            }
            return result;
        }
    }

    /**
     * 处理文本以适应嵌入模型的token限制
     *
     * @param text 原始文本
     * @return 处理后的文本
     */
    private String processTextForEmbedding(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 检查token数量
        if (tokenUtils.exceedsSafeThreshold(text)) {
            log.warn("文本长度超过安全阈值，进行智能截断，原始长度: {}", text.length());
            return tokenUtils.truncateText(text);
        }

        return text;
    }

    /**
     * 降级策略：当常规嵌入失败时使用
     *
     * @param text 输入文本
     * @return 嵌入向量
     */
    private List<Float> generateEmbeddingWithFallback(String text) {
        try {
            // 等待一段时间后重试
            TimeUnit.MILLISECONDS.sleep(500);

            // 使用更激进的截断
            String truncatedText = tokenUtils.truncateText(text, 300);
            log.warn("使用降级策略，文本已截断至: {}", truncatedText.length());

            var embedding = embeddingModel.embed(TextSegment.from(truncatedText)).content();
            return embedding.vectorAsList();
        } catch (Exception e) {
            log.error("降级策略失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 检查文本是否适合嵌入
     *
     * @param text 输入文本
     * @return 是否适合嵌入
     */
    public boolean isTextSuitableForEmbedding(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return !tokenUtils.exceedsTokenLimit(text);
    }

    /**
     * 估算文本的token数量
     *
     * @param text 输入文本
     * @return token数量
     */
    public int estimateTokenCount(String text) {
        return tokenUtils.estimateTokens(text);
    }
}