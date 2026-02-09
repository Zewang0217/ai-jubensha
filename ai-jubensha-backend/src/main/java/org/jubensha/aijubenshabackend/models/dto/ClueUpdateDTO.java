package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;

/**
 * 线索更新数据传输对象
 */
@Data
public class ClueUpdateDTO {

    @Size(max = 100, message = "线索名称长度不能超过100个字符")
    private String name;

    @Size(max = 5000, message = "线索描述长度不能超过5000个字符")
    private String description;

    private ClueType type;
    private ClueVisibility visibility;

    @Size(max = 100, message = "场景名称长度不能超过100个字符")
    private String scene;

    @URL(message = "图片URL格式错误")
    private String imageUrl;

    @Min(value = 1, message = "重要度不能小于1")
    @Max(value = 100, message = "重要度不能大于100")
    private Integer importance;
}