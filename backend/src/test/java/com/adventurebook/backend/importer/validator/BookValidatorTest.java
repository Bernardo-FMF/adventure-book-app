package com.adventurebook.backend.importer.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BookValidatorTest {
    private final BookValidator validator = new BookValidator(new BookParser());

    private static String book(String... sections) {
        return """
                {"title":"T","author":"A","difficulty":"EASY","sections":[%s]}
                """.formatted(String.join(",", sections));
    }

    private static ValidationErrorType[] typesOf(ParseResult result) {
        return result.errors().stream().map(ValidationError::type).toArray(ValidationErrorType[]::new);
    }

    @Nested
    @DisplayName("A book that breaks no rule")
    class ValidBooks {
        @Test
        @DisplayName("a beginning leading to an ending is valid")
        void acceptsMinimalBook() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":2}]}""",
                    """
                    {"id":2,"type":"END","text":"fin"}"""
            ));

            assertThat(result.isValid()).isTrue();
            assertThat(result.errors()).isEmpty();
            assertThat(result.book().title()).isEqualTo("T");
        }

        @Test
        @DisplayName("rule 2 allows several endings")
        void acceptsMultipleEndings() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"a","gotoId":2},{"description":"b","gotoId":3}]}""",
                    """
                    {"id":2,"type":"END","text":"one"}""",
                    """
                    {"id":3,"type":"END","text":"two"}"""
            ));

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Rule 1: exactly one beginning")
    class BeginningRule {
        @Test
        @DisplayName("a book with no beginning is rejected")
        void rejectsBookWithoutBeginning() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"NODE","text":"mid","options":[{"description":"go","gotoId":2}]}""",
                    """
                    {"id":2,"type":"END","text":"fin"}"""
            ));

            assertThat(typesOf(result)).containsExactly(ValidationErrorType.NO_BEGIN);
        }

        @Test
        @DisplayName("every beginning past the first is reported")
        void rejectsBookWithSeveralBeginnings() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":4}]}""",
                    """
                    {"id":2,"type":"BEGIN","text":"also","options":[{"description":"go","gotoId":4}]}""",
                    """
                    {"id":3,"type":"BEGIN","text":"again","options":[{"description":"go","gotoId":4}]}""",
                    """
                    {"id":4,"type":"END","text":"fin"}"""
            ));

            assertThat(typesOf(result))
                    .containsExactly(ValidationErrorType.MULTIPLE_BEGIN, ValidationErrorType.MULTIPLE_BEGIN);
        }
    }

    @Nested
    @DisplayName("Rule 2: at least one ending")
    class EndingRule {
        @Test
        @DisplayName("a book that never ends is rejected")
        void rejectsBookWithoutEnding() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":2}]}""",
                    """
                    {"id":2,"type":"NODE","text":"mid","options":[{"description":"back","gotoId":1}]}"""
            ));

            assertThat(typesOf(result)).containsExactly(ValidationErrorType.NO_END);
        }
    }

    @Nested
    @DisplayName("Rule 3: options must resolve to a declared section")
    class GotoRule {
        @Test
        @DisplayName("an option pointing at an unknown id")
        void rejectsGotoToUnknownSection() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":999}]}""",
                    """
                    {"id":2,"type":"END","text":"fin"}"""
            ));

            assertThat(typesOf(result)).containsExactly(ValidationErrorType.UNRESOLVED_GOTO);
            assertThat(result.errors().getFirst().message())
                    .isEqualTo("Section '1' points to a missing section '999'");
        }

        @Test
        @DisplayName("an option with no destination reads differently from a mistyped one")
        void rejectsOptionWithoutGotoId() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"nowhere"}]}""",
                    """
                    {"id":2,"type":"END","text":"fin"}"""
            ));

            assertThat(typesOf(result)).containsExactly(ValidationErrorType.UNRESOLVED_GOTO);
            assertThat(result.errors().getFirst().message())
                    .isEqualTo("Section '1' has an option with no target section");
        }

        @Test
        @DisplayName("a numeric goto resolves against a string id")
        void matchesIdsAcrossJsonTypes() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":1000}]}""",
                    """
                    {"id":"1000","type":"END","text":"fin"}"""
            ));

            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Rule 4: a non-ending section must offer options")
    class DeadEndRule {
        @Test
        @DisplayName("a node with nowhere to go is rejected")
        void rejectsNodeWithoutOptions() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":2}]}""",
                    """
                    {"id":2,"type":"NODE","text":"stuck"}""",
                    """
                    {"id":3,"type":"END","text":"fin"}"""
            ));

            assertThat(typesOf(result)).containsExactly(ValidationErrorType.DEAD_END_NODE);
            assertThat(result.errors().getFirst().message())
                    .isEqualTo("Non-ending section '2' has no options");
        }

        @Test
        @DisplayName("an unreachable dead end is still reported")
        void rejectsUnreachableDeadEnd() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":2}]}""",
                    """
                    {"id":2,"type":"END","text":"fin"}""",
                    """
                    {"id":666,"type":"NODE","text":"nobody can get here"}"""
            ));

            assertThat(typesOf(result)).containsExactly(ValidationErrorType.DEAD_END_NODE);
        }
    }

    @Nested
    @DisplayName("Unusable input")
    class UnusableInput {
        @Test
        @DisplayName("a file that is not JSON is rejected before any rule runs")
        void rejectsMalformedJson() {
            assertThat(typesOf(validator.validate("{\"title\": "))).containsExactly(ValidationErrorType.UNPARSEABLE);
        }

        @Test
        @DisplayName("a book with an empty section list is rejected")
        void rejectsBookWithEmptySections() {
            assertThat(typesOf(validator.validate(book()))).containsExactly(ValidationErrorType.NO_SECTIONS);
        }
    }

    @Nested
    @DisplayName("Section ids")
    class SectionIds {
        @Test
        @DisplayName("a repeated section id is reported")
        void reportsDuplicateIds() {
            ParseResult result = validator.validate(book(
                    """
                    {"id":1,"type":"BEGIN","text":"start","options":[{"description":"go","gotoId":2}]}""",
                    """
                    {"id":1,"type":"NODE","text":"clash","options":[{"description":"go","gotoId":2}]}""",
                    """
                    {"id":2,"type":"END","text":"fin"}"""
            ));

            assertThat(typesOf(result)).containsExactly(ValidationErrorType.DUPLICATE_ID);
            assertThat(result.errors().getFirst().message())
                    .isEqualTo("Book has more than one section with id '1'");
        }
    }

    @Nested
    @DisplayName("Use actual books")
    class SeedBooks {
        static Stream<Arguments> seedBooks() {
            return Stream.of(
                    Arguments.of("crystal-caverns.json", new ValidationErrorType[]{
                            ValidationErrorType.DEAD_END_NODE}),
                    Arguments.of("dragon-quest.json", new ValidationErrorType[]{
                            ValidationErrorType.UNPARSEABLE}),
                    Arguments.of("pirates-jade-sea.json", new ValidationErrorType[]{
                            ValidationErrorType.UNRESOLVED_GOTO, ValidationErrorType.DEAD_END_NODE}),
                    Arguments.of("the-prisoner.json", new ValidationErrorType[]{
                            ValidationErrorType.DEAD_END_NODE}),
                    Arguments.of("new-the-prisoner.json", new ValidationErrorType[]{})
            );
        }

        private static String read(String filename) {
            try (InputStream in = BookValidatorTest.class.getResourceAsStream("/data/" + filename)) {
                assertThat(in).as("seed file %s must be on the classpath", filename).isNotNull();
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("seedBooks")
        @DisplayName("each shipped book reports the problems it was written to demonstrate")
        void reportsKnownProblemsPerBook(String filename, ValidationErrorType[] expected) {
            assertThat(typesOf(validator.validate(read(filename)))).containsExactlyInAnyOrder(expected);
        }

        @Test
        @DisplayName("the section every book hides at id 666 is the dead end")
        void namesTheDeadEndSection() {
            ParseResult result = validator.validate(read("the-prisoner.json"));

            assertThat(result.errors().getFirst().message())
                    .isEqualTo("Non-ending section '666' has no options");
        }

        @Test
        @DisplayName("the repaired prisoner book is the one shipped book that passes")
        void acceptsRepairedPrisonerBook() {
            ParseResult result = validator.validate(read("new-the-prisoner.json"));

            assertThat(result.isValid()).isTrue();
            assertThat(result.book().title()).isEqualTo("(Fixed) The Prisoner: Escape");
        }

        @Test
        @DisplayName("the pirate book names the section its beginning cannot reach")
        void namesTheMissingSection() {
            ParseResult result = validator.validate(read("pirates-jade-sea.json"));

            assertThat(result.errors()).extracting(ValidationError::message)
                    .contains("Section '1' points to a missing section '999'");
        }
    }
}
