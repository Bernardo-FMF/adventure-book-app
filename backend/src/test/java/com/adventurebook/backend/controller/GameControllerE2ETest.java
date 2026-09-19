package com.adventurebook.backend.controller;

import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.OptionEntity;
import com.adventurebook.backend.persistence.SectionEntity;
import com.adventurebook.backend.persistence.types.GameStatus;
import com.adventurebook.backend.persistence.types.SectionType;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.response.GameStateDto;
import com.adventurebook.backend.response.OptionDto;
import com.adventurebook.backend.utils.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.books.seed-location=classpath*:no-seed-data/*.json"
)
class GameControllerE2ETest extends PostgresIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    private RestTestClient client;
    private long bookId;

    @BeforeEach
    void seedBook() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        bookRepository.deleteAll();

        BookEntity book = new BookEntity("the-pass", "The Mountain Pass");

        SectionEntity begin = new SectionEntity("1", SectionType.BEGIN, "The pass is closed.");
        begin.addOption(new OptionEntity("Take the old road", "100"));
        begin.addOption(new OptionEntity("Climb the ridge", "200"));
        book.addSection(begin);

        SectionEntity road = new SectionEntity("100", SectionType.NODE, "Pines close in around you.");
        road.addOption(new OptionEntity("Press on", "900"));
        book.addSection(road);

        SectionEntity ridge = new SectionEntity("200", SectionType.NODE, "The wind cuts at you.");
        ridge.addOption(new OptionEntity("Descend", "900"));
        book.addSection(ridge);

        book.addSection(new SectionEntity("900", SectionType.END, "You reach the far side."));

        bookId = bookRepository.save(book).getId();
    }

    private RestTestClient.ResponseSpec startGame(long id) {
        return client.post().uri("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"bookId\": " + id + "}")
                .exchange();
    }

    @Test
    @DisplayName("starting a game places the player at the book's beginning with full health")
    void startsAtTheBeginning() {
        GameStateDto state = startGame(bookId)
                .expectStatus().isCreated()
                .expectBody(GameStateDto.class)
                .returnResult().getResponseBody();

        assertThat(state).isNotNull();
        assertThat(state.id()).isNotNull();
        assertThat(state.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(state.health()).isEqualTo(10);
        assertThat(state.bookSummary().id()).isEqualTo(bookId);
        assertThat(state.bookSummary().title()).isEqualTo("The Mountain Pass");
        assertThat(state.section().sectionRef()).isEqualTo("1");
        assertThat(state.section().type()).isEqualTo(SectionType.BEGIN);
        assertThat(state.section().text()).isEqualTo("The pass is closed.");
        // Nothing has been chosen yet, so nothing has happened to the player yet.
        assertThat(state.consequence()).isNull();
    }

    @Test
    @DisplayName("the beginning section offers its choices in the order the book lists them")
    void offersTheChoices() {
        GameStateDto state = startGame(bookId)
                .expectStatus().isCreated()
                .expectBody(GameStateDto.class)
                .returnResult().getResponseBody();

        assertThat(state).isNotNull();
        assertThat(state.section()).isNotNull();
        assertThat(state.section().options()).extracting(OptionDto::description)
                .containsExactly("Take the old road", "Climb the ridge");
        assertThat(state.section().options()).allSatisfy(option -> assertThat(option.id()).isPositive());
    }

    @Test
    @DisplayName("each start is its own game")
    void startsIndependentGames() {
        GameStateDto first = startGame(bookId).expectBody(GameStateDto.class).returnResult().getResponseBody();
        GameStateDto second = startGame(bookId).expectBody(GameStateDto.class).returnResult().getResponseBody();

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    @DisplayName("starting a game on a book that does not exist is a 404")
    void rejectsUnknownBook() {
        startGame(bookId + 999).expectStatus().isNotFound();
    }
}
