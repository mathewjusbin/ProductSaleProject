package com.sparksupport.product.application.service;

import com.sparksupport.product.application.dto.CreateProductDto;
import com.sparksupport.product.application.dto.UpdateProductDto;
import com.sparksupport.product.application.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService  {

    Page<Product> getAllProducts(Pageable pageable); // pagination

    Product getProductById(Integer id);

    Product addProduct(CreateProductDto createProductDto);

    Product updateProduct(Integer id, UpdateProductDto updateProductDto);

    void deleteProduct(Integer id);

    Double getTotalRevenue();

    Double getRevenueByProduct(Integer productId);

}
