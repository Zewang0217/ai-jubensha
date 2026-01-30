package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.DifficultyLevel;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scripts")
public class Script {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String author;
    
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;
    
    private Integer duration;
    
    private Integer playerCount;
    
    private String coverImage;
    
//    @Column(columnDefinition = "LONGTEXT")
//    private String content;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
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