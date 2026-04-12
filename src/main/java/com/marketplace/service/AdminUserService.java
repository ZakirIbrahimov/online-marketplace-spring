package com.marketplace.service;

import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AdminUserService(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        currentUserService.requireCurrentUser();
        User target = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found."));
        if (target.getRoles().contains(Role.ADMIN)) {
            throw new BusinessException("Cannot change admin account state here.");
        }
        target.setEnabled(enabled);
        userRepository.save(target);
    }
}
