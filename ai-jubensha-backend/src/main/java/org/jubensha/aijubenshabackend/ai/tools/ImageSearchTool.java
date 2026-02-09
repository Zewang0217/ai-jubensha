package org.jubensha.aijubenshabackend.ai.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.models.ImageResource;
import org.jubensha.aijubenshabackend.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.jubensha.aijubenshabackend.core.exception.enums.ErrorCodeEnum.THIRD_PARTY_SERVICE_ERROR;

/**
 * 用于获取图片素材
 *
 * @author luobo
 * @date 2026/2/9
 */
@Slf4j
@Component
public class ImageSearchTool extends BaseTool {
    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    @Value("${pexels.api-key}")
    private String pexelsApiKey;

    @Override
    public String getToolName() {
        return "image-search";
    }

    @Override
    public String getDisplayName() {
        return "图片获取调用";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "\n\n[图片获取成功]\n\n";
    }

    @Tool("根据关键词搜索图片")
    public ImageResource searchImage(@P("搜索关键词") String keyword) {
        ImageResource imageResource;
        try (HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                .header("Authorization", pexelsApiKey)
                .form("query", keyword)
                .form("per_page", 1)
                .form("page", 1)
                .timeout(10000)
                .execute()) {

            if (!response.isOk()) {
                throw new BusinessException("Pexels API 请求失败，状态码: " + response.getStatus());
            }

            JSONObject body = JSONUtil.parseObj(response.body());

            // 检查 API 返回的错误信息
            if (body.containsKey("error")) {
                throw new BusinessException("Pexels API 错误: " + body.getStr("error"));
            }

            JSONArray photos = body.getJSONArray("photos");

            // 防御性编程：检查空结果
            if (photos == null || photos.isEmpty()) {
                throw new BusinessException("未找到关键词 '" + keyword + "' 相关的图片");
            }

            JSONObject photo = photos.getJSONObject(0);
            JSONObject src = photo.getJSONObject("src");

            if (src == null) {
                throw new BusinessException("图片源信息缺失");
            }

            // 构建结果
            return ImageResource.builder()
                    .description(photo.getStr("alt", keyword))
                    .imageUrl(src.getStr("medium", ""))
                    .build();
        } catch (Exception e) {
            log.error("Pexels API 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(THIRD_PARTY_SERVICE_ERROR);
        }
    }
}
