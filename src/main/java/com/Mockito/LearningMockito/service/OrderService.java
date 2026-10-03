package com.Mockito.LearningMockito.service;

import com.Mockito.LearningMockito.client.PaymentClient;
import com.Mockito.LearningMockito.exception.PaymetFailedExecption;
import com.Mockito.LearningMockito.model.Order;
import com.Mockito.LearningMockito.model.OrderStatus;
import com.Mockito.LearningMockito.repository.OrderRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRespository orderRespository;
    private final PaymentClient paymentClient;

    public OrderService(OrderRespository orderRespository,PaymentClient paymentClient)
    {
        this.orderRespository=orderRespository;
        this.paymentClient=paymentClient;
    }

    public Order placeOrder(String customerId, BigDecimal amount){
        if(amount == null || amount.signum() <= 0){
            throw new IllegalArgumentException("Order amount must be positive");

        }

        boolean paymentSuccess = paymentClient.charge(customerId,amount);

        if(!paymentSuccess)
        {
            log.warn("Payment failed for customer {} amount {} ", customerId,amount);
            throw new PaymetFailedExecption("Payment declined for customer : " + customerId);
        }

        Order order = new Order(customerId,amount, OrderStatus.CONFIRMED);
        Order saved = orderRespository.save(order);
        log.info("Order {} confirmed for customer {}",saved.getId(),saved.getCustomerId());
        return saved ;



    }


}
