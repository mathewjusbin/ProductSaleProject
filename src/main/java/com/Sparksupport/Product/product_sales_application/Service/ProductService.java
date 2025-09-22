package com.Sparksupport.Product.product_sales_application.Service;

import com.Sparksupport.Product.product_sales_application.Dto.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {

    Page<Product> getAllProducts(Pageable pageable); // pagination

    Product getProductById(Integer id);

    Product addProduct(Product product);

    Product updateProduct(Integer id, Product product);

    void deleteProduct(Integer id);

    Double getTotalRevenue();

    Double getRevenueByProduct(Integer productId);

}
