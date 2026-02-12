package org.jubensha.aijubenshabackend.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * SiliconFlow API客户端
 * 用于调用SiliconFlow的rerank API和图像生成API
 */
@Slf4j
@Component
public class SiliconFlowClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public SiliconFlowClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用图像生成API
     *
     * @param baseUrl     API基础URL
     * @param apiKey      API密钥
     * @param requestBody 请求体
     * @return 图像生成响应
     */
    public Map<String, Object> callImageGenerationApi(String baseUrl, String apiKey, Map<String, Object> requestBody) {
        try {
            log.info("调用SiliconFlow图像生成API, baseUrl: {}", baseUrl);

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 发送POST请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/images/generations",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            log.info("图像生成API调用成功, 状态码: {}", response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("调用SiliconFlow图像生成API失败: {}", e.getMessage(), e);
            throw new RuntimeException("图像生成API调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用rerank API
     *
     * @param baseUrl     API基础URL
     * @param apiKey      API密钥
     * @param requestBody 请求体
     * @return API响应
     */
    public Map<String, Object> callRerankApi(String baseUrl, String apiKey, Map<String, Object> requestBody) {
        try {
            log.info("调用SiliconFlow rerank API, baseUrl: {}", baseUrl);

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            String url = baseUrl + "/rerank";
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 处理响应
            Map<String, Object> response = responseEntity.getBody();
            log.info("SiliconFlow rerank API调用成功, 响应: {}", response);

            return response;

        } catch (Exception e) {
            log.error("调用SiliconFlow rerank API失败: {}", e.getMessage(), e);
            throw new RuntimeException("调用SiliconFlow rerank API失败", e);
        }
    }
}
