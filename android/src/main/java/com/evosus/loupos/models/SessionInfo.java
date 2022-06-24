package com.evosus.loupos.models;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SessionInfo extends RealmObject {
    private String Username;
    @PrimaryKey
    private String Email;
    private String CompanySerialNumber;
    private String CompanyName;
    private String PosStationName;
    private String PosStationId;

    public String getUsername() {
        return Username;
    }
    
    public void setUsername(String Username) {
        this.Username = Username;
    }
    
    public String getEmail() {
        return Email;
    }
    
    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getCompanySerialNumber() {
        return CompanySerialNumber;
    }

    public void setCompanySerialNumber(String CompanySerialNumber) {
        this.CompanySerialNumber = CompanySerialNumber;
    }

    public String getCompanyName() {
        return CompanyName;
    }
    
    public void setCompanyName(String CompanyName) {
        this.CompanyName = CompanyName;
    }

    public String getPosStationName() {
        return PosStationName;
    }
    
    public void setPosStationName(String PosStationName) {
        this.PosStationName = PosStationName;
    }

    public String getPosStationId() {
        return PosStationId;
    }
    
    public void setPosStationId(String PosStationId) {
        this.PosStationId = PosStationId;
    }
}
