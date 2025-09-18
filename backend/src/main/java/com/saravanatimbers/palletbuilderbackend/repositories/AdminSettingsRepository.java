package com.saravanatimbers.palletbuilderbackend.repositories;

import com.saravanatimbers.palletbuilderbackend.models.AdminSettings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminSettingsRepository extends MongoRepository<AdminSettings, String> {
} 