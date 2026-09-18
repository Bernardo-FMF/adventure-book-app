package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "book")
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "author", length = 120)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 10)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", length = 40)
    private Genre genre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Tags are lazy loaded, so when we access tags for each individual book we'd be performing a query for each.
    // Making the tags lazy and with a batch size, when we first access the tags of a book, we'll perform a batch fetch
    // for the tags of N books (N = 10).
    @ElementCollection(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    @CollectionTable(name = "book_tag", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "tag", nullable = false, length = 40)
    private Set<String> tags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionEntity> sections = new ArrayList<>();

    // This is used to obtain a count of how many sections a book has without actually loading the collection,
    // and just performing a lighter query that only obtains the count.
    @Formula("(select count(*) from section s where s.book_id = id)")
    private int sectionsCount;

    protected BookEntity() {
    }

    public BookEntity(String slug, String title) {
        this.slug = slug;
        this.title = title;
    }

    public void addSection(SectionEntity section) {
        section.assignTo(this);
        sections.add(section);
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public List<SectionEntity> getSections() {
        return sections;
    }

    public int getSectionsCount() {
        return sectionsCount;
    }
}
