package com.sparksupport.product.product_sales_application.repository;


import com.sparksupport.product.product_sales_application.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {
    
    List<Sale> findByProductId(Integer productId);

}
