package com.evosus.loupos.models;

import java.math.BigDecimal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SKUKitLine extends RealmObject {
    @PrimaryKey
    private String ID_;
    private String MySKU;
    private String Description;
    private String SKUType;
    private Double Quantity;
    private String KitPriceLineEnum;
    private String UnitMeasureName;
    private Double ConversionFactor;
    private Double PriceCalculated;
    private Double SubtotalCalculated;
    private Integer Order;
    private Boolean isUpdatedSubtotal;
    private Double RetailPrice;
    private Double RetailMinusPercent;
    private Double FixedPrice;
    private Boolean isRetailMinusPercent;
    private Boolean isFixedPrice;
    private Boolean isNoCharge;
    private String SKUID;
    private String KitSKUID;
    private String EvosusCompanySN;

    public String get_ID() {
        return ID_;
    }

    public void setID_(String ID_) {
        this.ID_ = ID_;
    }

    public String getMySKU() {
        return MySKU;
    }

    public void setMySKU(String MySKU) {
        this.MySKU = MySKU;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getSKUType() {
        return SKUType;
    }

    public void setSKUType(String SKUType) {
        this.SKUType = SKUType;
    }

    public Double getQuantity() {
        return Quantity;
    }

    public void setQuantity(Double Quantity) {
        this.Quantity = Quantity;
    }

    public String getKitPriceLineEnum() {
        return KitPriceLineEnum;
    }

    public void setKitPriceLineEnum(String KitPriceLineEnum) {
        this.KitPriceLineEnum = KitPriceLineEnum;
    }

    public String getUnitMeasureName() {
        return UnitMeasureName;
    }

    public void setUnitMeasureName(String UnitMeasureName) {
        this.UnitMeasureName = UnitMeasureName;
    }

    public Double getConversionFactor() {
        return ConversionFactor;
    }

    public void setConversionFactor(Double ConversionFactor) {
        this.ConversionFactor = ConversionFactor;
    }

    public Double getPriceCalculated() {
        return PriceCalculated;
    }

    public void setPriceCalculated(Double PriceCalculated) {
        this.PriceCalculated = PriceCalculated;
    }

    public Double getSubtotalCalculated() {
        return SubtotalCalculated;
    }

    public void setSubtotalCalculated(Double SubtotalCalculated) {
        this.SubtotalCalculated = SubtotalCalculated;
    }

    public Integer getOrder() {
        return Order;
    }

    public void setOrder(Integer Order) {
        this.Order = Order;
    }

    public Boolean getisUpdatedSubtotal() {
        return isUpdatedSubtotal;
    }

    public void setisUpdatedSubtotal(Boolean isUpdatedSubtotal) {
        this.isUpdatedSubtotal = isUpdatedSubtotal;
    }

    public Double getRetailPrice() {
        return RetailPrice;
    }

    public void setRetailPrice(Double RetailPrice) {
        this.RetailPrice = RetailPrice;
    }

    public Double getRetailMinusPercent() {
        return RetailMinusPercent;
    }

    public void setRetailMinusPercent(Double RetailMinusPercent) {
        this.RetailMinusPercent = RetailMinusPercent;
    }

    public Double getFixedPrice() {
        return FixedPrice;
    }

    public void setFixedPrice(Double FixedPrice) {
        this.FixedPrice = FixedPrice;
    }

    public Boolean getisRetailMinusPercent() {
        return isRetailMinusPercent;
    }

    public void setisRetailMinusPercent(Boolean isRetailMinusPercent) {
        this.isRetailMinusPercent = isRetailMinusPercent;
    }

    public Boolean getisFixedPrice() {
        return isFixedPrice;
    }

    public void setisFixedPrice(Boolean isFixedPrice) {
        this.isFixedPrice = isFixedPrice;
    }

    public Boolean getisNoCharge() {
        return isNoCharge;
    }

    public void setisNoCharge(Boolean isNoCharge) {
        this.isNoCharge = isNoCharge;
    }

    public String getSKUID() {
        return SKUID;
    }

    public void setSKUID(String SKUID) {
        this.SKUID = SKUID;
    }

    public String getKitSKUID() {
        return KitSKUID;
    }

    public void setKitSKUID(String KitSKUID) {
        this.KitSKUID = KitSKUID;
    }

    public String getEvosusCompanySN() {
        return EvosusCompanySN;
    }
    
    public void setEvosusCompanySN(String EvosusCompanySN) {
        this.EvosusCompanySN = EvosusCompanySN;
    }
}