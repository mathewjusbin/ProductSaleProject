package com.sparksupport.product.product_sales_application.repository;


import com.sparksupport.product.product_sales_application.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {
    
    List<Sale> findByProductId(Integer productId);

    // Add paginated version for better performance with large datasets
    Page<Sale> findByProductId(Integer productId, Pageable pageable);

    // Methods to exclude deleted sales (soft delete support)
    Optional<Sale> findByIdAndIsDeletedFalse(Integer id);

    Page<Sale> findByIsDeletedFalse(Pageable pageable);

    Page<Sale> findByProductIdAndIsDeletedFalse(Integer productId, Pageable pageable);

    List<Sale> findByProductIdAndIsDeletedFalse(Integer productId);

    // Find by ID including deleted sales (for admin purposes if needed)
    @Query("SELECT s FROM Sale s WHERE s.Id = :id")
    Optional<Sale> findByIdIncludingDeleted(@Param("id") Integer id);
}
