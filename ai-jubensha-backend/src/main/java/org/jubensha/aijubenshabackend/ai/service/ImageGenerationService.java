package org.jubensha.aijubenshabackend.ai.service;

import reactor.core.publisher.Flux;

/**
 * 图像生成服务接口
 * 提供剧本封面图像的AI生成功能
 */
public interface ImageGenerationService {

    /**
     * 根据剧本描述生成封面图片
     *
     * @param scriptName        剧本名称
     * @param scriptDescription 剧本描述
     * @param scriptGenre       剧本类型/题材
     * @return 生成的图片URL
     */
    String generateScriptCoverImage(String scriptName, String scriptDescription, String scriptGenre);

    /**
     * 流式生成剧本封面图片
     *
     * @param scriptName        剧本名称
     * @param scriptDescription 剧本描述
     * @param scriptGenre       剧本类型/题材
     * @return 图片生成进度流
     */
    Flux<String> generateScriptCoverImageStream(String scriptName, String scriptDescription, String scriptGenre);

    /**
     * 异步生成剧本封面图片
     *
     * @param scriptName        剧本名称
     * @param scriptDescription 剧本描述
     * @param scriptGenre       剧本类型/题材
     * @param callback          生成完成回调
     */
    void generateScriptCoverImageAsync(String scriptName, String scriptDescription, String scriptGenre, 
                                     ImageGenerationCallback callback);

    /**
     * 图像生成回调接口
     */
    interface ImageGenerationCallback {
        void onSuccess(String imageUrl);
        void onError(Exception error);
    }
}