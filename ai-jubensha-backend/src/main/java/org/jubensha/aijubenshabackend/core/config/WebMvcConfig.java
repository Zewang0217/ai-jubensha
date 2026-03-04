package org.jubensha.aijubenshabackend.core.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置类
 * 
 * 负责配置静态资源映射，特别是图片存储路径的映射。
 * 将HTTP请求路径映射到文件系统目录，实现图片资源的访问。
 * 
 * @author zewang
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 图片存储路径，从配置文件中读取
     */
    @Value("${image.storage.path}")
    private String imageStoragePath;

    /**
     * 图片URL访问前缀，从配置文件中读取
     */
    @Value("${image.storage.url-prefix}")
    private String imageUrlPrefix;

    /**
     * 应用启动时自动创建图片存储目录
     * 
     * @throws IOException 当目录创建失败时抛出异常
     */
    @PostConstruct
    public void init() throws IOException {
        Path storagePath = Paths.get(imageStoragePath);
        
        // 如果目录不存在，则创建目录（包括所有不存在的父目录）
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
    }

    /**
     * 配置静态资源映射
     * 
     * 将图片URL前缀映射到文件系统的图片存储目录。
     * 例如：/api/images/covers/** -> file:./uploads/images/covers/
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取存储路径的绝对路径
        Path storagePath = Paths.get(imageStoragePath).toAbsolutePath().normalize();
        
        // 添加资源处理器，将URL路径映射到文件系统路径
        registry.addResourceHandler(imageUrlPrefix + "/**")
                .addResourceLocations("file:" + storagePath.toString() + "/");
    }
}
