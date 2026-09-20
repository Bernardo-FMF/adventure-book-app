package com.adventurebook.backend.importer;

import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import com.adventurebook.backend.response.BookDto;
import com.adventurebook.backend.response.BookListDto;
import com.adventurebook.backend.response.MetadataDto;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.utils.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookImportE2ETest extends PostgresIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private BookImportService importService;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void reimportSeedBooks() throws Exception {
        bookRepository.deleteAll();
        importService.run(new DefaultApplicationArguments());
    }

    private BookListDto books() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build()
                .get().uri("/api/books").exchange()
                .expectStatus().isOk()
                .expectBody(BookListDto.class)
                .returnResult().getResponseBody();
    }

    @Test
    @DisplayName("only the books that pass validation are imported and served")
    void servesOnlyTheValidSeedBooks() {
        BookListDto body = books();

        assertThat(body).isNotNull();
        assertThat(body.books()).extracting(BookDto::title).containsExactlyInAnyOrder(
                "(Fixed) The Prisoner: Escape",
                "(Fixed) The Crystal Caverns",
                "(Fixed) Pirates of the Jade Sea",
                "(Fixed) Dragon Quest");
    }

    @Test
    @DisplayName("a book keeps every field the file gave it")
    void servesTheImportedSeedBook() {
        BookDto book = books().books().stream()
                .filter(b -> b.slug().equals("fixed-the-prisoner-escape"))
                .findFirst()
                .orElseThrow();
        assertThat(book.title()).isEqualTo("(Fixed) The Prisoner: Escape");
        assertThat(book.author()).isEqualTo("Daniel El Fuego");
        assertThat(book.tags()).containsExactly("Escape", "Prison");
        assertThat(book.sectionsCount()).isEqualTo(5);
        assertThat(book.description()).startsWith("Locked in a windowless cell");
        assertThat(book.genre()).isEqualTo(Genre.MYSTERY);
    }

    @Test
    @DisplayName("metadata describes the imported books")
    void servesMetadataForTheImportedBooks() {
        MetadataDto metadata = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build()
                .get().uri("/api/books/metadata").exchange()
                .expectStatus().isOk()
                .expectBody(MetadataDto.class)
                .returnResult().getResponseBody();

        assertThat(metadata).isNotNull();
        assertThat(metadata.bookCount()).isEqualTo(4);
        assertThat(metadata.genres())
                .containsExactly(Genre.FANTASY, Genre.HIGH_FANTASY, Genre.ADVENTURE, Genre.MYSTERY);
        assertThat(metadata.difficulties())
                .containsExactly(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
    }
}

