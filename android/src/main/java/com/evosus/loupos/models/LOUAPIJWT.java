package com.evosus.loupos.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LOUAPIJWT extends RealmObject {
     @PrimaryKey
     private String Key;
     private String Token;

     public String getKey() {
          return Key;
     }

     public void setKey(String key) {
          Key = key;
     }

     public String getToken() {
          return Token;
     }

     public void setToken(String token) {
          Token = token;
     }
}
