package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.ConsequenceType;
import jakarta.persistence.*;

@Entity
@Table(name = "consequence")
public class ConsequenceEntity {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option")
    private OptionEntity option;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 12)
    private ConsequenceType type;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    protected ConsequenceEntity() {
    }

    public ConsequenceEntity(ConsequenceType type, int amount, String text) {
        this.type = type;
        this.amount = amount;
        this.text = text;
    }

    void assignTo(OptionEntity option) {
        this.option = option;
    }

    public Long getId() {
        return id;
    }

    public OptionEntity getOption() {
        return option;
    }

    public ConsequenceType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public String getText() {
        return text;
    }
}
