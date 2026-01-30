package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scenes")
public class Scene {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;
    
    private String name;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    private String image;
    
    @Column(columnDefinition = "TEXT")
    private String availableActions;
    
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}