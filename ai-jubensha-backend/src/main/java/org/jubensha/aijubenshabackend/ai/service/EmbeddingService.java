package org.jubensha.aijubenshabackend.ai.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 嵌入服务，用于生成文本向量嵌入
 * <p>
 * 注意：以下方法需要与Milvus向量数据库配合使用：
 * 1. generateEmbedding：生成的向量嵌入会存储到Milvus向量数据库
 * 2. generateEmbeddings：生成的批量向量嵌入会存储到Milvus向量数据库
 * 3. generateEmbeddingsFromTextSegments：生成的文本段向量嵌入会存储到Milvus向量数据库
 */
@Service
public class EmbeddingService {

    @Resource(name = "embeddingModel")
    private EmbeddingModel embeddingModel;

//    @Autowired
//    public EmbeddingService(EmbeddingModel embeddingModel) {
//        this.embeddingModel = embeddingModel;
//    }

    /**
     * 生成单个文本的嵌入向量
     *
     * @param text 输入文本
     * @return 嵌入向量
     */
    public List<Float> generateEmbedding(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        var embedding = embeddingModel.embed(TextSegment.from(text)).content();
        return embedding.vectorAsList();
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

        return embeddingModel.embedAll(texts.stream()
                        .map(TextSegment::from)
                        .toList())
                .content()
                .stream()
                .map(embedding -> embedding.vectorAsList())
                .toList();
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

        return embeddingModel.embedAll(textSegments)
                .content()
                .stream()
                .map(embedding -> embedding.vectorAsList())
                .toList();
    }
}