package org.jubensha.aijubenshabackend.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * ParentDocumentService单元测试
 */
@Slf4j
@SpringBootTest
public class ParentDocumentServiceTest {

    @Autowired
    private ParentDocumentService parentDocumentService;

    private Long testGameId = 1L;
    private Long testPlayerId = 1L;
    private String testPlayerName = "测试玩家";
    private String testContent = "昨晚8点，我在花园里看到了张三，他手里拿着一把刀，看起来很紧张。后来我听说李四下半夜在自己房间被杀害了，现场有一把带血的刀。";

    @Test
    void testStoreParentDocument() {
        log.info("测试存储父文档");
        
        Long parentId = parentDocumentService.storeParentDocument(testGameId, testPlayerId, testPlayerName, testContent, 3);
        
        assert parentId != null;
        log.info("存储父文档成功，ID: {}", parentId);
    }

    @Test
    void testGetParentDocument() {
        log.info("测试获取父文档");
        
        // 先存储一个父文档
        Long parentId = parentDocumentService.storeParentDocument(testGameId, testPlayerId, testPlayerName, testContent, 3);
        assert parentId != null;
        
        // 获取父文档
        Map<String, Object> parentDoc = parentDocumentService.getParentDocument(parentId);
        
        assert parentDoc != null;
        assert parentId.equals(parentDoc.get("id"));
        assert testContent.equals(parentDoc.get("content"));
        assert testGameId.equals(parentDoc.get("game_id"));
        assert testPlayerId.equals(parentDoc.get("player_id"));
        assert testPlayerName.equals(parentDoc.get("player_name"));
        
        log.info("获取父文档成功，ID: {}, 内容长度: {}", parentId, ((String) parentDoc.get("content")).length());
    }

    @Test
    void testGetParentDocuments() {
        log.info("测试批量获取父文档");
        
        // 存储多个父文档
        List<Long> parentIds = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String content = testContent + " (测试" + i + ")";
            Long parentId = parentDocumentService.storeParentDocument(testGameId, testPlayerId, testPlayerName, content, 3);
            assert parentId != null;
            parentIds.add(parentId);
        }
        
        // 批量获取
        List<Map<String, Object>> parentDocs = parentDocumentService.getParentDocuments(parentIds);
        
        assert parentDocs != null;
        assert parentDocs.size() == parentIds.size();
        
        log.info("批量获取父文档成功，数量: {}", parentDocs.size());
    }

    @Test
    void testGetParentDocumentsByGameId() {
        log.info("测试根据游戏ID获取父文档");
        
        // 存储测试数据
        parentDocumentService.storeParentDocument(testGameId, testPlayerId, testPlayerName, testContent, 3);
        
        // 获取父文档
        List<Map<String, Object>> parentDocs = parentDocumentService.getParentDocumentsByGameId(testGameId, 10);
        
        assert parentDocs != null;
        assert !parentDocs.isEmpty();
        
        log.info("根据游戏ID获取父文档成功，数量: {}", parentDocs.size());
    }

    @Test
    void testUpdateParentDocument() {
        log.info("测试更新父文档");
        
        // 存储父文档
        Long parentId = parentDocumentService.storeParentDocument(testGameId, testPlayerId, testPlayerName, testContent, 3);
        assert parentId != null;
        
        // 更新内容
        String newContent = "更新后的内容：昨晚9点，我在客厅看到了李四。";
        boolean updated = parentDocumentService.updateParentDocument(parentId, newContent);
        
        assert updated;
        
        // 验证更新
        Map<String, Object> parentDoc = parentDocumentService.getParentDocument(parentId);
        assert parentDoc != null;
        assert newContent.equals(parentDoc.get("content"));
        
        log.info("更新父文档成功，ID: {}", parentId);
    }

    @Test
    void testDeleteParentDocument() {
        log.info("测试删除父文档");
        
        // 存储父文档
        Long parentId = parentDocumentService.storeParentDocument(testGameId, testPlayerId, testPlayerName, testContent, 3);
        assert parentId != null;
        
        // 删除父文档
        boolean deleted = parentDocumentService.deleteParentDocument(parentId);
        
        assert deleted;
        
        // 验证删除
        Map<String, Object> parentDoc = parentDocumentService.getParentDocument(parentId);
        assert parentDoc == null;
        
        log.info("删除父文档成功，ID: {}", parentId);
    }

    @Test
    void testDeleteParentDocumentsByGameId() {
        log.info("测试根据游戏ID删除父文档");
        
        // 存储测试数据
        parentDocumentService.storeParentDocument(testGameId, testPlayerId, testPlayerName, testContent, 3);
        
        // 删除
        int deletedCount = parentDocumentService.deleteParentDocumentsByGameId(testGameId);
        
        log.info("根据游戏ID删除父文档，删除数量: {}", deletedCount);
        
        // 验证删除
        List<Map<String, Object>> parentDocs = parentDocumentService.getParentDocumentsByGameId(testGameId, 10);
        assert parentDocs != null;
        assert parentDocs.isEmpty();
    }
}
