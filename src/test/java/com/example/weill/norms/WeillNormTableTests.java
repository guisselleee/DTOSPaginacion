package com.example.weill.norms;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class WeillNormTableTests {

    private final WeillNormTable table = new WeillNormTable();

    @Test
    void usesTheProvidedRawScoreThresholds() {
        assertThat(table.thresholdFor(NormGroup.AGE_7, 90)).isEqualTo(35);
        assertThat(table.thresholdFor(NormGroup.AGE_7, 25)).isEqualTo(29);
        assertThat(table.thresholdFor(NormGroup.AGE_16, 90)).isEqualTo(50);
        assertThat(table.thresholdFor(NormGroup.ILLITERATE, 50)).isEqualTo(17);
        assertThat(table.thresholdFor(NormGroup.ADULT, 30)).isEqualTo(30);
        assertThat(table.thresholdFor(NormGroup.UNIVERSITY, 5)).isEqualTo(35);
    }

    @Test
    void findsTheHighestPercentileWhoseThresholdWasReached() {
        assertThat(table.percentileFor(NormGroup.AGE_7, 35)).isEqualTo(90);
        assertThat(table.percentileFor(NormGroup.AGE_7, 34)).isEqualTo(80);
        assertThat(table.percentileFor(NormGroup.AGE_7, 21)).isEqualTo(30);
        assertThat(table.percentileFor(NormGroup.AGE_7, 20)).isEqualTo(20);
        assertThat(table.percentileFor(NormGroup.ILLITERATE, 30)).isEqualTo(90);
        assertThat(table.percentileFor(NormGroup.ADULT, 47)).isEqualTo(90);
        assertThat(table.percentileFor(NormGroup.UNIVERSITY, 56)).isEqualTo(80);
    }

    @Test
    void capsScoresAtTheAvailableNormativePercentiles() {
        assertThat(table.percentileFor(NormGroup.UNIVERSITY, 60)).isEqualTo(90);
        assertThat(table.percentileFor(NormGroup.UNIVERSITY, 0)).isEqualTo(5);
    }

    @Test
    void rejectsScoresOutsideTheSixtyItemTest() {
        assertThatIllegalArgumentException().isThrownBy(() -> table.percentileFor(NormGroup.ADULT, -1));
        assertThatIllegalArgumentException().isThrownBy(() -> table.percentileFor(NormGroup.ADULT, 61));
    }
}
