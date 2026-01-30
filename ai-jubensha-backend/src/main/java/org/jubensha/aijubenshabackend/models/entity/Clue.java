package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.jubensha.aijubenshabackend.models.enums.ClueType;
import org.jubensha.aijubenshabackend.models.enums.ClueVisibility;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clues")
public class Clue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "script_id", nullable = false)
    private Script script;
    
    private String name;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ClueType type;
    
    @Enumerated(EnumType.STRING)
    private ClueVisibility visibility;
    
    private String scene;

    @Min(1)
    @Max(100)
    // 面向DM用于控场
    private Integer importance;
    
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}