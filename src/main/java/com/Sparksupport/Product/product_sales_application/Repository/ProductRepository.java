package com.Sparksupport.Product.product_sales_application.Repository;

import com.Sparksupport.Product.product_sales_application.Dto.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByName(String name);

}
