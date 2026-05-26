package com.example.weill.test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class WeillTestCatalogTests {

    private final WeillTestCatalog catalog = new WeillTestCatalog();

    @Test
    void repeatsTwelveItemAnswerKeyAcrossFivePages() {
        assertThat(catalog.totalItems()).isEqualTo(60);
        assertThat(catalog.pages()).hasSize(5);
        assertThat(catalog.pages()).allSatisfy(page -> assertThat(page.items()).hasSize(12));

        assertThat(catalog.correctOption(1, 1, 1)).isEqualTo(2);
        assertThat(catalog.correctOption(1, 1, 2)).isEqualTo(8);
        assertThat(catalog.correctOption(1, 1, 3)).isEqualTo(7);
        assertThat(catalog.correctOption(5, 4, 1)).isEqualTo(7);
        assertThat(catalog.correctOption(5, 4, 2)).isEqualTo(3);
        assertThat(catalog.correctOption(5, 4, 3)).isEqualTo(2);
    }

    @Test
    void scoresSelectedOptionAgainstTheCatalogAnswer() {
        assertThat(catalog.isCorrect(3, 2, 2, 5)).isTrue();
        assertThat(catalog.isCorrect(3, 2, 2, 4)).isFalse();
    }

    @Test
    void rejectsCoordinatesOutsideTheSixtyItems() {
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.correctOption(6, 1, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.correctOption(1, 5, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.correctOption(1, 1, 4));
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.isCorrect(1, 1, 1, 9));
    }
}
