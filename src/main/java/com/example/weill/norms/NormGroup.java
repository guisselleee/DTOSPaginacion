package com.example.weill.norms;

public enum NormGroup {
    AGE_7("7"),
    AGE_8("8"),
    AGE_9("9"),
    AGE_10("10"),
    AGE_11("11"),
    AGE_12("12"),
    AGE_13("13"),
    AGE_14("14"),
    AGE_15("15"),
    AGE_16("16"),
    ILLITERATE("AN"),
    ADULT("AD"),
    UNIVERSITY("UN");

    private final String code;

    NormGroup(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static NormGroup forAge(int age) {
        if (age < 7 || age > 16) {
            throw new IllegalArgumentException("Age norm group must be between 7 and 16.");
        }
        return valueOf("AGE_" + age);
    }
}
