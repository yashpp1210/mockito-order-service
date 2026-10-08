package com.Mockito.LearningMockito.service;

import com.Mockito.LearningMockito.client.NotificationClient;
import com.Mockito.LearningMockito.client.PaymentClient;
import com.Mockito.LearningMockito.exception.PaymetFailedExecption;
import com.Mockito.LearningMockito.model.Order;
import com.Mockito.LearningMockito.model.OrderStatus;
import com.Mockito.LearningMockito.repository.OrderRespository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRespository orderRespository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private  OrderService orderService;


    @Test
    void placeOrder_whenPaymentSucceeds_savesConfirmedOrder(){

        String customerId = "cust-1";
        BigDecimal amount = new BigDecimal("500.00");

        when(paymentClient.charge(customerId,amount)).thenReturn(true);

        when(orderRespository.save(org.mockito.ArgumentMatchers.any(Order.class))).thenAnswer(invocation ->{
                Order o = invocation.getArgument(0);
                o.setId(1L);
                return o;
        });

        // ACT
        Order result = orderService.placeOrder(customerId, amount);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.getCustomerId()).isEqualTo(customerId);




    }

    // ---------- STAGE 2 ----------
    @Test
    void placeOrder_whenPaymentFails_throwsExceptionAndNeverSaves(){
        //Arrange
        String customerId = "cust-2";
        BigDecimal amount = new BigDecimal("300.00");


        when(paymentClient.charge(customerId,amount)).thenReturn(false);

        // ACT + ASSERT (exception)
        assertThatThrownBy(()-> orderService.placeOrder(customerId,amount)).isInstanceOf(PaymetFailedExecption.class).hasMessageContaining("declined");

        // ASSERT (behavior)
        verify(orderRespository,never()).save(any(Order.class));


    }

    // Test 3
    @Test
    void placeOrder_whenPaymentSucceeds_sendsConfirmationNotification(){

        //Arrange
        String customerId = "cust-3";
        BigDecimal amount  = new BigDecimal("750.00");

        when(paymentClient.charge(customerId,amount)).thenReturn(true);

        when(orderRespository.save(any(Order.class))).thenAnswer(invocation ->{
            Order o = invocation.getArgument(0);
            o.setId(10L);
            return o;
        });

        doNothing().when(notificationClient).sendConfirmation(customerId,10L);

        //ACT
        orderService.placeOrder(customerId,amount);

        //ASSERT
        verify(notificationClient).sendConfirmation(customerId,10L);


    }

}
