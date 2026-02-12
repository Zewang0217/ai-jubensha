package org.jubensha.aijubenshabackend.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.client.SiliconFlowClient;
import org.jubensha.aijubenshabackend.ai.service.ImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 图像生成服务实现类
 * 使用SiliconFlow API生成剧本封面图片
 */
@Slf4j
@Service
public class ImageGenerationServiceImpl implements ImageGenerationService {

    private final SiliconFlowClient siliconFlowClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.image-generation-model:stabilityai/stable-diffusion-3-medium}")
    private String imageGenerationModel;

    @Value("${ai.image-generation-base-url:https://api.siliconflow.cn/v1}")
    private String imageGenerationBaseUrl;

    @Value("${ai.image-generation-api-key}")
    private String imageGenerationApiKey;

    @Autowired
    public ImageGenerationServiceImpl(SiliconFlowClient siliconFlowClient, ObjectMapper objectMapper) {
        this.siliconFlowClient = siliconFlowClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateScriptCoverImage(String scriptName, String scriptDescription, String scriptGenre) {
        try {
            log.info("开始生成剧本封面图片: {}, 类型: {}", scriptName, scriptGenre);

            // 构建图像生成提示词
            String prompt = buildImagePrompt(scriptName, scriptDescription, scriptGenre);
            
            // 构建请求体
            Map<String, Object> requestBody = buildImageGenerationRequest(prompt);

            // 调用API
            Map<String, Object> response = siliconFlowClient.callImageGenerationApi(
                    imageGenerationBaseUrl, imageGenerationApiKey, requestBody);

            // 解析响应获取图片URL
            String imageUrl = extractImageUrlFromResponse(response);
            
            log.info("剧本封面图片生成成功: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("生成剧本封面图片失败: {}", e.getMessage(), e);
            // 返回默认图片URL作为fallback
            return getDefaultCoverImageUrl();
        }
    }

    @Override
    public Flux<String> generateScriptCoverImageStream(String scriptName, String scriptDescription, String scriptGenre) {
        return Flux.defer(() -> {
            try {
                log.info("开始流式生成剧本封面图片: {}, 类型: {}", scriptName, scriptGenre);

                String prompt = buildImagePrompt(scriptName, scriptDescription, scriptGenre);
                Map<String, Object> requestBody = buildImageGenerationRequest(prompt);

                // 发送请求并处理响应流
                return Mono.fromCallable(() -> {
                    Map<String, Object> response = siliconFlowClient.callImageGenerationApi(
                            imageGenerationBaseUrl, imageGenerationApiKey, requestBody);
                    return extractImageUrlFromResponse(response);
                })
                .flux()
                .onErrorResume(error -> {
                    log.error("流式生成剧本封面图片失败: {}", error.getMessage(), error);
                    return Flux.just(getDefaultCoverImageUrl());
                });

            } catch (Exception e) {
                log.error("初始化流式生成失败: {}", e.getMessage(), e);
                return Flux.just(getDefaultCoverImageUrl());
            }
        });
    }

    @Override
    public void generateScriptCoverImageAsync(String scriptName, String scriptDescription, String scriptGenre, 
                                            ImageGenerationCallback callback) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return generateScriptCoverImage(scriptName, scriptDescription, scriptGenre);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(imageUrl -> {
            if (callback != null) {
                callback.onSuccess(imageUrl);
            }
        }).exceptionally(error -> {
            if (callback != null) {
                callback.onError(new Exception(error));
            }
            return null;
        });
    }


    /**
     * 构建图像生成提示词 (Kwai-Kolors 模型专用优化版)
     * 策略：利用Kolors强大的中文理解能力，使用中英混合Prompt，以中文为主。
     */
    private String buildImagePrompt(String scriptName, String scriptDescription, String scriptGenre) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的剧本杀封面绘画师,需要生成一张符合下面描述剧本杀封面,为竖版构图 ");
        prompt.append("8k分辨率, 极其精致的细节, 商业插画, ");

        // 画面描述
        if (scriptDescription != null && !scriptDescription.isEmpty()) {
            // 清理换行符
            String cleanDesc = scriptDescription.replaceAll("[\\r\\n]", " ").trim();
            if (cleanDesc.length() > 300) {
                cleanDesc = cleanDesc.substring(0, 300);
            }
            // 直接告诉模型这是画面内容
            prompt.append("画面内容描述: ").append(cleanDesc).append(", ");
        }

        //风格化增强
        if (scriptGenre != null) {
            String genre = scriptGenre.replace("本", "").trim(); // 移除"本"字

            // 加入标题作为主题意象
            if (scriptName != null && !scriptName.isEmpty()) {
                prompt.append("主题意象: ").append(scriptName).append(", ");
            }

            prompt.append("风格流派: ");
            switch (genre) {
                case "推理":
                case "悬疑":
                    prompt.append("悬疑, 犯罪现场, 迷雾, 黑色电影风格, 阴郁氛围, 寻找线索, (Mystery, Suspense), ");
                    break;
                case "恐怖":
                case "惊悚":
                    prompt.append("中式恐怖, 阴森, 压抑, 黑暗, 惊悚氛围, 血迹, (Horror, Thriller), ");
                    break;
                case "古风":
                case "古装":
                case "情感":
                    prompt.append("中国风, 唯美, 汉服, 古建筑, 写意, 水墨感, 氛围感, (Traditional Chinese Art), ");
                    break;
                case "现代":
                case "都市":
                    prompt.append("现代都市, 霓虹灯, 现实主义, 电影质感, (Modern City), ");
                    break;
                case "科幻":
                case "机制":
                    prompt.append("赛博朋克, 未来科技, 机械感, 霓虹光效, (Cyberpunk, Sci-fi), ");
                    break;
                case "欢乐":
                    prompt.append("色彩鲜艳, 活泼, 二次元风格, 趣味插画, 高饱和度, ");
                    break;
                default:
                    prompt.append("极具张力, 故事感强, 电影级光影, ");
            }
        }

        //构图与光影
        prompt.append("画面中心聚焦, 留白设计(便于排版), 丁达尔效应, 电影打光, ");
        prompt.append("cinematic lighting, ray tracing, vivid colors");

        String finalPrompt = prompt.toString().trim();
        // 移除末尾逗号
        if (finalPrompt.endsWith(",")) {
            finalPrompt = finalPrompt.substring(0, finalPrompt.length() - 1);
        }

        log.info("【Kolors专用Prompt】: {}", finalPrompt);
        return finalPrompt;
    }



    /**
     * 构建图像生成请求体
     */
    private Map<String, Object> buildImageGenerationRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", imageGenerationModel);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1); // 生成一张图片
        requestBody.put("size", "1024x1024"); // 图片尺寸
        requestBody.put("response_format", "url"); // 返回URL格式

        // 优化负面提示词
        requestBody.put("negative_prompt",
                "丑陋, 变形, 模糊, 低质量, 畸形的手, 多余的手指, 缺手指, 画面脏, 水印, 文字, " +
                        "nsfw, low quality, worst quality, bad anatomy, bad hands, text, error, " +
                        "missing fingers, extra digit, fewer digits, cropped, normal quality, jpeg artifacts, " +
                        "signature, watermark, username, blurry");

        return requestBody;
    }


    /**
     * 从API响应中提取图片URL
     */
    @SuppressWarnings("unchecked")
    private String extractImageUrlFromResponse(Map<String, Object> response) {
        try {
            if (response != null && response.containsKey("data")) {
                Object dataObj = response.get("data");
                if (dataObj instanceof Iterable) {
                    Iterable<?> dataList = (Iterable<?>) dataObj;
                    for (Object item : dataList) {
                        if (item instanceof Map) {
                            Map<String, Object> imageData = (Map<String, Object>) item;
                            if (imageData.containsKey("url")) {
                                return (String) imageData.get("url");
                            }
                        }
                    }
                }
            }
            
            log.warn("无法从响应中提取图片URL，使用默认图片");
            return getDefaultCoverImageUrl();
            
        } catch (Exception e) {
            log.error("解析图片URL失败: {}", e.getMessage(), e);
            return getDefaultCoverImageUrl();
        }
    }

    /**
     * 获取默认封面图片URL
     */
    private String getDefaultCoverImageUrl() {
        // 可以配置多个默认图片，随机返回
        String[] defaultImages = {
            "https://picsum.photos/1024/1024?random=1",
            "https://picsum.photos/1024/1024?random=2",
            "https://picsum.photos/1024/1024?random=3"
        };
        
        int randomIndex = (int) (Math.random() * defaultImages.length);
        return defaultImages[randomIndex];
    }
}