package org.jubensha.aijubenshabackend.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 玩家实体类
 */
@Data
@Entity
@Table(name = "players")
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;
    
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;
    
    @Column(name = "password", nullable = false, length = 100)
    private String password;
    
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    @Column(name = "role", nullable = false, length = 20)
    private String role; // 角色：user, admin
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // 状态：active, inactive, banned
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 关联关系
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GamePlayer> gamePlayers;
}
