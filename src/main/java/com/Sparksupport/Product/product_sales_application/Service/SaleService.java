package com.sparksupport.product.product_sales_application.service;

import com.sparksupport.product.product_sales_application.dto.UpdateSaleDto;
import com.sparksupport.product.product_sales_application.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleService {

    Sale addSales(Integer productId, Sale sale);

    Sale updateSales(Integer saleId, UpdateSaleDto updateSaleDto);

    Boolean deleteSales(Integer saleId);

    // Methods for retrieving sales data
    Page<Sale> getSalesByProductId(Integer productId, Pageable pageable);

    Page<Sale> getAllSales(Pageable pageable);
}
