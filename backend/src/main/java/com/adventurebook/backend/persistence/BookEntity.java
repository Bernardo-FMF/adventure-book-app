package com.adventurebook.backend.persistence;

import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.hibernate.proxy.HibernateProxy;

import java.util.*;

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
    // The query is inlined into the query that fetches the book, so this field is not lazy.
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

    /**
     * Entity equality is based on the database row, and nothing else.
     * Comparing fields would make two unsaved books describing the same story equal, which they are not: they would become two rows.
     * So a book with no id yet is equal only to itself.
     * In short, compare ids only to avoid loading more entities.
     * <p>
     * We use the lazy initializer to obtain the class for 2 reasons:
     * 1. The proxy will be a subclass generated at runtime, so comparing the book proxy to the book class would fail.
     * 2. Using Hibernate.getClass() would give the correct class, but it would load the row, so there would be a query
     * inside the equals.
     * <p>
     * Source: https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/
     */
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
        return getId() != null && Objects.equals(getId(), ((BookEntity) o).getId());
    }

    /**
     * Constant per entity class. The id cannot be used: it is null before the book is saved and assigned afterward,
     * so a book added to a Set while new would move to a different bucket once persisted and be lost from the set it is already in.
     * The cost is that every book collides, turning a set lookup into a list iteration.
     */
    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    /**
     * The entity toString should only list attributed that don't fire a query, otherwise we could encounter a situation
     * where this would be called outside a session.
     */
    @Override
    public String toString() {
        return "BookEntity{id=" + id + ", slug='" + slug + "', title='" + title + "'}";
    }
}
