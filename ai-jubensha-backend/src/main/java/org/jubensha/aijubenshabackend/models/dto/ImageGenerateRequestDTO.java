package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 图片生成请求数据传输对象
 */
@Data
public class ImageGenerateRequestDTO {

    @NotBlank(message = "剧本名称不能为空")
    @Size(max = 100, message = "剧本名称长度不能超过100个字符")
    private String scriptName;

    @Size(max = 2000, message = "剧本描述长度不能超过2000个字符")
    private String scriptDescription;

    @Size(max = 50, message = "剧本类型长度不能超过50个字符")
    private String scriptGenre;
}