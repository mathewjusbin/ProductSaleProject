package com.Sparksupport.Product.product_sales_application.Repository;


import com.Sparksupport.Product.product_sales_application.Dto.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {

}
