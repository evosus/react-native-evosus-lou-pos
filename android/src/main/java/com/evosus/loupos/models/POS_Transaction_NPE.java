package com.evosus.loupos.models;

import java.math.BigDecimal;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POS_Transaction_NPE extends RealmObject {
    @PrimaryKey
    private String ID_;
    private Double Subtotal;
    private String SubtotalDisplay;
    private Double Total;
    private String TotalDisplay;
    private String ReturnMode;
    private String SKU_Search;
    private String ReceiptCompanyName;
    private String ReceiptCustomMessage;
    private String TrxStatus;
    private String PaymentMethod;
    private Double Tax;
    private String TaxDisplay;
    private Double Tendered;
    private String TenderedDisplay;
    private Double ChangeDue;
    private String ChangeDueDisplay;
    private Boolean PrintReceipt;
    private Boolean EmailReceipt;
    private Integer PaymentAttempts;
    private Double DiscountRate;
    private String DiscountType;
    private Double SubtotalAfterDiscount;
    private String SubtotalAfterDiscount_Display;
    private String SubtotalBeforeDiscount_Display;
    private Double Discount_Total;
    private Double SubtotalBeforeDiscount;
    private String TaxExemptDocumentation;
    private Boolean Invoiced;
    private String OrderType;
    private String Request;
    private Integer NumberOfLineItems;
    private String ClerkName;
    private String InvoiceID;
    private Boolean IsProcessed;
    private Integer ActiveListenersCount;
    private String CardType;
    private String CustomerName;
    private String CustomerVanityID;
    private Double TaxableTotal;
    private Integer TaxCodeID;
    private Date HoldDate;
    private Boolean HasError;
    private Boolean Synced;
    private String POSStationSessionID;
    private Integer DepartmentID;
    private Integer POSStationID;
    private String EvosusCompanySN;
    private String DepartmentName;

    public String getID_() {
        return ID_;
    }

    public void setID_(String iD_) {
        ID_ = iD_;
    }

    public Double getSubtotal() {
        return Subtotal;
    }

    public void setSubtotal(Double subtotal) {
        Subtotal = subtotal;
    }

    public String getSubtotalDisplay() {
        return SubtotalDisplay;
    }

    public void setSubtotalDisplay(String subtotalDisplay) {
        SubtotalDisplay = subtotalDisplay;
    }

    public Double getTotal() {
        return Total;
    }

    public void setTotal(Double total) {
        Total = total;
    }

    public String getTotalDisplay() {
        return TotalDisplay;
    }

    public void setTotalDisplay(String totalDisplay) {
        TotalDisplay = totalDisplay;
    }

    public String getReturnMode() {
        return ReturnMode;
    }

    public void setReturnMode(String returnMode) {
        ReturnMode = returnMode;
    }

    public String getSKU_Search() {
        return SKU_Search;
    }

    public void setSKU_Search(String sku_Search) {
        SKU_Search = sku_Search;
    }

    public String getReceiptCompanyName() {
        return ReceiptCompanyName;
    }

    public void setReceiptCompanyName(String receiptCompanyName) {
        ReceiptCompanyName = receiptCompanyName;
    }

    public String getReceiptCustomMessage() {
        return ReceiptCustomMessage;
    }

    public void setReceiptCustomMessage(String receiptCustomMessage) {
        ReceiptCustomMessage = receiptCustomMessage;
    }

    public String getTrxStatus() {
        return TrxStatus;
    }

    public void setTrxStatus(String trxStatus) {
        TrxStatus = trxStatus;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        PaymentMethod = paymentMethod;
    }

    public Double getTax() {
        return Tax;
    }

    public void setTax(Double tax) {
        Tax = tax;
    }

    public String getTaxDisplay() {
        return TaxDisplay;
    }

    public void setTaxDisplay(String taxDisplay) {
        TaxDisplay = taxDisplay;
    }

    public Double getTendered() {
        return Tendered;
    }

    public void setTendered(Double tendered) {
        Tendered = tendered;
    }

    public String getTenderedDisplay() {
        return TenderedDisplay;
    }

    public void setTenderedDisplay(String tenderedDisplay) {
        TenderedDisplay = tenderedDisplay;
    }

    public Double getChangeDue() {
        return ChangeDue;
    }

    public void setChangeDue(Double changeDue) {
        ChangeDue = changeDue;
    }

    public String getChangeDueDisplay() {
        return ChangeDueDisplay;
    }

    public void setChangeDueDisplay(String changeDueDisplay) {
        ChangeDueDisplay = changeDueDisplay;
    }

    public Boolean getPrintReceipt() {
        return PrintReceipt;
    }

    public void setPrintReceipt(Boolean printReceipt) {
        PrintReceipt = printReceipt;
    }

    public Boolean getEmailReceipt() {
        return EmailReceipt;
    }

    public void setEmailReceipt(Boolean emailReceipt) {
        EmailReceipt = emailReceipt;
    }

    public Integer getPaymentAttempts() {
        return PaymentAttempts;
    }

    public void setPaymentAttempts(Integer paymentAttempts) {
        PaymentAttempts = paymentAttempts;
    }

    public Double getDiscountRate() {
        return DiscountRate;
    }

    public void setDiscountRate(Double discountRate) {
        DiscountRate = discountRate;
    }

    public String getDiscountType() {
        return DiscountType;
    }

    public void setDiscountType(String discountType) {
        DiscountType = discountType;
    }

    public Double getSubtotalAfterDiscount() {
        return SubtotalAfterDiscount;
    }

    public void setSubtotalAfterDiscount(Double subtotalAfterDiscount) {
        SubtotalAfterDiscount = subtotalAfterDiscount;
    }

    public String getSubtotalAfterDiscount_Display() {
        return SubtotalAfterDiscount_Display;
    }

    public void setSubtotalAfterDiscount_Display(String subtotalAfterDiscount_Display) {
        SubtotalAfterDiscount_Display = subtotalAfterDiscount_Display;
    }

    public String getSubtotalBeforeDiscount_Display() {
        return SubtotalBeforeDiscount_Display;
    }

    public void setSubtotalBeforeDiscount_Display(String subtotalBeforeDiscount_Display) {
        SubtotalBeforeDiscount_Display = subtotalBeforeDiscount_Display;
    }

    public Double getDiscount_Total() {
        return Discount_Total;
    }

    public void setDiscount_Total(Double discount_Total) {
        Discount_Total = discount_Total;
    }

    public Double getSubtotalBeforeDiscount() {
        return SubtotalBeforeDiscount;
    }

    public void setSubtotalBeforeDiscount(Double subtotalBeforeDiscount) {
        SubtotalBeforeDiscount = subtotalBeforeDiscount;
    }

    public String getTaxExemptDocumentation() {
        return TaxExemptDocumentation;
    }

    public void setTaxExemptDocumentation(String taxExemptDocumentation) {
        TaxExemptDocumentation = taxExemptDocumentation;
    }

    public Boolean getInvoiced() {
        return Invoiced;
    }

    public void setInvoiced(Boolean invoiced) {
        Invoiced = invoiced;
    }

    public String getOrderType() {
        return OrderType;
    }

    public void setOrderType(String orderType) {
        OrderType = orderType;
    }

    public String getRequest() {
        return Request;
    }

    public void setRequest(String request) {
        Request = request;
    }

    public Integer getNumberOfLineItems() {
        return NumberOfLineItems;
    }

    public void setNumberOfLineItems(Integer numberOfLineItems) {
        NumberOfLineItems = numberOfLineItems;
    }

    public String getClerkName() {
        return ClerkName;
    }

    public void setClerkName(String clerkName) {
        ClerkName = clerkName;
    }

    public String getInvoiceID() {
        return InvoiceID;
    }

    public void setInvoiceID(String invoiceID) {
        InvoiceID = invoiceID;
    }

    public Boolean getIsProcessed() {
        return IsProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        IsProcessed = isProcessed;
    }

    public Integer getActiveListenersCount() {
        return ActiveListenersCount;
    }

    public void setActiveListenersCount(Integer activeListenersCount) {
        ActiveListenersCount = activeListenersCount;
    }

    public String getCardType() {
        return CardType;
    }

    public void setCardType(String cardType) {
        CardType = cardType;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getCustomerVanityID() {
        return CustomerVanityID;
    }

    public void setCustomerVanityID(String customerVanityID) {
        CustomerVanityID = customerVanityID;
    }

    public Double getTaxableTotal() {
        return TaxableTotal;
    }

    public void setTaxableTotal(Double taxableTotal) {
        TaxableTotal = taxableTotal;
    }

    public Integer getTaxCodeID() {
        return TaxCodeID;
    }

    public void setTaxCodeID(Integer taxCodeID) {
        TaxCodeID = taxCodeID;
    }

    public Date getHoldDate() {
        return HoldDate;
    }

    public void setHoldDate(Date holdDate) {
        HoldDate = holdDate;
    }

    public Boolean getHasError() {
        return HasError;
    }

    public void setHasError(Boolean hasError) {
        HasError = hasError;
    }

    public Boolean getSynced() {
        return Synced;
    }

    public void setSynced(Boolean synced) {
        Synced = synced;
    }

    public String getPOSStationSessionID() {
        return POSStationSessionID;
    }
    
    public void setPOSStationSessionID(String POSStationSessionID) {
        this.POSStationSessionID =  POSStationSessionID;
    }

    public Integer getDepartmentID() {
        return DepartmentID;
    }
    
    public void setDepartmentID(Integer DepartmentID) {
        this.DepartmentID =  DepartmentID;
    }

    public Integer getPOSStationID() {
        return POSStationID;
    }
    
    public void setPOSStationID(Integer POSStationID) {
        this.POSStationID =  POSStationID;
    }

    public String getEvosusCompanySN() {
        return EvosusCompanySN;
    }
    
    public void setEvosusCompanySN(String EvosusCompanySN) {
        this.EvosusCompanySN = EvosusCompanySN;
    }

    public String getDepartmentName() {
        return DepartmentName;
    }
    
    public void setDepartmentName(String DepartmentName) {
        this.DepartmentName = DepartmentName;
    }
}