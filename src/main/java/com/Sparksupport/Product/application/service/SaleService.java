package com.sparksupport.product.application.service;

import com.sparksupport.product.application.dto.UpdateSaleDto;
import com.sparksupport.product.application.model.Sale;
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
