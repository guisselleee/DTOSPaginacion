package com.example.weill.admin;

public record AdminUserSummary(
    long id,
    String name,
    String email,
    String educationLevel,
    String municipality,
    String department
) {
}
