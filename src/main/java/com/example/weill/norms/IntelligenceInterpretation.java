package com.example.weill.norms;

public enum IntelligenceInterpretation {
    VERY_SUPERIOR("Intelecto muy superior"),
    SUPERIOR("Intelecto superior"),
    GOOD_INTELLIGENCE("Buena inteligencia"),
    AVERAGE("Intelecto medio"),
    NOT_DEFINED("Sin rango definido");

    private final String displayName;

    IntelligenceInterpretation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static IntelligenceInterpretation fromIq(int iq) {
        if (iq >= 140) {
            return VERY_SUPERIOR;
        }
        if (iq >= 124) {
            return SUPERIOR;
        }
        if (iq >= 116) {
            return GOOD_INTELLIGENCE;
        }
        if (iq >= 85) {
            return AVERAGE;
        }
        return NOT_DEFINED;
    }
}
