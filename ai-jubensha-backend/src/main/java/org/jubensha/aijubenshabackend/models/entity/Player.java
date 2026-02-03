package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.PlayerRole;
import org.jubensha.aijubenshabackend.models.enums.PlayerStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "players")
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String password;
    
    private String email;
    
    private String avatar;
    
    @Enumerated(EnumType.STRING)
    private PlayerRole role;
    
    @Enumerated(EnumType.STRING)
    private PlayerStatus status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}