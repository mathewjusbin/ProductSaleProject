package com.sparksupport.product.product_sales_application.repository;

import com.sparksupport.product.product_sales_application.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByNameAndIsDeletedFalse(String name);

    // Find all non-deleted products with pagination
    Page<Product> findByIsDeletedFalse(Pageable pageable);

    // Find non-deleted product by ID
    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    // Custom query to find all non-deleted products
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    Page<Product> findAllActiveProducts(Pageable pageable);

    // Find by ID including deleted products (for admin purposes)
    @Query("SELECT p FROM Product p WHERE p.Id = :id")
    Optional<Product> findByIdIncludingDeleted(@Param("id") Integer id);

    // Find if name exists for products other than the specified ID
    boolean existsByNameAndIsDeletedFalseAndIdNot(String name, Integer id);
}
