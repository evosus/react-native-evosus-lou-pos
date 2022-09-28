package com.evosus.loupos.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POSStationSession extends RealmObject {
    @PrimaryKey
    private String _ID;
    private String OpenerName;
    private Date OpenDateTime;
    private Double OpenCashAmount;
    private Double OpenOverUnderAmount;
    private String CloserName;
    private Date CloseDateTime;
    private Double CloseCashExpectedAmount;
    private Double CloseCashAmount;
    private Double CloseOverUnderAmount;
    private Double CashLeftInDrawerAmount;
    private String CashAvailableToDepositAmount;
    private Boolean CloseOverUnderMessaged;
    private Boolean OpenOverUnderMessaged;
    private String GUIDString;
    private String LastPOSTransactionID;

    public String get_ID() {
        return _ID;
    }

    public void set_ID(String _ID) {
        this._ID = _ID;
    }

    public String getOpenerName() {
        return OpenerName;
    }

    public void setOpenerName(String OpenerName) {
        this.OpenerName = OpenerName;
    }

    public Date getOpenDateTime() {
        return OpenDateTime;
    }

    public void setOpenDateTime(Date OpenDateTime) {
        this.OpenDateTime = OpenDateTime;
    }

    public Double getOpenCashAmount() {
        return OpenCashAmount;
    }

    public void setOpenCashAmount(Double OpenCashAmount) {
        this.OpenCashAmount = OpenCashAmount;
    }

    public Double getOpenOverUnderAmount() {
        return OpenOverUnderAmount;
    }

    public void setOpenOverUnderAmount(Double OpenOverUnderAmount) {
        this.OpenOverUnderAmount = OpenOverUnderAmount;
    }

    public String getCloserName() {
        return CloserName;
    }

    public void setCloserName(String CloserName) {
        this.CloserName = CloserName;
    }

    public Date getCloseDateTime() {
        return CloseDateTime;
    }

    public void setCloseDateTime(Date CloseDateTime) {
        this.CloseDateTime = CloseDateTime;
    }

    public Double getCloseCashExpectedAmount() {
        return CloseCashExpectedAmount;
    }

    public void setCloseCashExpectedAmount(Double CloseCashExpectedAmount) {
        this.CloseCashExpectedAmount = CloseCashExpectedAmount;
    }

    public Double getCloseCashAmount() {
        return CloseCashAmount;
    }

    public void setCloseCashAmount(Double CloseCashAmount) {
        this.CloseCashAmount = CloseCashAmount;
    }

    public Double getCloseOverUnderAmount() {
        return CloseOverUnderAmount;
    }

    public void setCloseOverUnderAmount(Double CloseOverUnderAmount) {
        this.CloseOverUnderAmount = CloseOverUnderAmount;
    }

    public Double getCashLeftInDrawerAmount() {
        return CashLeftInDrawerAmount;
    }

    public void setCashLeftInDrawerAmount(Double CashLeftInDrawerAmount) {
        this.CashLeftInDrawerAmount = CashLeftInDrawerAmount;
    }

    public String getCashAvailableToDepositAmount() {
        return CashAvailableToDepositAmount;
    }

    public void setCashAvailableToDepositAmount(String CashAvailableToDepositAmount) {
        this.CashAvailableToDepositAmount = CashAvailableToDepositAmount;
    }

    public Boolean getCloseOverUnderMessaged() {
        return CloseOverUnderMessaged;
    }

    public void setCloseOverUnderMessaged(Boolean CloseOverUnderMessaged) {
        this.CloseOverUnderMessaged = CloseOverUnderMessaged;
    }

    public Boolean getOpenOverUnderMessaged() {
        return OpenOverUnderMessaged;
    }

    public void setOpenOverUnderMessaged(Boolean OpenOverUnderMessaged) {
        this.OpenOverUnderMessaged = OpenOverUnderMessaged;
    }

    public String getGUIDString() {
        return GUIDString;
    }

    public void setGUIDString(String GUIDString) {
        this.GUIDString = GUIDString;
    }

    public String getLastPOSTransactionID() {
        return LastPOSTransactionID;
    }

    public void setLastPOSTransactionID(String LastPOSTransactionID) {
        this.LastPOSTransactionID = LastPOSTransactionID;
    }

}

