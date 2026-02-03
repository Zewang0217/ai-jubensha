package org.jubensha.aijubenshabackend.memory;


import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.common.IndexParam.IndexType;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.CreateIndexReq.CreateIndexReqBuilder;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.core.config.ai.MilvusSchemaConfig;
import org.jubensha.aijubenshabackend.core.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mapping.model.IdPropertyIdentifierAccessor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;

/**
 * 管理milvus的collections的类
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-02 15:48
 * @since 2026
 */

@Service
@Slf4j
public class MilvusCollectionManager {

    private final MilvusClientV2 milvusClientV2;
    private final MilvusSchemaConfig schemaConfig;

    @Value("${milvus.metric-type:L2}")
    private String metricType;

    @Value("${milvus.index-type:HNSW}")
    private String indexType;

    @Autowired
    public MilvusCollectionManager(MilvusClientV2 milvusClientV2, MilvusSchemaConfig schemaConfig) {
        this.milvusClientV2 = milvusClientV2;
        this.schemaConfig = schemaConfig;

        // 初始化全局记忆集合
        initializeGlobalMemoryCollection();
    }

    // 为测试提供的构造函数，允许直接设置metricType
    public MilvusCollectionManager(MilvusClientV2 milvusClientV2, MilvusSchemaConfig schemaConfig, String metricType) {
        this.milvusClientV2 = milvusClientV2;
        this.schemaConfig = schemaConfig;
        this.metricType = metricType;

        // 初始化全局记忆集合
        initializeGlobalMemoryCollection();
    }

    /**
     * 初始化全局记忆集合
     */
    private void initializeGlobalMemoryCollection() {
        try {
            String collectionName = schemaConfig.getGlobalMemoryCollectionName();
            createCollectionIfNotExists(collectionName, schemaConfig.getGlobalMemorySchema());
            log.info("已初始化全局记忆集合：{}", collectionName);
        } catch (Exception e) {
            log.error("初始化 Milvus 集合失败：{}", e.getMessage(), e);
            throw new AppException(e.getMessage());
        }
    }

    /**
     * 初始化特定游戏的对话记忆集合
     */
    public void initializeConversationCollection(Long gameId) {
        try {
            String collectionName = schemaConfig.getConversationCollectionName(gameId);
            createCollectionIfNotExists(collectionName, schemaConfig.getConversationMemorySchema());
            log.info("游戏 {} 的对话记忆集合初始化完成: {}", gameId, collectionName);
        } catch (Exception e) {
            log.error("初始化游戏 {} 的对话记忆集合失败", gameId, e);
            throw new RuntimeException("对话记忆集合初始化失败", e);
        }
    }

    /**
     * 如果集合不存在则创建
     */
    private void createCollectionIfNotExists(String collectionName, CreateCollectionReq.CollectionSchema schema) {
        Boolean hasCollection = milvusClientV2.hasCollection(
            HasCollectionReq.builder()
                .collectionName(collectionName)
                .build()
        );

        if (hasCollection) {
            log.info("集合 {} 已经存在", collectionName);
            return;
        }

        // 索引添加
        IndexParam indexParamForEmbeddingField = IndexParam.builder()
            .fieldName("embedding")
            .indexName(collectionName + "_embedding" + "_idx")
            .metricType(MetricType.valueOf(metricType != null ? metricType : "L2"))
            .indexType(IndexType.HNSW)
            .extraParams(Map.of(
                "M", 16,
                "efConstruction", 200
            ))
            .build();
        log.info("创建embedding索引：{}", indexParamForEmbeddingField);

        IndexParam indexParamForIdField = IndexParam.builder()
            .fieldName("id")
            .indexType(IndexType.STL_SORT)
            .indexName(collectionName + "_id" + "_idx")
            .build();

        // 创建集合
        CreateCollectionReq request = CreateCollectionReq.builder()
            .collectionName(collectionName)
            .collectionSchema(schema)
            .indexParams(List.of(
                indexParamForEmbeddingField, indexParamForIdField))
            .build();

        // 执行创建操作
        milvusClientV2.createCollection(request);
        log.info("集合 {} 创建成功", collectionName);

        //  查询状态
        GetLoadStateReq getLoadStateReq = GetLoadStateReq.builder()
            .collectionName(collectionName)
            .build();
        Boolean loaded = milvusClientV2.getLoadState(getLoadStateReq);
        log.info("集合 {} 的加载状态为：{}", collectionName, loaded);
    }

    /**
     * 删除特定游戏的对话记忆集合
     */
    public void dropConversationCollection(Long gameId) {
        String collectionName = schemaConfig.getConversationCollectionName(gameId);

        try {
            // 释放集合（从内存中卸载
            milvusClientV2.releaseCollection(ReleaseCollectionReq.builder()
                .collectionName(collectionName)
                .build());

            // 删除集合
            milvusClientV2.dropCollection(DropCollectionReq.builder()
                .collectionName(collectionName)
                .build());
            log.info("已删除游戏 {} 的对话记忆集合: {}", gameId, collectionName);
        } catch (Exception e) {
            log.error("删除游戏 {} 的对话记忆集合失败: {}", gameId, collectionName, e);
        }
    }

    /**
     * 检查集合是否存在
     */
    public boolean collectionExists(String collectionName) {
        try {
            return milvusClientV2.hasCollection(HasCollectionReq.builder()
                .collectionName(collectionName)
                .build());
        } catch (Exception e) {
            log.error("检查集合 {} 是否存在时出错", collectionName, e);
            return false;
        }
    }
}
