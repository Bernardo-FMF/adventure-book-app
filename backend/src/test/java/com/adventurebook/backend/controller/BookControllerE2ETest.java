package com.adventurebook.backend.controller;

import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.OptionEntity;
import com.adventurebook.backend.persistence.SectionEntity;
import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import com.adventurebook.backend.persistence.types.SectionType;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.response.BookDto;
import com.adventurebook.backend.response.BookListDto;
import com.adventurebook.backend.utils.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        // Disable the importer by pointing to an invalid path
        properties = "app.books.seed-location=classpath:no-seed-data/*.json"
)
class BookControllerE2ETest extends PostgresIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    private RestTestClient client;

    @BeforeEach
    void resetBooks() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        bookRepository.deleteAll();
        bookRepository.saveAll(List.of(
                book("crystal-caverns", "The Crystal Caverns", "Evelyn Stormrider", Difficulty.EASY, Genre.FANTASY, 3, "Caves"),
                book("pirates-jade-sea", "Pirates of the Jade Sea", "Marina Blackwood", Difficulty.MEDIUM, Genre.ADVENTURE, 2, "Sailing"),
                book("the-prisoner", "The Prisoner", "Daniel El Fuego", Difficulty.HARD, Genre.MYSTERY, 5, "Escape")
        ));
    }

    private static BookEntity book(String slug, String title, String author, Difficulty difficulty, Genre genre, int sections, String tag) {
        BookEntity entity = new BookEntity(slug, title);
        entity.setAuthor(author);
        entity.setDifficulty(difficulty);
        entity.setGenre(genre);
        entity.getTags().add(tag);

        for (int i = 1; i <= sections; i++) {
            SectionEntity section = new SectionEntity(String.valueOf(i), i == sections ? SectionType.END : SectionType.NODE, "text " + i);
            if (i < sections) {
                section.addOption(new OptionEntity("go on", String.valueOf(i + 1)));
            }
            entity.addSection(section);
        }
        return entity;
    }

    private BookListDto get(String uri) {
        return client.get().uri(uri).exchange()
                .expectStatus().isOk()
                .expectBody(BookListDto.class)
                .returnResult().getResponseBody();
    }

    @Test
    @DisplayName("returns every book when no filter is given")
    void returnsAllBooks() {
        BookListDto body = get("/api/books");

        assertThat(body.books()).extracting(BookDto::slug)
                .containsExactlyInAnyOrder("crystal-caverns", "pirates-jade-sea", "the-prisoner");
        assertThat(body.pagination().totalElements()).isEqualTo(3);
        assertThat(body.pagination().totalPages()).isEqualTo(1);
        assertThat(body.pagination().currentPage()).isZero();
    }

    @Test
    @DisplayName("a book carries its author, tags and section count")
    void returnsBookDetails() {
        BookDto book = get("/api/books?query=Crystal").books().getFirst();

        assertThat(book.title()).isEqualTo("The Crystal Caverns");
        assertThat(book.author()).isEqualTo("Evelyn Stormrider");
        assertThat(book.difficulty()).isEqualTo(Difficulty.EASY);
        assertThat(book.genre()).isEqualTo(Genre.FANTASY);
        assertThat(book.tags()).containsExactly("Caves");
        assertThat(book.sectionsCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("the query matches part of a title, ignoring case")
    void filtersByQuery() {
        assertThat(get("/api/books?query=jade").books()).extracting(BookDto::slug)
                .containsExactly("pirates-jade-sea");
    }

    @Test
    @DisplayName("several difficulties can be requested at once")
    void filtersByDifficulty() {
        assertThat(get("/api/books?difficulty=EASY&difficulty=HARD").books()).extracting(BookDto::slug)
                .containsExactlyInAnyOrder("crystal-caverns", "the-prisoner");
    }

    @Test
    @DisplayName("genre and difficulty filters can be combined")
    void combinesFilters() {
        assertThat(get("/api/books?genre=FANTASY&genre=MYSTERY&difficulty=HARD").books())
                .extracting(BookDto::slug).containsExactly("the-prisoner");
    }

    @Test
    @DisplayName("a page reports the total count")
    void paginates() {
        BookListDto firstPage = get("/api/books?page=0&size=2");

        assertThat(firstPage.books()).hasSize(2);
        assertThat(firstPage.pagination().totalElements()).isEqualTo(3);
        assertThat(firstPage.pagination().totalPages()).isEqualTo(2);

        BookListDto secondPage = get("/api/books?page=1&size=2");

        assertThat(secondPage.books()).hasSize(1);
        assertThat(secondPage.pagination().currentPage()).isEqualTo(1);
    }

    @Test
    @DisplayName("an unknown filter value is rejected as a bad request")
    void rejectsUnknownDifficulty() {
        client.get().uri("/api/books?difficulty=IMPOSSIBLE").exchange().expectStatus().isBadRequest();
    }
}
