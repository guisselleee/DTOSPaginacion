package com.example.weill.user;

import java.time.LocalDate;

public record UserRegistrationForm(
    String name,
    String email,
    String educationLevel,
    String schoolCenter,
    String municipality,
    String department,
    LocalDate birthDate,
    Boolean currentlyStudying
) {
}
