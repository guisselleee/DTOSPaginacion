package com.example.weill.audit;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfiguration {

    @Bean
    ServletListenerRegistrationBean<SessionAuditListener> sessionAuditListenerRegistration(
        SessionAuditListener listener
    ) {
        return new ServletListenerRegistrationBean<>(listener);
    }
}
