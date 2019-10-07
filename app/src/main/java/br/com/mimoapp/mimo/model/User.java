package br.com.mimoapp.mimo.model;

/**
 * Created by rafael on 16/10/17.
 */

public class User {

    private String id;
    private String FcmToken;
    private String name;
    private String phone;
    private String latitude;
    private String longitude;

    public User () {

    }

    public String getFcmToken() {
        return FcmToken;
    }

    public void setFcmToken(String FCMToken) {
        this.FcmToken = FCMToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
