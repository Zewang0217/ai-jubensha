package org.jubensha.aijubenshabackend.ai.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 评分数据管理器
 * 用于管理和传递评分数据，基于内存存储
 *
 * @author Zewang
 * @version 1.0
 * @date 2026-02-24
 * @since 2026
 */
@Slf4j
@Component
public class ScoreDataManager {

    // 存储游戏评分数据，key为游戏ID，value为评分数据JSON字符串
    private final Map<Long, String> scoreDataMap = new ConcurrentHashMap<>();

    /**
     * 存储评分数据
     *
     * @param gameId   游戏ID
     * @param scoreData 评分数据JSON字符串
     */
    public void storeScoreData(Long gameId, String scoreData) {
        scoreDataMap.put(gameId, scoreData);
        log.info("已存储游戏 {} 的评分数据，数据长度: {}", gameId, scoreData.length());
    }

    /**
     * 获取评分数据
     *
     * @param gameId 游戏ID
     * @return 评分数据JSON字符串，如果不存在则返回null
     */
    public String getScoreData(Long gameId) {
        String scoreData = scoreDataMap.get(gameId);
        if (scoreData != null) {
            log.info("获取游戏 {} 的评分数据，数据长度: {}", gameId, scoreData.length());
        } else {
            log.warn("游戏 {} 的评分数据不存在", gameId);
        }
        return scoreData;
    }

    /**
     * 删除评分数据
     *
     * @param gameId 游戏ID
     * @return 是否删除成功
     */
    public boolean removeScoreData(Long gameId) {
        String removedData = scoreDataMap.remove(gameId);
        if (removedData != null) {
            log.info("已删除游戏 {} 的评分数据", gameId);
            return true;
        } else {
            log.warn("游戏 {} 的评分数据不存在，删除失败", gameId);
            return false;
        }
    }

    /**
     * 检查评分数据是否存在
     *
     * @param gameId 游戏ID
     * @return 是否存在
     */
    public boolean hasScoreData(Long gameId) {
        boolean exists = scoreDataMap.containsKey(gameId);
        log.debug("游戏 {} 的评分数据存在: {}", gameId, exists);
        return exists;
    }

    /**
     * 清空所有评分数据
     */
    public void clearAllScoreData() {
        int size = scoreDataMap.size();
        scoreDataMap.clear();
        log.info("已清空所有评分数据，共删除 {} 条记录", size);
    }

    /**
     * 获取当前存储的评分数据数量
     *
     * @return 数据数量
     */
    public int getScoreDataCount() {
        int count = scoreDataMap.size();
        log.debug("当前存储的评分数据数量: {}", count);
        return count;
    }
}
