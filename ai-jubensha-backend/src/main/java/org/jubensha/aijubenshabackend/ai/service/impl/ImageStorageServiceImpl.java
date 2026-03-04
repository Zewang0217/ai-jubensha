package org.jubensha.aijubenshabackend.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 图片存储服务实现类
 * 负责下载远程图片并保存到本地文件系统
 *
 * @author zewang
 */
@Slf4j
@Service
public class ImageStorageServiceImpl implements ImageStorageService {

    private final RestTemplate restTemplate;

    @Value("${image.storage.path:./uploads/images/covers}")
    private String storagePath;

    @Value("${image.storage.url-prefix:/api/images/covers}")
    private String urlPrefix;

    @Value("${image.storage.default-cover:/api/images/covers/default-cover.jpg}")
    private String defaultCoverPath;

    public ImageStorageServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String downloadAndStore(String imageUrl, String scriptName) {
        // 参数校验
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            log.warn("图片URL为空，返回默认封面");
            return defaultCoverPath;
        }
        if (scriptName == null || scriptName.trim().isEmpty()) {
            log.warn("剧本名称为空，返回默认封面");
            return defaultCoverPath;
        }

        try {
            log.info("开始下载图片: {}, 剧本: {}", imageUrl, scriptName);
            return downloadAndSaveImage(imageUrl, scriptName);
        } catch (Exception e) {
            log.error("下载图片失败: {}, 错误: {}", imageUrl, e.getMessage(), e);
            return defaultCoverPath;
        }
    }

    /**
     * 下载并保存图片到本地
     *
     * @param imageUrl   远程图片URL
     * @param scriptName 剧本名称
     * @return 本地访问URL
     * @throws IOException 文件操作异常
     */
    private String downloadAndSaveImage(String imageUrl, String scriptName) throws IOException {
        // Step 1: 下载图片字节数据
        byte[] imageBytes = downloadImageBytes(imageUrl);
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IOException("下载的图片数据为空");
        }

        // Step 2: 确保存储目录存在
        Path storageDir = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
            log.info("创建图片存储目录: {}", storageDir);
        }

        // Step 3: 生成唯一文件名并保存
        String filename = generateUniqueFilename(scriptName, imageUrl);
        Path filePath = storageDir.resolve(filename);
        Files.write(filePath, imageBytes);

        log.info("图片保存成功: {}, 大小: {} bytes", filePath, imageBytes.length);
        return urlPrefix + "/" + filename;
    }

    /**
     * 下载图片字节数据
     *
     * @param imageUrl 图片URL
     * @return 图片字节数组
     */
    private byte[] downloadImageBytes(String imageUrl) {
        try {
            return restTemplate.getForObject(imageUrl, byte[].class);
        } catch (RestClientException e) {
            log.error("调用远程图片URL失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成唯一文件名
     * 格式: scriptName_timestamp_uuid.extension
     *
     * @param scriptName 剧本名称
     * @param imageUrl   图片URL（用于提取扩展名）
     * @return 唯一文件名
     */
    private String generateUniqueFilename(String scriptName, String imageUrl) {
        // 清理剧本名称中的特殊字符
        String cleanName = scriptName.replaceAll("[\\\\/:*?\"<>|\\s]", "_");
        if (cleanName.length() > 20) {
            cleanName = cleanName.substring(0, 20);
        }

        // 生成时间戳
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // 生成UUID短版本
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // 提取扩展名
        String extension = extractExtension(imageUrl);

        return String.format("%s_%s_%s.%s", cleanName, timestamp, uuid, extension);
    }

    /**
     * 从URL中提取文件扩展名
     *
     * @param url 图片URL
     * @return 文件扩展名，默认为jpg
     */
    private String extractExtension(String url) {
        if (url == null) {
            return "jpg";
        }

        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains(".png")) {
            return "png";
        } else if (lowerUrl.contains(".gif")) {
            return "gif";
        } else if (lowerUrl.contains(".webp")) {
            return "webp";
        } else if (lowerUrl.contains(".jpeg") || lowerUrl.contains(".jpg")) {
            return "jpg";
        }

        return "jpg";
    }
}
