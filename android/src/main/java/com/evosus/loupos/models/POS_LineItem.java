package com.evosus.loupos.models;

import java.math.BigDecimal;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POS_LineItem extends RealmObject {

    @PrimaryKey
    private String ID_;
    private String POS_TransactionID;
    private Integer LineNumber;
    private String MySKU;
    private String Description;
    private String DescriptionFull;
    private String SKUType;
    private Double UnitPrice;
    private String UnitPriceDisplay;
    private Double Quantity;
    private Double Discount;
    private String DiscountDisplay;
    private Double Subtotal;
    private String SubtotalDisplay;
    private Boolean isComment;
    private String Comment;
    private Date ServiceDate;
    private String Status;
    private String CustomerVanityID;
    private Date DateCompleted;
    private String SKUID;
    private String SerialNumber;
    private Boolean Taxable;

    public String getID_() {
        return ID_;
    }
    
    public void setID_(String ID_) {
        this.ID_ =  ID_;
    }

    public String getPOS_TransactionID() {
        return POS_TransactionID;
    }
    
    public void setPOS_TransactionID(String POS_TransactionID) {
        this.POS_TransactionID =  POS_TransactionID;
    }

    public Integer getLineNumber() {
        return LineNumber;
    }
    
    public void setLineNumber(Integer LineNumber) {
        this.LineNumber =  LineNumber;
    }

    public String getMySKU() {
        return MySKU;
    }
    
    public void setMySKU(String MySKU) {
        this.MySKU =  MySKU;
    }

    public String getDescription() {
        return Description;
    }
    
    public void setDescription(String Description) {
        this.Description =  Description;
    }

    public String getDescriptionFull() {
        return DescriptionFull;
    }
    
    public void setDescriptionFull(String DescriptionFull) {
        this.DescriptionFull =  DescriptionFull;
    }

    public String getSKUType() {
        return SKUType;
    }
    
    public void setSKUType(String SKUType) {
        this.SKUType =  SKUType;
    }

    public Double getUnitPrice() {
        return UnitPrice;
    }
    
    public void setUnitPrice(Double UnitPrice) {
        this.UnitPrice =  UnitPrice;
    }

    public String getUnitPriceDisplay() {
        return UnitPriceDisplay;
    }
    
    public void setUnitPriceDisplay(String UnitPriceDisplay) {
        this.UnitPriceDisplay =  UnitPriceDisplay;
    }


    public Double getQuantity() {
        return Quantity;
    }
    
    public void setQuantity(Double Quantity) {
        this.Quantity =  Quantity;
    }

    public Double getDiscount() {
        return Discount;
    }
    
    public void setDiscount(Double Discount) {
        this.Discount =  Discount;
    }

    public String getDiscountDisplay() {
        return DiscountDisplay;
    }
    
    public void setDiscountDisplay(String DiscountDisplay) {
        this.DiscountDisplay =  DiscountDisplay;
    }

    public Double getSubtotal() {
        return Subtotal;
    }
    
    public void setSubtotal(Double Subtotal) {
        this.Subtotal =  Subtotal;
    }

    public String getSubtotalDisplay() {
        return SubtotalDisplay;
    }
    
    public void setSubtotalDisplay(String SubtotalDisplay) {
        this.SubtotalDisplay =  SubtotalDisplay;
    }

    public Boolean getisComment() {
        return isComment;
    }
    
    public void setisComment(Boolean isComment) {
        this.isComment =  isComment;
    }

    public String getComment() {
        return Comment;
    }
    
    public void setComment(String Comment) {
        this.Comment =  Comment;
    }

    public Date getServiceDate() {
        return ServiceDate;
    }
    
    public void setServiceDate(Date ServiceDate) {
        this.ServiceDate =  ServiceDate;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status =  Status;
    }

    public String getCustomerVanityID() {
        return CustomerVanityID;
    }
    
    public void setCustomerVanityID(String CustomerVanityID) {
        this.CustomerVanityID =  CustomerVanityID;
    }

    public Date getDateCompleted() {
        return DateCompleted;
    }
    
    public void setDateCompleted(Date DateCompleted) {
        this.DateCompleted =  DateCompleted;
    }

    public String getSKUID() {
        return SKUID;
    }
    
    public void setSKUID(String SKUID) {
        this.SKUID =  SKUID;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }
    
    public void setSerialNumber(String SerialNumber) {
        this.SerialNumber =  SerialNumber;
    }

    public Boolean getTaxable() {
        return Taxable;
    }

    public void setTaxable(Boolean Taxable) {
        this.Taxable =  Taxable;
    }
}
