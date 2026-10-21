package com.mss301.inventory_service.repository;

import com.mss301.inventory_service.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
