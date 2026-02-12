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
        ImageResource imageResource;
        try (HttpResponse response = HttpRequest.get(BAIDU_API_URL)
//                .header("Authorization", pexelsApiKey)
//                .form("query", keyword)
                .form("msg", keyword)
//                .form("per_page", 1)
                .form("page", 1)
                .timeout(10000)
                .execute()) {

//            log.debug("请求地址: https://api.pexels.com/v1/search?query={}&per_page=1&page=1", keyword);
            log.debug("请求地址: https://zj.v.api.aa1.cn/api/so-baidu-img/?msg={}&page=1", keyword);

            if (!response.isOk()) {
                throw new BusinessException("Image API 请求失败，状态码: " + response.getStatus());
            }

            JSONObject body = JSONUtil.parseObj(response.body());

            // 检查 API 返回的错误信息
            if (body.containsKey("error")) {
                throw new BusinessException("Image API 错误: " + body.getStr("error"));
            }

            /* Pexels API 返回的 JSON 结构
            JSONArray photos = body.getJSONArray("photos");

            if (photos == null || photos.isEmpty()) {
                throw new BusinessException("未找到关键词 '" + keyword + "' 相关的图片");
            }

            JSONObject photo = photos.getJSONObject(0);
            JSONObject src = photo.getJSONObject("src");

            if (src == null) {
                throw new BusinessException("图片源信息缺失");
            }
             */

            // Baidu API
            JSONArray photos = body.getJSONArray("data");

            if (photos == null || photos.isEmpty()) {
                throw new BusinessException("未找到关键词 '" + keyword + "' 相关的图片");
            }

            JSONObject photo = photos.getJSONObject(2);
            String src = photo.getStr("hoverUrl", IMAGE_SRC_DEFAULT);

            if (src == null) {
                throw new BusinessException("图片源信息缺失");
            }

            Integer width = photo.getInt("width");
            Integer height = photo.getInt("height");

            String description = photo.getStr("oriTitle", keyword);

            // 构建结果
            return ImageResource.builder()
                    .description(description)
                    .imageUrl(src)
                    .build();
        } catch (Exception e) {
            log.error("Pexels API 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(THIRD_PARTY_SERVICE_ERROR);
        }
    }
}
