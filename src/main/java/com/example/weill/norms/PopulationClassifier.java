package com.example.weill.norms;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class PopulationClassifier {

    public PopulationProfile classify(int age, boolean currentlyStudying, String educationLevel) {
        if (age < 7) {
            throw new IllegalArgumentException("The Weill norms require a minimum age of 7.");
        }

        if (age <= 12) {
            requireStudyingMinor(currentlyStudying);
            return new PopulationProfile(age, PopulationCategory.CHILD, NormGroup.forAge(age));
        }
        if (age <= 16) {
            requireStudyingMinor(currentlyStudying);
            return new PopulationProfile(age, PopulationCategory.ADOLESCENT, NormGroup.forAge(age));
        }
        if (currentlyStudying) {
            return new PopulationProfile(age, PopulationCategory.UNIVERSITY_STUDENT, NormGroup.UNIVERSITY);
        }
        if (isIlliterate(educationLevel)) {
            return new PopulationProfile(age, PopulationCategory.ILLITERATE_ADULT, NormGroup.ILLITERATE);
        }
        return new PopulationProfile(age, PopulationCategory.ADULT, NormGroup.ADULT);
    }

    private void requireStudyingMinor(boolean currentlyStudying) {
        if (!currentlyStudying) {
            throw new IllegalArgumentException("No norm group is defined for a non-student under age 17.");
        }
    }

    private boolean isIlliterate(String educationLevel) {
        if (educationLevel == null) {
            return false;
        }
        String normalized = Normalizer.normalize(educationLevel, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);
        return normalized.contains("analfabet");
    }
}
