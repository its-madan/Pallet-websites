package com.saravanatimbers.palletbuilderbackend.repositories;

import com.saravanatimbers.palletbuilderbackend.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    // Add custom query methods if needed
    List<Order> findByUserId(String userId);
    void deleteByUserId(String userId);
} 