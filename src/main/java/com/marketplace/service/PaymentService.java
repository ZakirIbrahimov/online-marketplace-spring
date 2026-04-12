package com.marketplace.service;

import com.marketplace.domain.User;

public interface PaymentService {

    void completeMockPayment(Long orderId, User payer);
}
