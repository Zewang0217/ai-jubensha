package org.jubensha.aijubenshabackend.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.service.ImageGenerationService;
import org.jubensha.aijubenshabackend.models.dto.ImageGenerateRequestDTO;
import org.jubensha.aijubenshabackend.models.dto.ImageGenerateResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 图片生成控制器
 * 提供独立的图片生成API端点
 */
@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImageGenerationService imageGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<ImageGenerateResponseDTO> generateCoverImage(
            @Valid @RequestBody ImageGenerateRequestDTO request) {
        try {
            log.info("开始生成剧本封面图片: {}, 类型: {}", request.getScriptName(), request.getScriptGenre());

            // 调用图片生成服务
            String imageUrl = imageGenerationService.generateScriptCoverImage(
                    request.getScriptName(),
                    request.getScriptDescription(),
                    request.getScriptGenre());

            // 构建响应
            ImageGenerateResponseDTO response = ImageGenerateResponseDTO.success(
                    imageUrl, request.getScriptName(), request.getScriptGenre());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("生成剧本封面图片失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/generate/async")
    public ResponseEntity<ImageGenerateResponseDTO> generateCoverImageAsync(
            @Valid @RequestBody ImageGenerateRequestDTO request) {
        try {
            log.info("开始异步生成剧本封面图片: {}, 类型: {}", request.getScriptName(), request.getScriptGenre());

            // 使用异步回调方式
            imageGenerationService.generateScriptCoverImageAsync(
                    request.getScriptName(),
                    request.getScriptDescription(),
                    request.getScriptGenre(),
                    new ImageGenerationService.ImageGenerationCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            log.info("异步图片生成成功: {}", imageUrl);
                        }

                        @Override
                        public void onError(Exception error) {
                            log.error("异步图片生成失败: {}", error.getMessage(), error);
                        }
                    });

            // 立即返回响应
            ImageGenerateResponseDTO response = ImageGenerateResponseDTO.asyncSubmitted(
                    request.getScriptName(), "图片生成任务已提交");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("提交异步图片生成任务失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    /**
     * 流式生成剧本封面图片
     */
    @GetMapping("/scripts/{scriptId}/generate-cover")
    public Flux<String> generateCoverImageStream(
            @RequestParam String scriptName,
            @RequestParam(required = false) String scriptDescription,
            @RequestParam(required = false) String scriptGenre) {

        // 参数验证
        if (scriptName == null || scriptName.trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("剧本名称不能为空"));
        }

        log.info("开始流式生成剧本封面图片: {}, 类型: {}", scriptName, scriptGenre);

        // 调用流式图片生成服务
        return imageGenerationService.generateScriptCoverImageStream(
                scriptName, scriptDescription, scriptGenre);
    }

}
