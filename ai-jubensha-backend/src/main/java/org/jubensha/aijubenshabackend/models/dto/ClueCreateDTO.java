package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;

/**
 * 线索创建数据传输对象
 */
@Data
public class ClueCreateDTO {

    @NotNull(message = "剧本ID不能为空")
    private Long scriptId;

    @NotBlank(message = "线索名称不能为空")
    @Size(max = 100, message = "线索名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "线索描述不能为空")
    @Size(max = 5000, message = "线索描述长度不能超过5000个字符")
    private String description;

    @NotNull(message = "线索类型不能为空")
    private ClueType type;

    @NotNull(message = "线索可见性不能为空")
    private ClueVisibility visibility;

    @Size(max = 100, message = "场景名称长度不能超过100个字符")
    private String scene;

    @Min(value = 1, message = "重要度不能小于1")
    @Max(value = 100, message = "重要度不能大于100")
    private Integer importance;
}