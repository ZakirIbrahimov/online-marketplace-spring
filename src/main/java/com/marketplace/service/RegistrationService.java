package com.marketplace.service;

import com.marketplace.domain.MerchantApprovalStatus;
import com.marketplace.domain.MerchantProfile;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            MerchantProfileRepository merchantProfileRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerShopper(String email, String rawPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email already registered.");
        }
        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.addRole(Role.SHOPPER);
        userRepository.save(u);
    }

    @Transactional
    public void registerMerchant(String email, String rawPassword, String businessName, String description) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email already registered.");
        }
        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.addRole(Role.SHOPPER);
        userRepository.save(u);

        MerchantProfile mp = new MerchantProfile();
        mp.setUser(u);
        mp.setBusinessName(businessName.trim());
        mp.setDescription(description != null ? description.trim() : null);
        mp.setApprovalStatus(MerchantApprovalStatus.PENDING);
        merchantProfileRepository.save(mp);
    }
}
