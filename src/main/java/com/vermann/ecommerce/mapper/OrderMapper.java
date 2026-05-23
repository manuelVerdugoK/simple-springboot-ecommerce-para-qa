package com.vermann.ecommerce.mapper;

import com.vermann.ecommerce.dto.response.OrderItemResponse;
import com.vermann.ecommerce.dto.response.OrderResponse;
import com.vermann.ecommerce.model.Order;
import com.vermann.ecommerce.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    OrderResponse toResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponse toItemResponse(OrderItem item);
}
