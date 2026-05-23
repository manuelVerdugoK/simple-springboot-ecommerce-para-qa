package com.vermann.ecommerce.service;

import com.vermann.ecommerce.dto.request.ProductRequest;
import com.vermann.ecommerce.dto.request.UpdateStockRequest;
import com.vermann.ecommerce.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> findAll(String category);

    ProductResponse findById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse updateStock(Long id, UpdateStockRequest request);

    void delete(Long id);
}
