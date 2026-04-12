package com.marketplace.config;

import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.MerchantApprovalStatus;
import com.marketplace.domain.MerchantProfile;
import com.marketplace.domain.Product;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevDataLoader {

    @Bean
    CommandLineRunner seedUsers(
            UserRepository userRepository,
            MerchantProfileRepository merchantProfileRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByEmailIgnoreCase("admin@marketplace.local")) {
                return;
            }
            User admin = new User();
            admin.setEmail("admin@marketplace.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.addRole(Role.ADMIN);
            userRepository.save(admin);

            User merchantUser = new User();
            merchantUser.setEmail("merchant@marketplace.local");
            merchantUser.setPassword(passwordEncoder.encode("merchant123"));
            merchantUser.addRole(Role.SHOPPER);
            merchantUser.addRole(Role.MERCHANT);
            userRepository.save(merchantUser);

            MerchantProfile mp = new MerchantProfile();
            mp.setUser(merchantUser);
            mp.setBusinessName("Demo Crafts");
            mp.setDescription("Seeded merchant for demos.");
            mp.setApprovalStatus(MerchantApprovalStatus.APPROVED);
            merchantProfileRepository.save(mp);

            Product p1 = new Product();
            p1.setMerchant(merchantUser);
            p1.setTitle("Handmade Mug");
            p1.setDescription("Ceramic mug, 350ml.");
            p1.setPrice(new BigDecimal("18.99"));
            p1.setStock(40);
            p1.setListingStatus(ListingStatus.ACTIVE);
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setMerchant(merchantUser);
            p2.setTitle("Canvas Tote");
            p2.setDescription("Heavy cotton tote bag.");
            p2.setPrice(new BigDecimal("24.50"));
            p2.setStock(25);
            p2.setListingStatus(ListingStatus.ACTIVE);
            productRepository.save(p2);

            User shopper = new User();
            shopper.setEmail("shopper@marketplace.local");
            shopper.setPassword(passwordEncoder.encode("shopper123"));
            shopper.addRole(Role.SHOPPER);
            userRepository.save(shopper);
        };
    }
}
