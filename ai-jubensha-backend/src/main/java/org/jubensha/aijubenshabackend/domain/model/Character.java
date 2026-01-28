package org.jubensha.aijubenshabackend.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色实体类
 */
@Data
@Entity
@Table(name = "characters")
public class Character {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "gender", nullable = false, length = 10)
    private String gender;
    
    @Column(name = "age", nullable = false)
    private Integer age;
    
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    
    @Column(name = "background", nullable = false, length = 5000)
    private String background;
    
    @Column(name = "goal", nullable = false, length = 1000)
    private String goal;
    
    @Column(name = "secret", nullable = false, length = 2000)
    private String secret;
    
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // 状态：active, inactive
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;
    
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Clue> clues;
    
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GamePlayer> gamePlayers;
}
