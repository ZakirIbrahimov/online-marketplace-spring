package com.marketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marketplace.domain.Cart;
import com.marketplace.domain.CartItem;
import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.OrderStatus;
import com.marketplace.domain.PaymentStatus;
import com.marketplace.domain.Product;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceStockTest {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private OrderService orderService;
    @Autowired private PaymentService paymentService;
    @Autowired private CustomerOrderRepository customerOrderRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User shopper;
    private User merchant;
    private Product product;

    @BeforeEach
    void setUp() {
        merchant = new User();
        merchant.setEmail("m@x.test");
        merchant.setPassword(passwordEncoder.encode("p"));
        merchant.addRole(Role.SHOPPER);
        merchant.addRole(Role.MERCHANT);
        merchant = userRepository.save(merchant);

        shopper = new User();
        shopper.setEmail("s@x.test");
        shopper.setPassword(passwordEncoder.encode("p"));
        shopper.addRole(Role.SHOPPER);
        shopper = userRepository.save(shopper);

        product = new Product();
        product.setMerchant(merchant);
        product.setTitle("T");
        product.setDescription("D");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(3);
        product.setListingStatus(ListingStatus.ACTIVE);
        product = productRepository.save(product);

        Cart cart = new Cart();
        cart.setUser(shopper);
        cart = cartRepository.save(cart);
        CartItem line = new CartItem();
        line.setProduct(product);
        line.setQuantity(2);
        cart.addItem(line);
        cartRepository.save(cart);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(shopper, null, shopper.getAuthorities()));
    }

    @Test
    void checkoutThenPayReducesStock() {
        var order = orderService.checkout();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(3);

        paymentService.completeMockPayment(order.getId(), shopper);

        assertThat(customerOrderRepository.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PAID);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(1);
        assertThat(customerOrderRepository.findById(order.getId()).orElseThrow().getPayment().getStatus())
                .isEqualTo(PaymentStatus.SUCCEEDED);
    }

    @Test
    void payAgainFails() {
        var order = orderService.checkout();
        paymentService.completeMockPayment(order.getId(), shopper);
        assertThatThrownBy(() -> paymentService.completeMockPayment(order.getId(), shopper))
                .isInstanceOf(BusinessException.class);
    }
}
