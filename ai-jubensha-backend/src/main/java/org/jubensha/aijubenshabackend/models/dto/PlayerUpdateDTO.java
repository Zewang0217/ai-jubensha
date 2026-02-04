package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;

/**
 * 玩家更新数据传输对象
 */
@Data
public class PlayerUpdateDTO {

    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    private String username;

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatarUrl;

    private PlayerStatus status;
}