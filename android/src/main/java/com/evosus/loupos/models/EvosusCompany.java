package com.evosus.loupos.models;

import java.math.BigDecimal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EvosusCompany extends RealmObject {

    @PrimaryKey
    private String SerialNumber;

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String SKUType) {
        this.SerialNumber = SerialNumber;
    }

}
