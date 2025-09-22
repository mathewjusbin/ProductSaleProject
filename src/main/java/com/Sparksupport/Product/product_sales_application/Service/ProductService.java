package com.sparksupport.product.product_sales_application.service;

import com.sparksupport.product.product_sales_application.dto.ProductDto;
import com.sparksupport.product.product_sales_application.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService  {

    Page<Product> getAllProducts(Pageable pageable); // pagination

    Product getProductById(Integer id);

    Product addProduct(ProductDto product);

    public Product updateProduct(Integer id, Product product);

    void deleteProduct(Integer id);

    Double getTotalRevenue();

    Double getRevenueByProduct(Integer productId);

}
