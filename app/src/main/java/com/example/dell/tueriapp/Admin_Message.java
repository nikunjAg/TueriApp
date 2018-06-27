package com.example.dell.tueriapp;

public class Admin_Message {

    public String admin, adminMessage;

    public Admin_Message(){}

    public Admin_Message(String admin, String adminMessage) {
        this.admin = admin;
        this.adminMessage = adminMessage;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }
}
