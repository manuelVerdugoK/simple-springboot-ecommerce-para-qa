package com.vermann.ecommerce.service.impl;

import com.vermann.ecommerce.dto.request.CreateOrderRequest;
import com.vermann.ecommerce.dto.request.OrderItemRequest;
import com.vermann.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.vermann.ecommerce.dto.response.OrderResponse;
import com.vermann.ecommerce.exception.BusinessException;
import com.vermann.ecommerce.exception.ResourceNotFoundException;
import com.vermann.ecommerce.mapper.OrderMapper;
import com.vermann.ecommerce.model.*;
import com.vermann.ecommerce.repository.OrderRepository;
import com.vermann.ecommerce.repository.ProductRepository;
import com.vermann.ecommerce.repository.UserRepository;
import com.vermann.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return orderRepository.findByUserId(userId).stream().map(orderMapper::toResponse).toList();
    }

    @Override
    public OrderResponse create(CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndActiveTrue(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new BusinessException(
                        "Insufficient stock for product: " + product.getName(),
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            orderItems.add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(total)
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            savedOrder.getItems().add(item);
        }

        return orderMapper.toResponse(orderRepository.save(savedOrder));
    }

    @Override
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        OrderStatus current = order.getStatus();
        OrderStatus next = request.getStatus();

        validateTransition(current, next);

        if (next == OrderStatus.CONFIRMED) {
            decrementStock(order);
        }

        order.setStatus(next);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    public void cancel(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Order can only be cancelled when status is PENDING",
                    HttpStatus.BAD_REQUEST
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED, CANCELLED -> false;
        };
        if (!valid) {
            throw new BusinessException(
                    "Invalid status transition from " + current + " to " + next,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void decrementStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new BusinessException(
                        "Insufficient stock for product: " + product.getName(),
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
            product.setStock(newStock);
            productRepository.save(product);
        }
    }
}
