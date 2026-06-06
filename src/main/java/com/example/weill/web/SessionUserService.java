package com.example.weill.web;

import com.example.weill.user.UserService;
import com.example.weill.user.WeillUser;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionUserService {

    private final UserService userService;

    public SessionUserService(UserService userService) {
        this.userService = userService;
    }

    public Optional<WeillUser> currentUser(HttpSession session) {
        Object userId = session.getAttribute(AuthController.SESSION_USER_ID);
        if (!(userId instanceof Long id)) {
            return Optional.empty();
        }
        try {
            return Optional.of(userService.getUser(id));
        } catch (EntityNotFoundException exception) {
            session.invalidate();
            return Optional.empty();
        }
    }
}
