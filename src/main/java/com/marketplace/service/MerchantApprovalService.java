package com.marketplace.service;

import com.marketplace.domain.MerchantApprovalStatus;
import com.marketplace.domain.MerchantProfile;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantApprovalService {

    private final MerchantProfileRepository merchantProfileRepository;
    private final UserRepository userRepository;

    public MerchantApprovalService(
            MerchantProfileRepository merchantProfileRepository, UserRepository userRepository) {
        this.merchantProfileRepository = merchantProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void approve(Long profileId) {
        MerchantProfile mp = merchantProfileRepository
                .findById(profileId)
                .orElseThrow(() -> new BusinessException("Merchant profile not found."));
        if (mp.getApprovalStatus() != MerchantApprovalStatus.PENDING) {
            throw new BusinessException("Profile is not pending.");
        }
        mp.setApprovalStatus(MerchantApprovalStatus.APPROVED);
        mp.setDeclineReason(null);
        User user = mp.getUser();
        user.addRole(Role.MERCHANT);
        userRepository.save(user);
        merchantProfileRepository.save(mp);
    }

    @Transactional
    public void decline(Long profileId, String reason) {
        MerchantProfile mp = merchantProfileRepository
                .findById(profileId)
                .orElseThrow(() -> new BusinessException("Merchant profile not found."));
        if (mp.getApprovalStatus() != MerchantApprovalStatus.PENDING) {
            throw new BusinessException("Profile is not pending.");
        }
        mp.setApprovalStatus(MerchantApprovalStatus.DECLINED);
        mp.setDeclineReason(reason != null ? reason.trim() : null);
        User user = mp.getUser();
        user.getRoles().remove(Role.MERCHANT);
        userRepository.save(user);
        merchantProfileRepository.save(mp);
    }
}
