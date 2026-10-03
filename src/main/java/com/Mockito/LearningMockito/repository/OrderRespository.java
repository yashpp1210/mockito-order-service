package com.Mockito.LearningMockito.repository;

import com.Mockito.LearningMockito.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRespository extends JpaRepository<Order,Long> {
}
