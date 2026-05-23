package com.vermann.ecommerce.service;

import com.vermann.ecommerce.dto.request.CreateOrderRequest;
import com.vermann.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.vermann.ecommerce.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> findAll();

    OrderResponse findById(Long id);

    List<OrderResponse> findByUserId(Long userId);

    OrderResponse create(CreateOrderRequest request);

    OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request);

    void cancel(Long id);
}
