package com.sparksupport.product.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAspectJAutoProxy
public class ProductSalesApllication {

	public static void main(String[] args) {
		SpringApplication.run(ProductSalesApllication.class, args);
	}

}
