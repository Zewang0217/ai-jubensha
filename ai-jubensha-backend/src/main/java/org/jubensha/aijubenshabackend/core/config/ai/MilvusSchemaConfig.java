package org.jubensha.aijubenshabackend.core.config.ai;


import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * milvus的schema配置类
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-02 15:36
 * @since 2026
 */

@Component
public class MilvusSchemaConfig {

    @Value("${milvus.embedding-dimension:1024}")
    private Integer embeddingDimension;

    @Value("${milvus.collection-name:conversation_}")
    private String conversationPrefix;

    @Value("${milvus.collections.global-memory:global_memory}")
    private String globalMemoryCollection;

    @Resource(name = "milvusClient")
    private MilvusClientV2 client;

    /**
     * 获取对话记忆集合的 Schema
     */
    public CreateCollectionReq.CollectionSchema getConversationMemorySchema() {
        CreateCollectionReq.CollectionSchema schema =
                client.createSchema();

        // 主键字段
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());

        // 玩家ID
        schema.addField(AddFieldReq.builder()
                .fieldName("player_id")
                .dataType(DataType.Int64)
                .build());

        // 玩家名称
        schema.addField(AddFieldReq.builder()
                .fieldName("player_name")
                .dataType(DataType.VarChar)
                .maxLength(255)
                .build());

        // 对话内容
        schema.addField(AddFieldReq.builder()
                .fieldName("content")
                .dataType(DataType.VarChar)
                .maxLength(65535)
                .build());

        // 时间戳
        schema.addField(AddFieldReq.builder()
                .fieldName("timestamp")
                .dataType(DataType.Int64)
                .build());

        // 向量嵌入
        schema.addField(AddFieldReq.builder()
                .fieldName("embedding")
                .dataType(DataType.FloatVector)
                .dimension(embeddingDimension)
                .build());

        return schema;
    }

    public CreateCollectionReq.CollectionSchema getGlobalMemorySchema() {
        CreateCollectionReq.CollectionSchema schema =
                client.createSchema();

        // 主键字段
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());

        // 剧本ID
        schema.addField(AddFieldReq.builder()
                .fieldName("script_id")
                .dataType(DataType.Int64)
                .build());

        // 角色ID
        schema.addField(AddFieldReq.builder()
                .fieldName("character_id")
                .dataType(DataType.Int64)
                .build());

        // 数据类型（clue/timeline）
        schema.addField(AddFieldReq.builder()
                .fieldName("type")
                .dataType(DataType.VarChar)
                .maxLength(50)
                .build());

        // 内容
        schema.addField(AddFieldReq.builder()
                .fieldName("content")
                .dataType(DataType.VarChar)
                .maxLength(65535)
                .build());

        // 时间点（仅时间线使用）
        schema.addField(AddFieldReq.builder()
                .fieldName("timestamp")
                .dataType(DataType.VarChar)
                .maxLength(100)  // 格式化的日期时间字符串
                .build());

        // 向量嵌入
        schema.addField(AddFieldReq.builder()
                .fieldName("embedding")
                .dataType(DataType.FloatVector)
                .dimension(embeddingDimension)
                .build());

        return schema;
    }

    /**
     * 获取对话记忆集合名称（包含gameId）
     */
    public String getConversationCollectionName(Long gameId) {
        return conversationPrefix + gameId;
    }

    /**
     * 获取全局记忆集合名称
     */
    public String getGlobalMemoryCollectionName() {
        return globalMemoryCollection;
    }
}
