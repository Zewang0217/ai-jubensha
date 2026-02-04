package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 场景创建数据传输对象
 */
@Data
public class SceneCreateDTO {

    @NotNull(message = "剧本ID不能为空")
    private Long scriptId;

    @NotBlank(message = "场景名称不能为空")
    private String name;

    @NotBlank(message = "场景描述不能为空")
    private String description;

    @URL(message = "场景图片URL格式不正确")
    private String imageUrl;

    private String availableActions;
}