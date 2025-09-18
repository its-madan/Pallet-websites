package com.saravanatimbers.palletbuilderbackend.repositories;

import com.saravanatimbers.palletbuilderbackend.models.UserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends MongoRepository<UserInfo, String> {
    Optional<UserInfo> findByUserId(String userId);
    boolean existsByUserId(String userId);
} 