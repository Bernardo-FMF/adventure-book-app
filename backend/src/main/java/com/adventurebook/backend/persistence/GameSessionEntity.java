package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.GameStatus;
import com.adventurebook.backend.persistence.types.SectionType;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_consequence_id")
    private ConsequenceEntity lastConsequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    protected GameSessionEntity() {
    }

    public GameSessionEntity(BookEntity book, SectionEntity begin, PlayerEntity player) {
        this.book = book;
        this.section = begin;
        this.player = player;
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

    public void recordConsequence(ConsequenceEntity consequence) {
        this.lastConsequence = consequence;
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

    public ConsequenceEntity getLastConsequence() {
        return lastConsequence;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public boolean isOver() {
        return status != GameStatus.IN_PROGRESS;
    }

    public boolean belongsTo(PlayerEntity other) {
        return player != null && other != null && Objects.equals(player.getId(), other.getId());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> otherClass = o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : getClass();
        if (thisClass != otherClass) {
            return false;
        }
        return getId() != null && Objects.equals(getId(), ((GameSessionEntity) o).getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "GameSessionEntity{id=" + id + ", health=" + health + ", status=" + status + "}";
    }
}
