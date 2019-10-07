package br.com.mimoapp.mimo.model;

/**
 * Created by rafael on 20/10/17.
 */

public class Contact {

    private String id;
    private String name;
    private String phone;

    public Contact () {

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
}

