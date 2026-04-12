package com.marketplace.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marketplace.service.AdminMetricsService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.marketplace.config.SecurityConfig;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.repository.OrderStatusEventRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.AdminUserService;
import com.marketplace.service.DisputeService;
import com.marketplace.service.MerchantApprovalService;
import com.marketplace.service.ProductService;

@WebMvcTest(controllers = AdminController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AdminControllerMvcTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AdminMetricsService adminMetricsService;
    @MockBean private MerchantProfileRepository merchantProfileRepository;
    @MockBean private MerchantApprovalService merchantApprovalService;
    @MockBean private ProductRepository productRepository;
    @MockBean private ProductService productService;
    @MockBean private CustomerOrderRepository customerOrderRepository;
    @MockBean private OrderStatusEventRepository orderStatusEventRepository;
    @MockBean private DisputeService disputeService;
    @MockBean private UserRepository userRepository;
    @MockBean private AdminUserService adminUserService;

    @MockBean private UserDetailsService userDetailsService;

    @Test
    void adminDashboardRequiresAuth() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDashboardOk() throws Exception {
        when(adminMetricsService.snapshot())
                .thenReturn(new AdminMetricsService.AdminMetrics(0, 0, BigDecimal.ZERO, 0, 0));
        mockMvc.perform(get("/admin")).andExpect(status().isOk());
    }

    @Test
    void adminDashboardForbiddenForShopper() throws Exception {
        mockMvc.perform(get("/admin").with(user("a@b.com").roles("SHOPPER")))
                .andExpect(status().isForbidden());
    }
}
