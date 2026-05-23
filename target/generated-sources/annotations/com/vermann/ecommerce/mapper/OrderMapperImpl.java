package com.vermann.ecommerce.mapper;

import com.vermann.ecommerce.dto.response.OrderItemResponse;
import com.vermann.ecommerce.dto.response.OrderResponse;
import com.vermann.ecommerce.model.Order;
import com.vermann.ecommerce.model.OrderItem;
import com.vermann.ecommerce.model.Product;
import com.vermann.ecommerce.model.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-23T10:27:57-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setUserId( orderUserId( order ) );
        orderResponse.setId( order.getId() );
        orderResponse.setStatus( order.getStatus() );
        orderResponse.setTotal( order.getTotal() );
        orderResponse.setItems( orderItemListToOrderItemResponseList( order.getItems() ) );
        orderResponse.setCreatedAt( order.getCreatedAt() );

        return orderResponse;
    }

    @Override
    public OrderItemResponse toItemResponse(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        OrderItemResponse orderItemResponse = new OrderItemResponse();

        orderItemResponse.setProductId( itemProductId( item ) );
        orderItemResponse.setProductName( itemProductName( item ) );
        orderItemResponse.setQuantity( item.getQuantity() );
        orderItemResponse.setUnitPrice( item.getUnitPrice() );

        return orderItemResponse;
    }

    private Long orderUserId(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<OrderItemResponse> orderItemListToOrderItemResponseList(List<OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderItem orderItem : list ) {
            list1.add( toItemResponse( orderItem ) );
        }

        return list1;
    }

    private Long itemProductId(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        Long id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String itemProductName(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
