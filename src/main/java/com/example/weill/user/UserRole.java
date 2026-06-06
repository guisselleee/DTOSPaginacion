package com.example.weill.user;

public enum UserRole {
    EVALUADO("Evaluado"),
    LICENCIADO("Licenciado"),
    DECANA("Decana"),
    ADMIN_TECNICO("Administrador técnico");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTakeTest() {
        return this == EVALUADO;
    }

    public boolean canViewClinicalData() {
        return this == LICENCIADO || this == DECANA;
    }

    public boolean canManageSystem() {
        return this == ADMIN_TECNICO;
    }

    public boolean requiresPassword() {
        return this != EVALUADO;
    }
}
