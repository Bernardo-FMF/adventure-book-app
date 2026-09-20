package com.adventurebook.backend.controller;

import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.ConsequenceEntity;
import com.adventurebook.backend.persistence.OptionEntity;
import com.adventurebook.backend.persistence.SectionEntity;
import com.adventurebook.backend.persistence.types.ConsequenceType;
import com.adventurebook.backend.persistence.types.GameStatus;
import com.adventurebook.backend.persistence.types.SectionType;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.response.GameStateDto;
import com.adventurebook.backend.response.GameSummaryDto;
import com.adventurebook.backend.response.OptionDto;
import com.adventurebook.backend.utils.PostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

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

    private static final String BEARER_PLAYER = "Bearer tester";

    private RestTestClient client;
    private RestTestClient anonymousClient;
    private long bookId;

    @AfterEach
    void removeSeededBooks() {
        bookRepository.deleteAll();
    }

    private RestTestClient playerNamed(String authorization) {
        return RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
    }

    private List<GameSummaryDto> activeGames(RestTestClient forPlayer) {
        return forPlayer.get().uri("/api/games")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<GameSummaryDto>>() {})
                .returnResult().getResponseBody();
    }

    private RestTestClient.ResponseSpec startGame(long id) {
        return client.post().uri("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"bookId\": " + id + "}")
                .exchange();
    }

    private GameStateDto start() {
        return startGame(bookId).expectBody(GameStateDto.class).returnResult().getResponseBody();
    }

    private long optionId(GameStateDto state, String description) {
        return state.section().options().stream()
                .filter(option -> option.description().equals(description))
                .findFirst()
                .orElseThrow()
                .id();
    }

    private GameStateDto choose(GameStateDto state, String description) {
        return client.post().uri("/api/games/" + state.id() + "/choices/" + optionId(state, description))
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameStateDto.class)
                .returnResult().getResponseBody();
    }

    @BeforeEach
    void seedBook() {
        // Every game endpoint resolves its player from this header, so the default spares each test from repeating it.
        client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.AUTHORIZATION, BEARER_PLAYER)
                .build();
        anonymousClient = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        bookRepository.deleteAll();

        BookEntity book = new BookEntity("the-pass", "The Mountain Pass");

        SectionEntity begin = new SectionEntity("1", SectionType.BEGIN, "The pass is closed.");
        begin.addOption(new OptionEntity("Take the old road", "100"));
        OptionEntity ridgeOption = new OptionEntity("Climb the ridge", "200");
        ridgeOption.setConsequence(new ConsequenceEntity(ConsequenceType.LOSE_HEALTH, 4, "The wind cuts through your cloak."));
        begin.addOption(ridgeOption);
        book.addSection(begin);

        SectionEntity road = new SectionEntity("100", SectionType.NODE, "Pines close in around you.");
        road.addOption(new OptionEntity("Press on", "900"));
        OptionEntity restOption = new OptionEntity("Rest by the wagon", "200");
        restOption.setConsequence(new ConsequenceEntity(ConsequenceType.GAIN_HEALTH, 5, "You feel steadier."));
        road.addOption(restOption);
        book.addSection(road);

        SectionEntity ridge = new SectionEntity("200", SectionType.NODE, "The wind cuts at you.");
        ridge.addOption(new OptionEntity("Descend", "900"));
        OptionEntity fatalOption = new OptionEntity("Cross the ice bridge", "900");
        fatalOption.setConsequence(new ConsequenceEntity(ConsequenceType.LOSE_HEALTH, 10, "The ice gives way beneath you."));
        ridge.addOption(fatalOption);
        book.addSection(ridge);

        book.addSection(new SectionEntity("900", SectionType.END, "You reach the far side."));

        bookId = bookRepository.save(book).getId();
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
    @DisplayName("starting a game on a book that does not exist is a 404")
    void rejectsUnknownBook() {
        startGame(bookId + 999).expectStatus().isNotFound();
    }

    @Test
    @DisplayName("a started game can be read back unchanged")
    void readsBackTheGame() {
        GameStateDto started = startGame(bookId)
                .expectBody(GameStateDto.class).returnResult().getResponseBody();

        GameStateDto fetched = client.get().uri("/api/games/" + started.id()).exchange()
                .expectStatus().isOk()
                .expectBody(GameStateDto.class)
                .returnResult().getResponseBody();

        assertThat(fetched).isNotNull();
        assertThat(fetched.id()).isEqualTo(started.id());
        assertThat(fetched.status()).isEqualTo(started.status());
        assertThat(fetched.health()).isEqualTo(started.health());
        assertThat(fetched.section().sectionRef()).isEqualTo(started.section().sectionRef());
        assertThat(fetched.section().options()).hasSameSizeAs(started.section().options());
    }

    @Test
    @DisplayName("a choice without a consequence just moves the player on")
    void movesToTheChosenSection() {
        GameStateDto after = choose(start(), "Take the old road");

        assertThat(after.section().sectionRef()).isEqualTo("100");
        assertThat(after.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(after.health()).isEqualTo(10);
        assertThat(after.consequence()).isNull();
        assertThat(after.section().options()).isNotEmpty();
    }

    @Test
    @DisplayName("a choice reduces health and reports what happened")
    void appliesTheConsequence() {
        GameStateDto after = choose(start(), "Climb the ridge");

        assertThat(after.health()).isEqualTo(6);
        assertThat(after.section().sectionRef()).isEqualTo("200");
        assertThat(after.consequence().type()).isEqualTo(ConsequenceType.LOSE_HEALTH);
        assertThat(after.consequence().amount()).isEqualTo(4);
        assertThat(after.consequence().text()).isEqualTo("The wind cuts through your cloak.");
    }

    @Test
    @DisplayName("a harmless choice clears the previous consequence")
    void clearsThePreviousConsequence() {
        GameStateDto hurt = choose(start(), "Climb the ridge");

        GameStateDto after = choose(hurt, "Descend");

        assertThat(after.consequence()).isNull();
    }

    @Test
    @DisplayName("healing never takes the player above the starting health")
    void capsHealing() {
        GameStateDto onTheRoad = choose(start(), "Take the old road");

        GameStateDto after = choose(onTheRoad, "Rest by the wagon");

        assertThat(after.health()).isEqualTo(10);
    }

    @Test
    @DisplayName("reaching an ending finishes the game and offers nothing further")
    void finishesAtAnEnding() {
        GameStateDto onTheRoad = choose(start(), "Take the old road");

        GameStateDto after = choose(onTheRoad, "Press on");

        assertThat(after.status()).isEqualTo(GameStatus.FINISHED);
        assertThat(after.section().sectionRef()).isEqualTo("900");
        assertThat(after.section().options()).isEmpty();
    }

    @Test
    @DisplayName("losing all health kills the player where they stood")
    void killsThePlayer() {
        GameStateDto onTheRidge = choose(start(), "Climb the ridge");

        GameStateDto after = choose(onTheRidge, "Cross the ice bridge");

        assertThat(after.status()).isEqualTo(GameStatus.DEAD);
        assertThat(after.health()).isZero();
        assertThat(after.section().sectionRef()).isEqualTo("200");
        assertThat(after.section().options()).isEmpty();
        assertThat(after.consequence().text()).isEqualTo("The ice gives way beneath you.");
    }

    @Test
    @DisplayName("a dead player is told what killed them even after the session is over")
    void remembersTheFatalConsequence() {
        GameStateDto onTheRidge = choose(start(), "Climb the ridge");
        GameStateDto dead = choose(onTheRidge, "Cross the ice bridge");

        GameStateDto fetched = client.get().uri("/api/games/" + dead.id()).exchange()
                .expectStatus().isOk()
                .expectBody(GameStateDto.class)
                .returnResult().getResponseBody();

        assertThat(fetched.status()).isEqualTo(GameStatus.DEAD);
        assertThat(fetched.consequence().text()).isEqualTo("The ice gives way beneath you.");
    }

    @Test
    @DisplayName("an option from another section is refused")
    void refusesAnOptionFromElsewhere() {
        GameStateDto started = start();
        GameStateDto onTheRoad = choose(started, "Take the old road");
        long staleOption = optionId(started, "Climb the ridge");

        client.post().uri("/api/games/" + onTheRoad.id() + "/choices/" + staleOption)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("no more choices once the adventure is over")
    void refusesChoicesAfterTheEnd() {
        GameStateDto onTheRoad = choose(start(), "Take the old road");
        long pressOn = optionId(onTheRoad, "Press on");
        choose(onTheRoad, "Press on");

        client.post().uri("/api/games/" + onTheRoad.id() + "/choices/" + pressOn)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("a request without a player name is refused")
    void refusesAnAnonymousRequest() {
        anonymousClient.post().uri("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"bookId\": " + bookId + "}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("a player cannot start a second game for a book they are already playing")
    void refusesASecondGameForTheSameBook() {
        start();

        startGame(bookId).expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("another player's game is invisible rather than forbidden")
    void hidesSomeoneElsesGame() {
        GameStateDto mine = start();

        RestTestClient intruder = playerNamed("Bearer someone-else");

        intruder.get().uri("/api/games/" + mine.id()).exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("a game in progress is listed so it can be resumed")
    void listsTheGameInProgress() {
        GameStateDto started = start();

        assertThat(activeGames(client))
                .singleElement()
                .satisfies(summary -> {
                    assertThat(summary.gameId()).isEqualTo(started.id());
                    assertThat(summary.bookId()).isEqualTo(bookId);
                });
    }
}

