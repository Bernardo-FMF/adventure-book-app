package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.SectionType;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "section", uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "section_ref"}))
public class SectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(name = "section_ref", nullable = false, length = 40)
    private String sectionRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 6)
    private SectionType type;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<OptionEntity> options = new ArrayList<>();

    protected SectionEntity() {
    }

    public SectionEntity(String sectionRef, SectionType type, String text) {
        this.sectionRef = sectionRef;
        this.type = type;
        this.text = text;
    }

    // The position is the order the option was added in, which corresponds to how many options have been inserted.
    public void addOption(OptionEntity option) {
        option.assignTo(this, options.size());
        options.add(option);
    }

    void assignTo(BookEntity book) {
        this.book = book;
    }

    public Long getId() {
        return id;
    }

    public BookEntity getBook() {
        return book;
    }

    public String getSectionRef() {
        return sectionRef;
    }

    public SectionType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public List<OptionEntity> getOptions() {
        return options;
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
        return getId() != null && Objects.equals(getId(), ((SectionEntity) o).getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SectionEntity{id=" + id + ", sectionRef='" + sectionRef + "', type=" + type + "}";
    }
}
