package com.sparksupport.product.product_sales_application.serviceImpl;

import com.sparksupport.product.product_sales_application.dto.CreateProductDto;
import com.sparksupport.product.product_sales_application.exception.ProductNotFoundException;
import com.sparksupport.product.product_sales_application.model.Product;
import com.sparksupport.product.product_sales_application.repository.ProductRepository;
import com.sparksupport.product.product_sales_application.repository.SaleRepository;
import com.sparksupport.product.product_sales_application.service.ProductService;
import com.sparksupport.product.product_sales_application.util.ProductServiceUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        // Only return non-deleted products
        return productRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    public Product getProductById(Integer id) {
        // Only return non-deleted products
        return productRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new ProductNotFoundException(id)
        );
    }

    @Override
    public Product addProduct(CreateProductDto createProductDto) {
        // Convert CreateProductDto to Product entity using the utility method
        Product productEntity = ProductServiceUtil.convertToProduct(createProductDto);

        if (productRepository.existsByNameAndIsDeletedFalse(productEntity.getName())) {
            throw new IllegalArgumentException("Product with name '" + createProductDto.getName() + "' already exists.");
        }

        return productRepository.save(productEntity);
    }

    @Override
    public Product updateProduct(Integer id, Product product) {
        //first find by Id
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        //Next validation for find by name - optimized to single DB call
        if (product.getName() != null && !product.getName().isBlank()) {
            // Check if the name already exists for other products (excluding current product)
            // This replaces the previous two-step check with a single optimized query
            if (productRepository.existsByNameAndIsDeletedFalseAndIdNot(product.getName().trim(), id)) {
                throw new IllegalArgumentException("Product with name '" + product.getName() + "' already exists.");
            }
            existingProduct.setName(product.getName().trim());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            existingProduct.setDescription(product.getDescription().trim());
        }
        if (product.getQuantity() != null) {
            existingProduct.setQuantity(product.getQuantity());
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Check if product is already deleted
        if (product.getIsDeleted()) {
            throw new IllegalStateException("Product with ID " + id + " is already deleted");
        }

        // Soft delete: mark as deleted instead of removing from database
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Override
    public Double getTotalRevenue() {
        // Calculate total revenue from all non-deleted sales
        // Using sale price recorded at time of sale for accurate revenue calculation
        return saleRepository.findByIsDeletedFalse(org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .mapToDouble(sale -> {
                    BigDecimal salePrice = sale.getSalePrice() != null ? sale.getSalePrice() : BigDecimal.ZERO;
                    Integer quantity = sale.getQuantity() != null ? sale.getQuantity() : 0;
                    return salePrice.multiply(BigDecimal.valueOf(quantity)).doubleValue();
                })
                .sum();
    }

    @Override
    public Double getRevenueByProduct(Integer productId) {
        // Verify product exists and is not deleted
        Product existingProduct = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Get revenue only from non-deleted sales for this product
        return saleRepository.findByProductIdAndIsDeletedFalse(productId).stream()
                .mapToDouble(sale -> {
                    BigDecimal salePrice = sale.getSalePrice() != null ? sale.getSalePrice() : BigDecimal.ZERO;
                    Integer quantity = sale.getQuantity() != null ? sale.getQuantity() : 0;
                    return salePrice.multiply(BigDecimal.valueOf(quantity)).doubleValue();
                })
                .sum();
    }
}
