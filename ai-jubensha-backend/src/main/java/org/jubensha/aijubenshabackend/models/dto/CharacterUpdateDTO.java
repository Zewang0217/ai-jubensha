package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 角色更新数据传输对象
 */
@Data
public class CharacterUpdateDTO {

    @Size(max = 100, message = "角色名称长度不能超过100个字符")
    private String name;

    @Size(max = 2000, message = "角色描述长度不能超过2000个字符")
    private String description;

    @Size(max = 5000, message = "背景故事长度不能超过5000个字符")
    private String backgroundStory;

    @Size(max = 2000, message = "秘密信息长度不能超过2000个字符")
    private String secret;

    @URL(message = "头像URL格式不正确")
    private String avatarUrl;
}