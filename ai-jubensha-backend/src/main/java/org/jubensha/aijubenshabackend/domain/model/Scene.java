package org.jubensha.aijubenshabackend.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场景实体类
 */
@Data
@Entity
@Table(name = "scenes")
public class Scene {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", nullable = false, length = 2000)
    private String description;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex; // 场景顺序
    
    @Column(name = "duration", nullable = false)
    private Integer duration; // 场景时长（分钟）
    
    @Column(name = "bgm_url", length = 255)
    private String bgmUrl; // 背景音乐URL
    
    @Column(name = "image_url", length = 255)
    private String imageUrl; // 场景图片URL
    
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
    
    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Clue> clues;
}
