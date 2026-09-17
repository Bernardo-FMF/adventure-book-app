package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.SectionType;
import jakarta.persistence.*;

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

    SectionEntity(BookEntity book, String sectionRef, SectionType type, String text) {
        this.book = book;
        this.sectionRef = sectionRef;
        this.type = type;
        this.text = text;
    }

    public OptionEntity addOption(String description, String gotoRef) {
        OptionEntity option = new OptionEntity(this, description, gotoRef, options.size());
        options.add(option);
        return option;
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
}
