package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.ConsequenceType;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "consequence")
public class ConsequenceEntity {
    // The id is mapped from the option (hence the @MapsId on the option property), so the PK and FK are the same column.
    // This essentially translates to: at most one consequence per option.
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
        return getId() != null && Objects.equals(getId(), ((ConsequenceEntity) o).getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ConsequenceEntity{id=" + id + ", type=" + type + ", amount=" + amount + "}";
    }
}
