package org.jubensha.aijubenshabackend.ai.service;

/**
 * 图片存储服务接口
 * 提供远程图片下载并保存到本地的功能
 *
 * @author zewang
 */
public interface ImageStorageService {

    /**
     * 下载远程图片并存储到本地
     *
     * @param imageUrl   远程图片URL
     * @param scriptName 剧本名称，用于生成文件名
     * @return 本地访问URL，格式为 /api/images/covers/{filename}；下载失败时返回默认图片路径
     * @throws IllegalArgumentException 当imageUrl或scriptName为空时抛出
     */
    String downloadAndStore(String imageUrl, String scriptName);
}
