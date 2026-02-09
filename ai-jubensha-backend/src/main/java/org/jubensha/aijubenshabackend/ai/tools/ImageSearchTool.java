package org.jubensha.aijubenshabackend.ai.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jubensha.aijubenshabackend.ai.models.ImageResource;
import org.jubensha.aijubenshabackend.core.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        try {
            JSONObject body = JSONUtil.parseObj(
                    HttpUtil.get(
                            PEXELS_API_URL, Map.of(
                                    "query", keyword,
                                    "per_page", 1,
                                    "page", 1
                            )
                    ));
            JSONArray photos = body.getJSONArray("photos");
            JSONObject photo = photos.getJSONObject(0);
            JSONObject src = photo.getJSONObject("src");
            imageResource = ImageResource.builder()
                    .description(photo.getStr("alt", keyword))
                    .imageUrl(src.getStr("medium"))
                    .build();
        } catch (Exception e) {
            log.error("Pexels API 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(THIRD_PARTY_SERVICE_ERROR);
        }
        log.debug("图片获取成功: {}", imageResource);
        return imageResource;
    }
}
