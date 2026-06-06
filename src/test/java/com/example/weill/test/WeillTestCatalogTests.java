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
    void mapsItemsAndOptionsToTheirPageImageDirectories() {
        TestItem firstItem = catalog.item(1, 1, 1);
        assertThat(firstItem.targetImagePath())
            .isEqualTo("/images/weill_images/page_1/item_01/main.jpg");
        assertThat(firstItem.options())
            .extracting(OptionImage::imagePath)
            .containsExactly(
                "/images/weill_images/page_1/item_01/option_01.jpg",
                "/images/weill_images/page_1/item_01/option_02.jpg",
                "/images/weill_images/page_1/item_01/option_03.jpg",
                "/images/weill_images/page_1/item_01/option_04.jpg",
                "/images/weill_images/page_1/item_01/option_05.jpg",
                "/images/weill_images/page_1/item_01/option_06.jpg",
                "/images/weill_images/page_1/item_01/option_07.jpg",
                "/images/weill_images/page_1/item_01/option_08.jpg"
            );

        assertThat(catalog.item(3, 2, 2).targetImagePath())
            .isEqualTo("/images/weill_images/page_3/item_05/main.jpg");
        assertThat(catalog.item(5, 4, 3).targetImagePath())
            .isEqualTo("/images/weill_images/page_5/item_12/main.jpg");
    }

    @Test
    void rejectsCoordinatesOutsideTheSixtyItems() {
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.correctOption(6, 1, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.correctOption(1, 5, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.correctOption(1, 1, 4));
        assertThatIllegalArgumentException().isThrownBy(() -> catalog.isCorrect(1, 1, 1, 9));
    }
}
