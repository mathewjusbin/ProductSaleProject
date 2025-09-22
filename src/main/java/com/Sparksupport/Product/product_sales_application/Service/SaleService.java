package com.Sparksupport.Product.product_sales_application.Service;

import com.Sparksupport.Product.product_sales_application.Dto.Sale;

public interface SaleService {

    Sale addSales(Integer productId, Sale sale);

    Boolean updateSales(Integer saleId, Sale sale);

    Boolean deleteSales(Integer saleId);

}
