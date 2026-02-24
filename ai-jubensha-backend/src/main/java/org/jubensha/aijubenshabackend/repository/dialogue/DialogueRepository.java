package org.jubensha.aijubenshabackend.repository.dialogue;

import org.jubensha.aijubenshabackend.models.entity.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话记录Repository接口
 * 提供对话记录的CRUD操作和查询方法
 * <p>
 * 用于父文档存储服务，支持按游戏ID查询对话记录
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-24
 * @since 2026
 */
@Repository
public interface DialogueRepository extends JpaRepository<Dialogue, Long> {

    /**
     * 根据游戏ID查询对话记录
     * <p>
     * 按时间戳倒序排列，返回最新的对话记录
     *
     * @param gameId 游戏ID
     * @param limit 限制返回数量
     * @return 对话记录列表
     */
    List<Dialogue> findByGameIdOrderByTimestampDesc(Long gameId, org.springframework.data.domain.Pageable pageable);

    /**
     * 根据游戏ID删除所有对话记录
     * <p>
     * 用于清理指定游戏的所有父文档
     *
     * @param gameId 游戏ID
     * @return 删除的记录数量
     */
    int deleteByGameId(Long gameId);

    /**
     * 根据ID列表批量查询对话记录
     * <p>
     * 用于批量获取父文档
     *
     * @param ids 对话记录ID列表
     * @return 对话记录列表
     */
    List<Dialogue> findByIdIn(List<Long> ids);
}
