package com.sparksupport.product.product_sales_application.service;

import com.sparksupport.product.product_sales_application.model.Sale;

public interface SaleService {

    Sale addSales(Integer productId, Sale sale);

    Boolean updateSales(Integer saleId, Sale sale);

    Boolean deleteSales(Integer saleId);

}
