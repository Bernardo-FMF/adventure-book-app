package com.adventurebook.backend.importer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class SlugGeneratorTest {
    private final SlugGenerator slugGenerator = new SlugGenerator();

    @ParameterizedTest(name = "\"{0}\" becomes \"{1}\"")
    @CsvSource({
            "'The Prisoner', the-prisoner",
            "'(Fixed) The Prisoner: Escape', fixed-the-prisoner-escape",
            "'Pirates of the Jade Sea', pirates-of-the-jade-sea",
            "'  Padded  Title  ', padded-title",
            "'Already-Hyphenated', already-hyphenated",
            "'Symbols !?*& everywhere', symbols-everywhere"
    })
    @DisplayName("punctuation and spacing become single separators")
    void buildsSlugFromTitle(String title, String expected) {
        assertThat(slugGenerator.slugFrom(title)).isEqualTo(expected);
    }
}
