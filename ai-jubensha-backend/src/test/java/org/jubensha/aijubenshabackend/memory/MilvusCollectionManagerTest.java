package org.jubensha.aijubenshabackend.memory;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import org.jubensha.aijubenshabackend.core.config.ai.MilvusSchemaConfig;
import org.jubensha.aijubenshabackend.core.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MilvusCollectionManager测试类
 * 测试Milvus集合管理功能
 */
class MilvusCollectionManagerTest {

    @Mock
    private MilvusClientV2 milvusClientV2;

    @Mock
    private MilvusSchemaConfig schemaConfig;

    private CreateCollectionReq.CollectionSchema mockGlobalSchema;
    private CreateCollectionReq.CollectionSchema mockConversationSchema;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 初始化模拟数据
        mockGlobalSchema = mock(CreateCollectionReq.CollectionSchema.class);
        mockConversationSchema = mock(CreateCollectionReq.CollectionSchema.class);
        
        // 模拟集合名称和schema
        when(schemaConfig.getGlobalMemoryCollectionName()).thenReturn("global_memory");
        when(schemaConfig.getGlobalMemorySchema()).thenReturn(mockGlobalSchema);
        when(schemaConfig.getConversationCollectionName(1L)).thenReturn("conversation_1");
        when(schemaConfig.getConversationMemorySchema()).thenReturn(mockConversationSchema);
        
        // 模拟集合检查结果 - 先返回true，避免创建集合的逻辑
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenReturn(true);
        
        // 模拟加载状态
        when(milvusClientV2.getLoadState(any(GetLoadStateReq.class))).thenReturn(false);
    }

    @Test
    void constructor_shouldInitializeGlobalMemoryCollection() {
        // 执行 - 构造函数会调用initializeGlobalMemoryCollection
        MilvusCollectionManager manager = new MilvusCollectionManager(milvusClientV2, schemaConfig, "L2");
        
        // 验证
        verify(milvusClientV2, times(1)).hasCollection(any(HasCollectionReq.class));
        verify(schemaConfig, times(1)).getGlobalMemoryCollectionName();
        verify(schemaConfig, times(1)).getGlobalMemorySchema();
    }

    @Test
    void constructor_shouldThrowException_whenInitializationFails() {
        // 准备 - 模拟检查集合时抛出异常
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenThrow(new RuntimeException("检查集合失败"));
        
        // 验证
        assertThrows(AppException.class, () -> {
            new MilvusCollectionManager(milvusClientV2, schemaConfig, "L2");
        });
    }

    @Test
    void initializeConversationCollection_shouldSucceed() {
        // 准备 - 先创建一个实例（会初始化全局集合）
        // 重置mock以避免干扰
        reset(milvusClientV2);
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenReturn(false);
        when(milvusClientV2.getLoadState(any(GetLoadStateReq.class))).thenReturn(false);
        
        // 创建实例
        MilvusCollectionManager manager = new MilvusCollectionManager(milvusClientV2, schemaConfig, "L2");
        
        // 重置mock以验证新的调用
        reset(milvusClientV2);
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenReturn(false);
        when(milvusClientV2.getLoadState(any(GetLoadStateReq.class))).thenReturn(false);
        
        // 执行
        manager.initializeConversationCollection(1L);
        
        // 验证
        verify(milvusClientV2, times(1)).hasCollection(any(HasCollectionReq.class));
        verify(schemaConfig, times(1)).getConversationCollectionName(1L);
        verify(schemaConfig, times(1)).getConversationMemorySchema();
    }

    @Test
    void initializeConversationCollection_shouldThrowException_whenFailed() {
        // 准备 - 先创建一个实例
        MilvusCollectionManager manager = new MilvusCollectionManager(milvusClientV2, schemaConfig, "L2");
        
        // 重置mock并模拟异常
        reset(milvusClientV2);
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenThrow(new RuntimeException("检查集合失败"));
        
        // 验证
        assertThrows(RuntimeException.class, () -> {
            manager.initializeConversationCollection(1L);
        });
    }

    @Test
    void collectionExists_shouldReturnTrue_whenCollectionExists() {
        // 准备 - 创建实例
        MilvusCollectionManager manager = new MilvusCollectionManager(milvusClientV2, schemaConfig, "L2");
        
        // 重置mock并设置返回值
        reset(milvusClientV2);
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenReturn(true);
        
        // 执行
        boolean exists = manager.collectionExists("test_collection");
        
        // 验证
        assertTrue(exists);
        verify(milvusClientV2, times(1)).hasCollection(any(HasCollectionReq.class));
    }

    @Test
    void collectionExists_shouldReturnFalse_whenCollectionDoesNotExist() {
        // 准备 - 创建实例
        MilvusCollectionManager manager = new MilvusCollectionManager(milvusClientV2, schemaConfig, "L2");
        
        // 重置mock并设置返回值
        reset(milvusClientV2);
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenReturn(false);
        
        // 执行
        boolean exists = manager.collectionExists("test_collection");
        
        // 验证
        assertFalse(exists);
        verify(milvusClientV2, times(1)).hasCollection(any(HasCollectionReq.class));
    }

    @Test
    void collectionExists_shouldReturnFalse_whenErrorOccurs() {
        // 准备 - 创建实例
        MilvusCollectionManager manager = new MilvusCollectionManager(milvusClientV2, schemaConfig, "L2");
        
        // 重置mock并模拟异常
        reset(milvusClientV2);
        when(milvusClientV2.hasCollection(any(HasCollectionReq.class))).thenThrow(new RuntimeException("检查失败"));
        
        // 执行
        boolean exists = manager.collectionExists("test_collection");
        
        // 验证
        assertFalse(exists);
        verify(milvusClientV2, times(1)).hasCollection(any(HasCollectionReq.class));
    }
}