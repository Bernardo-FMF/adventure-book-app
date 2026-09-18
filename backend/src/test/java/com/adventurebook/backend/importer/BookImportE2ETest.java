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

    @Test
    @DisplayName("only the valid seed book is imported and served")
    void servesTheImportedSeedBook() {
        BookListDto body = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build()
                .get().uri("/api/books").exchange()
                .expectStatus().isOk()
                .expectBody(BookListDto.class)
                .returnResult().getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.books().size()).isEqualTo(1);

        assertThat(body.books()).extracting(BookDto::title)
                .containsExactly("(Fixed) The Prisoner: Escape");


        BookDto book = body.books().getFirst();
        assertThat(book.slug()).isEqualTo("fixed-the-prisoner-escape");
        assertThat(book.author()).isEqualTo("Daniel El Fuego");
        assertThat(book.tags()).containsExactly("Escape", "Prison");
        assertThat(book.sectionsCount()).isEqualTo(5);
        assertThat(book.description()).startsWith("Locked in a windowless cell");
        assertThat(book.genre()).isEqualTo(Genre.MYSTERY);
    }
}
