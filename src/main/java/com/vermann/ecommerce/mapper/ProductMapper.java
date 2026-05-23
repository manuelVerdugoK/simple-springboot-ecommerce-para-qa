package com.vermann.ecommerce.mapper;

import com.vermann.ecommerce.dto.request.ProductRequest;
import com.vermann.ecommerce.dto.response.ProductResponse;
import com.vermann.ecommerce.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    Product toEntity(ProductRequest request);

    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
