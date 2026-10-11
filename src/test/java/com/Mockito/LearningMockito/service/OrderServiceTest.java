package com.Mockito.LearningMockito.service;

import com.Mockito.LearningMockito.client.NotificationClient;
import com.Mockito.LearningMockito.client.PaymentClient;
import com.Mockito.LearningMockito.exception.PaymetFailedExecption;
import com.Mockito.LearningMockito.model.Order;
import com.Mockito.LearningMockito.model.OrderStatus;
import com.Mockito.LearningMockito.repository.OrderRespository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;

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

    // Test 4
    @Test
    void placeOrder_whenPaymentSuceeds_chargesThenSavesThenNotifies(){

        //ARRANGE
        String customerId = "cust-4";
        BigDecimal amount = new BigDecimal("900.0");

        when(paymentClient.charge(customerId,amount)).thenReturn(true);

        when(orderRespository.save(any(Order.class))).thenAnswer(invocation ->{
           Order o = invocation.getArgument(0);
           o.setId(20L);
           return o;
        });

        //ACT
        orderService.placeOrder(customerId,amount);

        //ASSERT (order of interactions across all three mocks)
        InOrder inOrder = inOrder(paymentClient,orderRespository,notificationClient);
        inOrder.verify(paymentClient).charge(customerId,amount);
        inOrder.verify(orderRespository).save(any(Order.class));
        inOrder.verify(notificationClient).sendConfirmation(customerId,20L);





    }

    //Test 5
    @Test
    void placeOrder_whenNotificationFails_stillReturnsConfirmedOrder(){

        // ARRANGE
        String customerId = "cust-5";
        BigDecimal amount = new BigDecimal("400.00");

        when(paymentClient.charge(customerId, amount)).thenReturn(true);

        when(orderRespository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order o = invocation.getArgument(0);
                    o.setId(30L);
                    return o;
                });

        doThrow(new RuntimeException("Notification service down"))
                .when(notificationClient).sendConfirmation(customerId, 30L);

        // ACT
        Order result = orderService.placeOrder(customerId, amount);

        // ASSERT
        assertThat(result.getId()).isEqualTo(30L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(notificationClient).sendConfirmation(customerId, 30L);
    }



}
