package br.com.mimoapp.mimo.model;

/**
 * Created by rafael on 22/10/17.
 */

public class Message {

    private String message;
    private String phone;

    public Message () {

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
