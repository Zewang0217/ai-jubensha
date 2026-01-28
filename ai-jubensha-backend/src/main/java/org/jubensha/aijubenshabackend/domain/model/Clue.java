package org.jubensha.aijubenshabackend.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 线索实体类
 */
@Data
@Entity
@Table(name = "clues")
public class Clue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    
    @Column(name = "importance", nullable = false)
    private Integer importance; // 重要性：1-5
    
    @Column(name = "type", nullable = false, length = 20)
    private String type; // 类型：physical, witness, document
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // 状态：hidden, found, used
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    private Scene scene;
}
