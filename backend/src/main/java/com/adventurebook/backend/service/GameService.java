package com.adventurebook.backend.service;

import com.adventurebook.backend.exception.BookNotFoundException;
import com.adventurebook.backend.exception.MissingSectionException;
import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.ConsequenceEntity;
import com.adventurebook.backend.persistence.GameSessionEntity;
import com.adventurebook.backend.persistence.SectionEntity;
import com.adventurebook.backend.persistence.types.SectionType;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.repository.GameRepository;
import com.adventurebook.backend.repository.SectionRepository;
import com.adventurebook.backend.response.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
    public GameStateDto start(long bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book with id " + bookId));

        SectionEntity beginningSection = sectionRepository.findByBookIdAndType(book.getId(), SectionType.BEGIN)
                .orElseThrow(() -> new MissingSectionException("Book " + bookId + " has no beginning section"));

        GameSessionEntity game = gameRepository.save(new GameSessionEntity(book, beginningSection));
        return mapGameState(game, null);
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
