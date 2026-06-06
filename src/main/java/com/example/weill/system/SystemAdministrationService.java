package com.example.weill.system;

import com.example.weill.audit.AuditLog;
import com.example.weill.audit.AuditLogRepository;
import com.example.weill.user.UserService;
import com.example.weill.user.WeillUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemAdministrationService {

    private final UserService userService;
    private final AuditLogRepository auditLogRepository;

    public SystemAdministrationService(
        UserService userService,
        AuditLogRepository auditLogRepository
    ) {
        this.userService = userService;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<WeillUser> authorizedUsers() {
        return userService.authorizedUsers();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> recentAuditEvents() {
        return auditLogRepository.findTop100ByOrderByOccurredAtDesc();
    }
}
