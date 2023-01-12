package it.stazionidesal.desal.api.services.users;

import com.google.gson.annotations.SerializedName;

public class UserCredentials {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("fullName")
    private String fullName;

    public UserCredentials() {
    }

    public UserCredentials(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }
}
