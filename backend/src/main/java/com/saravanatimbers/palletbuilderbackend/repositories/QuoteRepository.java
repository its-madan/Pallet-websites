package com.saravanatimbers.palletbuilderbackend.repositories;

import com.saravanatimbers.palletbuilderbackend.models.Quote;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
 
public interface QuoteRepository extends MongoRepository<Quote, String> {
    List<Quote> findByUserId(String userId);
    List<Quote> findByStatus(String status);
    void deleteByUserId(String userId);
} 