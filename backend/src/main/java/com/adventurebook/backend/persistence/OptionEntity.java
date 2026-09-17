package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.ConsequenceType;
import jakarta.persistence.*;

@Entity
@Table(name = "section_option")
public class OptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @Column(name = "pos", nullable = false)
    private int position;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "goto", nullable = false, length = 40)
    private String gotoRef;

    @OneToOne(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConsequenceEntity consequence;

    protected OptionEntity() {
    }

    OptionEntity(SectionEntity section, String description, String gotoRef, int position) {
        this.section = section;
        this.description = description;
        this.gotoRef = gotoRef;
        this.position = position;
    }

    public ConsequenceEntity setConsequence(ConsequenceType type, int amount, String text) {
        this.consequence = new ConsequenceEntity(this, type, amount, text);
        return consequence;
    }

    public Long getId() {
        return id;
    }

    public SectionEntity getSection() {
        return section;
    }

    public int getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    public String getGotoRef() {
        return gotoRef;
    }

    public ConsequenceEntity getConsequence() {
        return consequence;
    }
}
