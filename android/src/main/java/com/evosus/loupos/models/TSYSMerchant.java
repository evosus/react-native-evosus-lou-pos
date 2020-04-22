package com.evosus.loupos.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TSYSMerchant extends RealmObject {
     @PrimaryKey
     private long _ID;
     private String Name;
     private String MerchantId;
     private String DeviceId;
     private String MultiPassDeviceID;
     private String TransactionKey;
     private Boolean IsTest;
     private String UserId;
     private String Password;
     private Integer OrderPrefix;
     private String PhoneNumber;
     private Date TestedDate;
     private String TestedBy;
     private Boolean ShowSaleActivity;
     private Boolean ShowVoidActivity;
     private Boolean Active;

     public long get_ID() {
          return _ID;
     }

     public void set_ID(long _ID) {
          this._ID = _ID;
     }

     public String getName() {
          return Name;
     }

     public void setName(String name) {
          Name = name;
     }

     public String getMerchantId() {
          return MerchantId;
     }

     public void setMerchantId(String merchantId) {
          MerchantId = merchantId;
     }

     public String getDeviceId() {
          return DeviceId;
     }

     public void setDeviceId(String deviceId) {
          DeviceId = deviceId;
     }

     public String getMultiPassDeviceID() {
          return MultiPassDeviceID;
     }

     public void setMultiPassDeviceID(String multiPassDeviceID) {
          MultiPassDeviceID = multiPassDeviceID;
     }

     public String getTransactionKey() {
          return TransactionKey;
     }

     public void setTransactionKey(String transactionKey) {
          TransactionKey = transactionKey;
     }

     public Boolean getTest() {
          return IsTest;
     }

     public void setTest(Boolean test) {
          IsTest = test;
     }

     public String getUserId() {
          return UserId;
     }

     public void setUserId(String userId) {
          UserId = userId;
     }

     public String getPassword() {
          return Password;
     }

     public void setPassword(String password) {
          Password = password;
     }

     public Integer getOrderPrefix() {
          return OrderPrefix;
     }

     public void setOrderPrefix(Integer orderPrefix) {
          OrderPrefix = orderPrefix;
     }

     public String getPhoneNumber() {
          return PhoneNumber;
     }

     public void setPhoneNumber(String phoneNumber) {
          PhoneNumber = phoneNumber;
     }

     public Date getTestedDate() {
          return TestedDate;
     }

     public void setTestedDate(Date testedDate) {
          TestedDate = testedDate;
     }

     public String getTestedBy() {
          return TestedBy;
     }

     public void setTestedBy(String testedBy) {
          TestedBy = testedBy;
     }

     public Boolean getShowSaleActivity() {
          return ShowSaleActivity;
     }

     public void setShowSaleActivity(Boolean showSaleActivity) {
          ShowSaleActivity = showSaleActivity;
     }

     public Boolean getShowVoidActivity() {
          return ShowVoidActivity;
     }

     public void setShowVoidActivity(Boolean showVoidActivity) {
          ShowVoidActivity = showVoidActivity;
     }

     public Boolean getActive() {
          return Active;
     }

     public void setActive(Boolean active) {
          Active = active;
     }

}
