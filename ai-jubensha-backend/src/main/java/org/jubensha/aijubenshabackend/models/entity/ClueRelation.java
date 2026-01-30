package org.jubensha.aijubenshabackend.models.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clue_relations")
public class ClueRelation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "clue_id1", nullable = false)
    private Clue clue1;
    
    @ManyToOne
    @JoinColumn(name = "clue_id2", nullable = false)
    private Clue clue2;
    
    private Integer strength;
    
    @Column(columnDefinition = "TEXT")
    private String description;
}