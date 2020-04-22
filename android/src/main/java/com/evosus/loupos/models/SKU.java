package com.evosus.loupos.models;

import java.math.BigDecimal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SKU extends RealmObject {
     private String SKUType;
     private String MySKU;
     private String Description;
     private String UPC;
     @PrimaryKey
     private String ReadableID;
     private String SearchStringSKUDescription;
     private String PriceBy;
     private String PriceValuePercent;
     private String PriceValue;
     private String DisplayProfit;
     private Double SellPrice;
     private String DisplaySellPrice;
     private Double KitPriceTotal;
     private String DisplayCost;
     private Boolean Active;
     private Boolean Taxable;

     public String getSKUType() {
          return SKUType;
     }

     public void setSKUType(String SKUType) {
          this.SKUType = SKUType;
     }

     public String getMySKU() {
          return MySKU;
     }

     public void setMySKU(String mySKU) {
          MySKU = mySKU;
     }

     public String getDescription() {
          return Description;
     }

     public void setDescription(String description) {
          Description = description;
     }

     public String getUPC() {
          return UPC;
     }

     public void setUPC(String UPC) {
          this.UPC = UPC;
     }

     public String getReadableID() {
          return ReadableID;
     }

     public void setReadableID(String readableID) {
          ReadableID = readableID;
     }

     public String getSearchStringSKUDescription() {
          return SearchStringSKUDescription;
     }

     public void setSearchStringSKUDescription(String searchStringSKUDescription) {
          SearchStringSKUDescription = searchStringSKUDescription;
     }

     public String getPriceBy() {
          return PriceBy;
     }

     public void setPriceBy(String priceBy) {
          PriceBy = priceBy;
     }

     public String getPriceValuePercent() {
          return PriceValuePercent;
     }

     public void setPriceValuePercent(String priceValuePercent) {
          PriceValuePercent = priceValuePercent;
     }

     public String getPriceValue() {
          return PriceValue;
     }

     public void setPriceValue(String priceValue) {
          PriceValue = priceValue;
     }

     public String getDisplayProfit() {
          return DisplayProfit;
     }

     public void setDisplayProfit(String displayProfit) {
          DisplayProfit = displayProfit;
     }

     public Double getSellPrice() {
          return SellPrice;
     }

     public void setSellPrice(Double sellPrice) {
          SellPrice = sellPrice;
     }

     public String getDisplaySellPrice() {
          return DisplaySellPrice;
     }

     public void setDisplaySellPrice(String displaySellPrice) {
          DisplaySellPrice = displaySellPrice;
     }

     public  void setKitPriceTotal(Double kitPriceTotal) {
          KitPriceTotal = kitPriceTotal;
     }

     public Double getKitPriceTotal() {
          return  KitPriceTotal;
     }

     public String getDisplayCost() {
          return DisplayCost;
     }

     public void setDisplayCost(String displayCost) {
          DisplayCost = displayCost;
     }

     public Boolean getActive() {
          return Active;
     }

     public void setActive(Boolean active) {
          Active = active;
     }

     public Boolean getTaxable() {
          return Taxable;
     }

     public void setTaxable(Boolean taxable) {
          Taxable = taxable;
     }
}
