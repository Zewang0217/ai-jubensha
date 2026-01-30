package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "scene_clues")
public class SceneClue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;
    
    @ManyToOne
    @JoinColumn(name = "clue_id", nullable = false)
    private Clue clue;
    
    @Column(columnDefinition = "TEXT")
    private String discoveryCondition;
}