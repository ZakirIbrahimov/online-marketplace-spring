package com.marketplace.service;

import com.marketplace.domain.User;
import com.marketplace.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("You must be signed in.");
        }
        Object p = auth.getPrincipal();
        if (p instanceof User u) {
            return userRepository.findById(u.getId()).orElseThrow();
        }
        throw new BusinessException("Invalid session.");
    }
}
