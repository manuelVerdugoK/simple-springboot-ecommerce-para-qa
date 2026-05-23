package com.vermann.ecommerce.service.impl;

import com.vermann.ecommerce.dto.request.ProductRequest;
import com.vermann.ecommerce.dto.request.UpdateStockRequest;
import com.vermann.ecommerce.dto.response.ProductResponse;
import com.vermann.ecommerce.exception.ResourceNotFoundException;
import com.vermann.ecommerce.mapper.ProductMapper;
import com.vermann.ecommerce.model.Product;
import com.vermann.ecommerce.repository.ProductRepository;
import com.vermann.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(String category) {
        List<Product> products = (category != null && !category.isBlank())
                ? productRepository.findByCategoryAndActiveTrue(category)
                : productRepository.findByActiveTrue();
        return products.stream().map(productMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        product.setActive(true);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        productMapper.updateEntity(request, product);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateStock(Long id, UpdateStockRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setStock(request.getStock());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
    }
}
