package org.jubensha.aijubenshabackend.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;
import java.time.LocalDateTime;

/**
 * Player 相关的 DTO 容器
 */
public class PlayerDTO {
    //PlayerCreateRequest.java - 注册用
    //其他字段都是默认的,如果用户要自定义则在更新界面修改
    @Data
    public static class PlayerCreateRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在 6 到 20 个字符之间")
        private String password;

        @NotBlank(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "昵称不能为空")
        private String nickname;

    }

    //PlayerUpdateRequest.java - 更新用
    @Data
    public static class PlayerUpdateRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "昵称不能为空")
        private String nickname;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在 6 到 20 个字符之间")
        private String password;

        @NotBlank(message = "邮箱格式不正确")
        private String email;

        private String avatar;

        private PlayerStatus status;

    }

    // PlayerResponse.java - 基础响应
    @Data
    public static class PlayerResponse {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
        private PlayerRole role;
        private PlayerStatus status;
        private LocalDateTime createTime;
    }

    // PlayerDetailResponse.java - 详细信息响应（用于查询详情）
    @Data
    public static class PlayerDetailResponse {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
        private PlayerRole role;
        private PlayerStatus status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime lastLoginAt;
    }

}