package org.jubensha.aijubenshabackend.ai.service;

import org.jubensha.aijubenshabackend.ai.service.impl.ImageGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * 图像生成服务测试类
 */
class ImageGenerationServiceImplTest {

    @Mock
    private org.jubensha.aijubenshabackend.ai.client.SiliconFlowClient siliconFlowClient;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private ImageGenerationServiceImpl imageGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 手动设置测试所需的配置值
        ReflectionTestUtils.setField(imageGenerationService, "imageGenerationModel", "Kwai-Kolors/Kolors");
        ReflectionTestUtils.setField(imageGenerationService, "imageGenerationBaseUrl", "https://api.siliconflow.cn/v1");
        ReflectionTestUtils.setField(imageGenerationService, "imageGenerationApiKey", "test-api-key");
    }

    @Test
    void testGenerateScriptCoverImage_Success() {
        // 准备测试数据
        String scriptName = "神秘庄园杀人事件";
        String scriptDescription = "在一个古老的庄园中发生了一起离奇的谋杀案...";
        String scriptGenre = "悬疑";

        // 模拟API响应
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", "https://generated-image.example.com/image123.jpg");
        mockResponse.put("data", Arrays.asList(imageData));

        // 使用更宽松的mock验证，允许null参数
        when(siliconFlowClient.callImageGenerationApi(any(), any(), anyMap()))
                .thenReturn(mockResponse);

        // 执行测试
        String result = imageGenerationService.generateScriptCoverImage(scriptName, scriptDescription, scriptGenre);

        // 验证结果
        assertNotNull(result);
        assertEquals("https://generated-image.example.com/image123.jpg", result);
        verify(siliconFlowClient).callImageGenerationApi(any(), any(), anyMap());
    }

    @Test
    void testGenerateScriptCoverImage_ApiFailure_Fallback() {
        // 准备测试数据
        String scriptName = "神秘庄园杀人事件";
        String scriptDescription = "在一个古老的庄园中发生了一起离奇的谋杀案...";
        String scriptGenre = "悬疑";

        // 模拟API调用失败，使用更宽松的参数匹配
        when(siliconFlowClient.callImageGenerationApi(any(), any(), anyMap()))
                .thenThrow(new RuntimeException("API调用失败"));

        // 执行测试
        String result = imageGenerationService.generateScriptCoverImage(scriptName, scriptDescription, scriptGenre);

        // 验证结果 - 应该返回默认图片URL
        assertNotNull(result);
        assertTrue(result.startsWith("https://picsum.photos/"));
        verify(siliconFlowClient).callImageGenerationApi(any(), any(), anyMap());
    }

    @Test
    void testGenerateScriptCoverImageStream_Success() {
        // 准备测试数据
        String scriptName = "科幻探险";
        String scriptDescription = "一群宇航员在遥远星球上的冒险故事...";
        String scriptGenre = "科幻";

        // 模拟API响应
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", "https://generated-image.example.com/scifi123.jpg");
        mockResponse.put("data", java.util.Arrays.asList(imageData));

        when(siliconFlowClient.callImageGenerationApi(any(), any(), anyMap()))
                .thenReturn(mockResponse);

        // 执行测试
        Flux<String> resultFlux = imageGenerationService.generateScriptCoverImageStream(
                scriptName, scriptDescription, scriptGenre);

        // 验证结果
        StepVerifier.create(resultFlux)
                .expectNext("https://generated-image.example.com/scifi123.jpg")
                .verifyComplete();

        verify(siliconFlowClient).callImageGenerationApi(any(), any(), anyMap());
    }

    @Test
    void testBuildImagePrompt_DifferentGenres() throws Exception {
        // 测试不同题材的提示词构建
        
        // 使用反射获取私有方法并设置可访问
        java.lang.reflect.Method buildImagePromptMethod = imageGenerationService.getClass()
                .getDeclaredMethod("buildImagePrompt", String.class, String.class, String.class);
        buildImagePromptMethod.setAccessible(true);
        
        // 悬疑题材
        String mysteryPrompt = buildImagePromptMethod
                .invoke(imageGenerationService, "案件调查", "侦探破案", "悬疑")
                .toString();
        assertTrue(mysteryPrompt.contains("神秘氛围"));
        assertTrue(mysteryPrompt.contains("悬念元素"));

        // 恐怖题材
        String horrorPrompt = buildImagePromptMethod
                .invoke(imageGenerationService, "鬼屋传说", "灵异事件", "恐怖")
                .toString();
        assertTrue(horrorPrompt.contains("阴森恐怖"));
        assertTrue(horrorPrompt.contains("惊悚元素"));

        // 古风题材
        String ancientPrompt = buildImagePromptMethod
                .invoke(imageGenerationService, "宫廷秘史", "古代传奇", "古风")
                .toString();
        assertTrue(ancientPrompt.contains("古典建筑"));
        assertTrue(ancientPrompt.contains("古代服饰"));
    }

    @Test
    void testExtractKeyElements_FromDescription() throws Exception {
        // 测试关键字提取
        
        // 使用反射获取私有方法并设置可访问
        java.lang.reflect.Method extractKeyElementsMethod = imageGenerationService.getClass()
                .getDeclaredMethod("extractKeyElements", String.class);
        extractKeyElementsMethod.setAccessible(true);
        
        String description1 = "在一座古老的城堡中，发生了一起谋杀案，现场发现了血迹和一把带血的刀";
        String elements1 = extractKeyElementsMethod
                .invoke(imageGenerationService, description1)
                .toString();
        assertTrue(elements1.contains("凶案现场"));
        assertTrue(elements1.contains("血迹"));

        String description2 = "一位著名侦探受邀调查这起案件，他带着放大镜仔细检查每一个角落";
        String elements2 = extractKeyElementsMethod
                .invoke(imageGenerationService, description2)
                .toString();
        assertTrue(elements2.contains("侦探形象"));
        assertTrue(elements2.contains("放大镜"));
    }
}