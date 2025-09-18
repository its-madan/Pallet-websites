package com.saravanatimbers.palletbuilderbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Document(collection = "admin_settings")
public class AdminSettings {
    @Id
    private String id;

    // Base cost per pallet type, e.g. { "euro": 1000, "block": 1200 }
    private Map<String, Double> basePalletCost;
    private int minimumOrderQuantity;
    private double priceIncreasePercentBelowMinimum;
    // Material surcharge per material type, e.g. { "wood": 100, "plastic": 200 }
    private Map<String, Double> materialSurcharge;
    // Urgency fee per urgency level, e.g. { "normal": 0, "urgent": 500 }
    private Map<String, Double> urgencyFee;
    // Shipping estimate (per order)
    private double shippingEstimate;
    private double cgstPercent;
    private double sgstPercent;
    private String paymentTermsNotes;
    private double vipDiscountPercent = 15.0;
    private double bulkOrdersDiscountPercent = 12.0;
    private double specialPricingDiscountPercent = 10.0;
    private double frequentBuyerDiscountPercent = 7.0;

    public AdminSettings() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Map<String, Double> getBasePalletCost() { return basePalletCost; }
    public void setBasePalletCost(Map<String, Double> basePalletCost) { this.basePalletCost = basePalletCost; }

    public int getMinimumOrderQuantity() { return minimumOrderQuantity; }
    public void setMinimumOrderQuantity(int minimumOrderQuantity) { this.minimumOrderQuantity = minimumOrderQuantity; }

    public double getPriceIncreasePercentBelowMinimum() { return priceIncreasePercentBelowMinimum; }
    public void setPriceIncreasePercentBelowMinimum(double priceIncreasePercentBelowMinimum) { this.priceIncreasePercentBelowMinimum = priceIncreasePercentBelowMinimum; }

    public Map<String, Double> getMaterialSurcharge() { return materialSurcharge; }
    public void setMaterialSurcharge(Map<String, Double> materialSurcharge) { this.materialSurcharge = materialSurcharge; }

    public Map<String, Double> getUrgencyFee() { return urgencyFee; }
    public void setUrgencyFee(Map<String, Double> urgencyFee) { this.urgencyFee = urgencyFee; }

    public double getShippingEstimate() { return shippingEstimate; }
    public void setShippingEstimate(double shippingEstimate) { this.shippingEstimate = shippingEstimate; }

    public double getCgstPercent() { return cgstPercent; }
    public void setCgstPercent(double cgstPercent) { this.cgstPercent = cgstPercent; }

    public double getSgstPercent() { return sgstPercent; }
    public void setSgstPercent(double sgstPercent) { this.sgstPercent = sgstPercent; }

    public String getPaymentTermsNotes() { return paymentTermsNotes; }
    public void setPaymentTermsNotes(String paymentTermsNotes) { this.paymentTermsNotes = paymentTermsNotes; }

    public double getVipDiscountPercent() { return vipDiscountPercent; }
    public void setVipDiscountPercent(double v) { this.vipDiscountPercent = v; }
    public double getBulkOrdersDiscountPercent() { return bulkOrdersDiscountPercent; }
    public void setBulkOrdersDiscountPercent(double v) { this.bulkOrdersDiscountPercent = v; }
    public double getSpecialPricingDiscountPercent() { return specialPricingDiscountPercent; }
    public void setSpecialPricingDiscountPercent(double v) { this.specialPricingDiscountPercent = v; }
    public double getFrequentBuyerDiscountPercent() { return frequentBuyerDiscountPercent; }
    public void setFrequentBuyerDiscountPercent(double v) { this.frequentBuyerDiscountPercent = v; }
} 