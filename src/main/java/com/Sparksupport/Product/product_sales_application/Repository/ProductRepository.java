package com.sparksupport.product.product_sales_application.repository;

import com.sparksupport.product.product_sales_application.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByNameAndIsDeletedFalse(String name);

}
