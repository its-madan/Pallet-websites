package com.saravanatimbers.palletbuilderbackend.controllers;

import com.saravanatimbers.palletbuilderbackend.models.AdminSettings;
import com.saravanatimbers.palletbuilderbackend.repositories.AdminSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/settings")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"}, allowCredentials = "true")
public class AdminSettingsController {
    @Autowired
    private AdminSettingsRepository adminSettingsRepository;

    // Fetch the single settings document (assume only one exists)
    @GetMapping
    public ResponseEntity<AdminSettings> getSettings() {
        Optional<AdminSettings> settings = adminSettingsRepository.findAll().stream().findFirst();
        if (settings.isPresent()) {
            return ResponseEntity.ok(settings.get());
        } else {
            // Return a default settings object if none exists
            AdminSettings defaultSettings = new AdminSettings();
            defaultSettings.setBasePalletCost(Map.of(
                "standard", 800.0,
                "euro", 950.0,
                "custom", 1200.0,
                "heavy-duty", 1500.0
            ));
            defaultSettings.setMinimumOrderQuantity(1);
            defaultSettings.setPriceIncreasePercentBelowMinimum(10.0);
            defaultSettings.setMaterialSurcharge(Map.of(
                "pine", 100.0,
                "ply", 120.0,
                "birch", 150.0,
                "recycled", 70.0
            ));
            defaultSettings.setUrgencyFee(Map.of(
                "express", 1000.0,
                "urgent", 500.0
            ));
            defaultSettings.setShippingEstimate(250.0);
            defaultSettings.setCgstPercent(9.0);
            defaultSettings.setSgstPercent(9.0);
            defaultSettings.setPaymentTermsNotes("50% advance payment. Balance on delivery. Subject to standard terms and conditions.");
            return ResponseEntity.ok(defaultSettings);
        }
    }

    // Update or create the settings document
    @PutMapping
    public ResponseEntity<AdminSettings> updateSettings(@RequestBody AdminSettings newSettings) {
        // If an ID is provided, update; else, upsert as the only settings doc
        if (newSettings.getId() == null || newSettings.getId().isEmpty()) {
            // Try to find existing
            Optional<AdminSettings> existing = adminSettingsRepository.findAll().stream().findFirst();
            existing.ifPresent(s -> newSettings.setId(s.getId()));
        }
        AdminSettings saved = adminSettingsRepository.save(newSettings);
        return ResponseEntity.ok(saved);
    }
} 