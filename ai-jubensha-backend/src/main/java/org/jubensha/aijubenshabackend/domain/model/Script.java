package org.jubensha.aijubenshabackend.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 剧本实体类
 */
@Data
@Entity
@Table(name = "scripts")
public class Script {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    
    @Column(name = "author", nullable = false, length = 50)
    private String author;
    
    @Column(name = "genre", nullable = false, length = 50)
    private String genre;
    
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;
    
    @Column(name = "player_count", nullable = false)
    private Integer playerCount;
    
    @Column(name = "duration", nullable = false)
    private Integer duration; // 游戏时长（分钟）
    
    @Column(name = "cover_url", length = 255)
    private String coverUrl;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // 状态：draft, published, archived
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 关联关系
    @OneToMany(mappedBy = "script", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Character> characters;
    
    @OneToMany(mappedBy = "script", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Scene> scenes;
    
    @OneToMany(mappedBy = "script", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Game> games;
}
