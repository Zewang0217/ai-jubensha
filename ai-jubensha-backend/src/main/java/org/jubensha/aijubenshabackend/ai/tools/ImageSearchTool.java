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
import org.jubensha.aijubenshabackend.ai.tools.permission.AgentType;
import org.jubensha.aijubenshabackend.ai.tools.permission.ToolPermissionLevel;
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
    private static final String BAIDU_API_URL = "https://zj.v.api.aa1.cn/api/so-baidu-img/";
    private static final String IMAGE_SRC_DEFAULT = "https://images.pexels.com/photos/35637981/pexels-photo-35637981.jpeg";

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

    @Override
    public ToolPermissionLevel getRequiredPermissionLevel(AgentType agentType) {
        return ToolPermissionLevel.NONE;
    }

    @Tool("根据关键词搜索图片")
    public ImageResource searchImage(@P("搜索关键词") String keyword) {
        try (HttpResponse response = HttpRequest.get(BAIDU_API_URL)
                .form("msg", keyword)
                .form("page", 1)
                .timeout(10000)
                .execute()) {

            log.debug("请求地址: https://zj.v.api.aa1.cn/api/so-baidu-img/?msg={}&page=1", keyword);

            if (!response.isOk()) {
                log.warn("Image API 请求失败，状态码: {}", response.getStatus());
                // 返回默认图片
                return ImageResource.builder()
                        .description("默认图片")
                        .imageUrl(IMAGE_SRC_DEFAULT)
                        .build();
            }

            JSONObject body = JSONUtil.parseObj(response.body());

            // 检查 API 返回的错误信息
            if (body.containsKey("error")) {
                log.warn("Image API 错误: {}", body.getStr("error"));
                // 返回默认图片
                return ImageResource.builder()
                        .description("默认图片")
                        .imageUrl(IMAGE_SRC_DEFAULT)
                        .build();
            }

            // Baidu API
            JSONArray photos = body.getJSONArray("data");

            if (photos == null || photos.isEmpty()) {
                log.warn("未找到关键词 '{}' 相关的图片，返回默认图片", keyword);
                // 返回默认图片
                return ImageResource.builder()
                        .description("默认图片")
                        .imageUrl(IMAGE_SRC_DEFAULT)
                        .build();
            }

            try {
                JSONObject photo = photos.getJSONObject(2);
                String src = photo.getStr("hoverUrl", IMAGE_SRC_DEFAULT);

                String description = photo.getStr("oriTitle", keyword);

                // 构建结果
                return ImageResource.builder()
                        .description(description)
                        .imageUrl(src)
                        .build();
            } catch (Exception e) {
                log.warn("处理图片数据失败: {}, 返回默认图片", e.getMessage());
                // 返回默认图片
                return ImageResource.builder()
                        .description("默认图片")
                        .imageUrl(IMAGE_SRC_DEFAULT)
                        .build();
            }
        } catch (Exception e) {
            log.error("图片搜索失败: {}", e.getMessage(), e);
            // 返回默认图片
            return ImageResource.builder()
                    .description("默认图片")
                    .imageUrl(IMAGE_SRC_DEFAULT)
                    .build();
        }
    }
}
