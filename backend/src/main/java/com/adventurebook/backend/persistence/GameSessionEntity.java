package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.GameStatus;
import com.adventurebook.backend.persistence.types.SectionType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "game_session")
public class GameSessionEntity {
    private static final int STARTING_HEALTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private SectionEntity section;

    @Column(name = "health", nullable = false)
    private int health;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private GameStatus status;

    protected GameSessionEntity() {
    }

    public GameSessionEntity(BookEntity book, SectionEntity begin) {
        this.book = book;
        this.section = begin;
        this.health = STARTING_HEALTH;
        this.status = GameStatus.IN_PROGRESS;
    }

    public void moveTo(SectionEntity next) {
        this.section = next;
        if (next.getType() == SectionType.END) {
            this.status = GameStatus.FINISHED;
        }
    }

    public void loseHealth(int amount) {
        this.health = Math.max(0, this.health - amount);
        if (this.health == 0) {
            this.status = GameStatus.DEAD;
        }
    }

    public void gainHealth(int amount) {
        this.health = Math.min(STARTING_HEALTH, this.health + amount);
    }

    public UUID getId() {
        return id;
    }

    public BookEntity getBook() {
        return book;
    }

    public SectionEntity getSection() {
        return section;
    }

    public int getHealth() {
        return health;
    }

    public GameStatus getStatus() {
        return status;
    }

    public boolean isOver() {
        return status != GameStatus.IN_PROGRESS;
    }
}
