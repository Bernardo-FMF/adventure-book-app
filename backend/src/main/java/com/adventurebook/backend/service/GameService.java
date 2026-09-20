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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final BookRepository bookRepository;
    private final SectionRepository sectionRepository;

    public GameService(GameRepository gameRepository, BookRepository bookRepository, SectionRepository sectionRepository) {
        this.gameRepository = gameRepository;
        this.bookRepository = bookRepository;
        this.sectionRepository = sectionRepository;
    }

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
        return mapGameState(game, null);
    }

    @Transactional(readOnly = true)
    public GameStateDto get(PlayerEntity player, UUID gameId) {
        GameSessionEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameSessionNotFoundException("Game with id " + gameId + " not found"));

        if (!game.belongsTo(player)) {
            throw new GameSessionNotFoundException("Game with id " + gameId + " not found");
        }

        return mapGameState(game, game.getLastConsequence());
    }

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

        return mapGameState(game, consequence);
    }

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
