package com.Mockito.LearningMockito.client;

import java.math.BigDecimal;

public interface PaymentClient {
    boolean charge(String customerId, BigDecimal amount);
}
