package com.example.weill.norms;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PopulationClassifierTests {

    private final PopulationClassifier classifier = new PopulationClassifier();

    @Test
    void classifiesStudentsFromSevenThroughSixteenByExactAge() {
        assertThat(classifier.classify(7, true, "Primaria"))
            .isEqualTo(new PopulationProfile(7, PopulationCategory.CHILD, NormGroup.AGE_7));
        assertThat(classifier.classify(12, true, "Primaria"))
            .isEqualTo(new PopulationProfile(12, PopulationCategory.CHILD, NormGroup.AGE_12));
        assertThat(classifier.classify(13, true, "Secundaria"))
            .isEqualTo(new PopulationProfile(13, PopulationCategory.ADOLESCENT, NormGroup.AGE_13));
        assertThat(classifier.classify(16, true, "Secundaria"))
            .isEqualTo(new PopulationProfile(16, PopulationCategory.ADOLESCENT, NormGroup.AGE_16));
    }

    @Test
    void classifiesPeopleSeventeenAndOlderByStudyStatusAndLiteracy() {
        assertThat(classifier.classify(17, true, "Universidad"))
            .isEqualTo(new PopulationProfile(17, PopulationCategory.UNIVERSITY_STUDENT, NormGroup.UNIVERSITY));
        assertThat(classifier.classify(35, false, "Secundaria"))
            .isEqualTo(new PopulationProfile(35, PopulationCategory.ADULT, NormGroup.ADULT));
        assertThat(classifier.classify(50, false, "Analfabeta"))
            .isEqualTo(new PopulationProfile(50, PopulationCategory.ILLITERATE_ADULT, NormGroup.ILLITERATE));
    }

    @Test
    void rejectsProfilesWithoutADefinedNormGroup() {
        assertThatIllegalArgumentException().isThrownBy(() -> classifier.classify(6, true, "Primaria"));
        assertThatIllegalArgumentException().isThrownBy(() -> classifier.classify(16, false, "Secundaria"));
    }
}
