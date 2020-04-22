package com.evosus.loupos.models;

import java.math.BigDecimal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CustomerDisplay extends RealmObject {
     private String DropdownSearchString;
     private String DisplayName;
     private String PhoneNumber;
     private String ShipToAddress;
     @PrimaryKey
     private String CustomerVanityID;
     private Boolean Active;

     public String getDropdownSearchString() {
          return DropdownSearchString;
     }

     public void setDropdownSearchString(String dropdownSearchString) {
          DropdownSearchString = dropdownSearchString;
     }

     public String getDisplayName() {
          return DisplayName;
     }

     public void setDisplayName(String displayName) {
          DisplayName = displayName;
     }

     public String getPhoneNumber() {
          return PhoneNumber;
     }

     public void setPhoneNumber(String phoneNumber) {
          PhoneNumber = phoneNumber;
     }

     public String getShipToAddress() {
          return ShipToAddress;
     }

     public void setShipToAddress(String shipToAddress) {
          ShipToAddress = shipToAddress;
     }

     public String getCustomerVanityID() {
          return CustomerVanityID;
     }

     public void setCustomerVanityID(String customerVanityID) {
          CustomerVanityID = customerVanityID;
     }

     public Boolean getActive() {
          return Active;
     }

     public void setActive(Boolean active) {
          Active = active;
     }
}


