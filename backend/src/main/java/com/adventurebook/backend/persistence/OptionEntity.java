package com.adventurebook.backend.persistence;

import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

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

    public OptionEntity(String description, String gotoRef) {
        this.description = description;
        this.gotoRef = gotoRef;
    }

    public void setConsequence(ConsequenceEntity consequence) {
        consequence.assignTo(this);
        this.consequence = consequence;
    }

    void assignTo(SectionEntity section, int position) {
        this.section = section;
        this.position = position;
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
        return getId() != null && Objects.equals(getId(), ((OptionEntity) o).getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OptionEntity{id=" + id + ", position=" + position + ", gotoRef='" + gotoRef + "'}";
    }
}
