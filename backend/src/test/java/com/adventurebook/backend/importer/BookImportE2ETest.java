package com.adventurebook.backend.importer;

import com.adventurebook.backend.persistence.types.Genre;
import com.adventurebook.backend.response.BookDto;
import com.adventurebook.backend.response.BookListDto;
import com.adventurebook.backend.utils.PostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookImportE2ETest extends PostgresIntegrationTest {
    @LocalServerPort
    private int port;

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
        // The originals are left broken on purpose, so only their repaired counterparts arrive.
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
}
