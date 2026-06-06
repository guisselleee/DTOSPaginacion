package com.example.weill.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTests {

    @Test
    void separatesEvaluationClinicalAndTechnicalCapabilities() {
        assertThat(UserRole.EVALUADO.canTakeTest()).isTrue();
        assertThat(UserRole.EVALUADO.canViewClinicalData()).isFalse();
        assertThat(UserRole.EVALUADO.canManageSystem()).isFalse();

        assertThat(UserRole.LICENCIADO.canViewClinicalData()).isTrue();
        assertThat(UserRole.DECANA.canViewClinicalData()).isTrue();
        assertThat(UserRole.LICENCIADO.canTakeTest()).isFalse();

        assertThat(UserRole.ADMIN_TECNICO.canManageSystem()).isTrue();
        assertThat(UserRole.ADMIN_TECNICO.canViewClinicalData()).isFalse();
        assertThat(UserRole.ADMIN_TECNICO.canTakeTest()).isFalse();
    }
}
