package com.example.weill.norms;

public enum PopulationCategory {
    CHILD("Niño"),
    ADOLESCENT("Adolescente"),
    UNIVERSITY_STUDENT("Universitario"),
    ADULT("Adulto"),
    ILLITERATE_ADULT("Analfabeta");

    private final String displayName;

    PopulationCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
