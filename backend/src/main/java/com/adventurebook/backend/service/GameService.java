package com.adventurebook.backend.service;

import com.adventurebook.backend.exception.*;
import com.adventurebook.backend.persistence.*;
import com.adventurebook.backend.persistence.types.ConsequenceType;
import com.adventurebook.backend.persistence.types.GameStatus;
import com.adventurebook.backend.persistence.types.SectionType;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.repository.GameRepository;
import com.adventurebook.backend.repository.SectionRepository;
import com.adventurebook.backend.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GameService {
    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final BookRepository bookRepository;
    private final SectionRepository sectionRepository;

    public GameService(GameRepository gameRepository, BookRepository bookRepository, SectionRepository sectionRepository) {
        this.gameRepository = gameRepository;
        this.bookRepository = bookRepository;
        this.sectionRepository = sectionRepository;
    }

    /**
     * Begins a game session for the specified player.
     * <p>
     * Here we perform several database operations:
     * 1. Fetch the book;
     * 2. Check the existence of a game for the same book and user;
     * 3. Fetch the beginning section of the book. We could've obtained this by accessing the sections field of the book.
     * However, this would load all sections while we only need the first;
     * 4. Insert the new row;
     * 5. While mapping the state of the game, we'll need to load the options.
     * <p>
     * One of the defined constraints is that there can only be one active game for the combination of bookId and userId.
     * This is validated in code, but the database also performs this check by using a partial unique index (uq_active_game_per_book).
     *
     * @param player the caller, resolved from the Authorization header.
     * @param bookId the book to play.
     * @return the state of the new game.
     */
    @Transactional
    public GameStateDto start(PlayerEntity player, long bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book with id " + bookId));

        boolean gameAlreadyExists = gameRepository.existsByPlayerIdAndBookIdAndStatus(player.getId(), bookId, GameStatus.IN_PROGRESS);
        if (gameAlreadyExists) {
            throw new InvalidGameSessionException("Game for book " + book.getTitle() + " is already created for user " + player.getUsername());
        }

        SectionEntity beginningSection = sectionRepository.findByBookIdAndType(book.getId(), SectionType.BEGIN)
                .orElseThrow(() -> new MissingSectionException("Book " + bookId + " has no beginning section"));

        GameSessionEntity game = gameRepository.save(new GameSessionEntity(book, beginningSection, player));
        log.info("Game {} started for player {} on book {}", game.getId(), player.getId(), bookId);

        return mapGameState(game, null);
    }

    /**
     * Reads a game back, so the player can resume it.
     * <p>
     * Here we perform a varying amount of database operations, with a minimum of four, maximum of five:
     * 1. Fetch the game row; To check if the game belongs to the player, we access the id, but it was already loaded even
     * though the user is a lazy proxy. This is because the user id is a FK in the game table.
     * 2. Load the games' current section;
     * 3. Load the games' current options;
     * 4. Load the games' book (we access the books title, so it ends up being an extra query);
     * If there's a consequence assigned to the current state of the game:
     * 5. Load the games' current consequence;
     * <p>
     * A game belonging to somebody else is reported as missing rather than forbidden. This way we don't have ID leaks.
     *
     * @param player the caller, resolved from the Authorization header.
     * @param gameId the game to read.
     * @return the current state of the game.
     */
    @Transactional(readOnly = true)
    public GameStateDto get(PlayerEntity player, UUID gameId) {
        GameSessionEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameSessionNotFoundException("Game with id " + gameId + " not found"));

        if (!game.belongsTo(player)) {
            throw new GameSessionNotFoundException("Game with id " + gameId + " not found");
        }

        return mapGameState(game, game.getLastConsequence());
    }

    /**
     * Advances the game by applying an option: applies the consequence, then moves, unless the consequence ended the run.
     * <p>
     * The cost of this operation in database operations come from walking through the associations of the game entity:
     * 1. Fetching the game;
     * 2. Loading the book;
     * 3. Loading the current section;
     * 4. Loading the options of the current section;
     * 5. Loading the consequence of the chosen option (if it has one);
     * 6. Fetch the new section pointed by the chosen option;
     * 7. Perform the update of the game;
     * This could be minimized by having a fetch graph applied on the game fetch, so we could load the book, section, options,
     * and consequence in one go. The problem here that this results in a join fetch, and in this case it would result in as
     * many rows as there are options. These rows would have duplicate information related to the game and the current section,
     * which at this small scale has minimal impact (so for a section with 2 options, we'd have 2 rows in the join fetch).
     * <p>
     * The consequence of the choice is stored before it's applied, so if a choice results in the game ending, we'll still
     * have the indication of what occurred and can report it to the player. When a player dies, the section doesn't move
     * forward, since that would display the consequence that killed the player, with the next section had they survived.
     *
     * @param player   the caller, resolved from the Authorization header.
     * @param gameId   the game being played.
     * @param optionId the option taken, which must belong to the section the game is currently on.
     * @return the state after the choice, carrying the consequence that was applied.
     */
    @Transactional
    public GameStateDto makeChoice(PlayerEntity player, UUID gameId, long optionId) {
        GameSessionEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameSessionNotFoundException("Game with id " + gameId + " not found"));

        if (!game.belongsTo(player)) {
            throw new GameSessionNotFoundException("Game with id " + gameId + " not found");
        }

        if (game.isOver()) {
            throw new InvalidChoiceException("This session is already over");
        }

        List<OptionEntity> options = game.getSection().getOptions();
        OptionEntity option = options.stream()
                .filter(candidate -> Objects.equals(candidate.getId(), optionId))
                .findFirst()
                .orElseThrow(() -> new InvalidChoiceException("Option " + optionId + " is not offered by section '" + game.getSection().getSectionRef() + "'"));

        String previousSectionRef = game.getSection().getSectionRef();

        ConsequenceEntity consequence = option.getConsequence();
        game.recordConsequence(consequence);

        if (Objects.nonNull(consequence)) {
            if (consequence.getType() == ConsequenceType.LOSE_HEALTH) {
                game.loseHealth(consequence.getAmount());
            } else {
                game.gainHealth(consequence.getAmount());
            }
        }

        if (!game.isOver()) {
            SectionEntity next = sectionRepository
                    .findByBookIdAndSectionRef(game.getBook().getId(), option.getGotoRef())
                    .orElseThrow(() -> new MissingSectionException("Option " + optionId + " points at missing section '" + option.getGotoRef() + "'"));
            game.moveTo(next);
        }

        gameRepository.save(game);

        log.info("Game {}: section {} -> {} via option {} ({} health, {})",
                gameId, previousSectionRef, game.getSection().getSectionRef(), optionId, game.getHealth(), game.getStatus());

        return mapGameState(game, consequence);
    }

    /**
     * Lists the games in progress of the specified player.
     * <p>
     * It performs a single query every time. Even though we access the book {@code getBook().getId()}, we don't perform another query.
     * This is because the book is a lazy proxy, and we're only accessing the id, which was already loaded on the initial query.
     * If we tried accessing a different field of the book, that is where another query would be made.
     *
     * @param player the caller, resolved from the Authorization header.
     * @return the games this player has in progress, empty if none.
     */
    @Transactional(readOnly = true)
    public List<GameSummaryDto> listActive(PlayerEntity player) {
        return gameRepository.findByPlayerIdAndStatus(player.getId(), GameStatus.IN_PROGRESS)
                .stream()
                .map(this::mapGameSummary)
                .toList();
    }

    private GameSummaryDto mapGameSummary(GameSessionEntity game) {
        return new GameSummaryDto(game.getId(), game.getBook().getId());
    }

    private GameStateDto mapGameState(GameSessionEntity game, ConsequenceEntity lastConsequence) {
        SectionEntity section = game.getSection();

        List<OptionDto> options = game.isOver()
                ? List.of()
                : section.getOptions().stream()
                .map(option -> new OptionDto(option.getId(), option.getDescription()))
                .toList();

        return new GameStateDto(
                game.getId(),
                new BookSummaryDto(game.getBook().getId(), game.getBook().getTitle()),
                game.getStatus(),
                game.getHealth(),
                new SectionDto(section.getSectionRef(), section.getText(), section.getType(), options),
                Objects.isNull(lastConsequence)
                        ? null
                        : new ConsequenceDto(lastConsequence.getType(), lastConsequence.getAmount(), lastConsequence.getText())
        );
    }
}
