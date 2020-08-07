package com.service.authentication_server.model;

import java.io.Serializable;

public class UserData implements Serializable {

    private String name;
    private String email;
    private String password;

    public UserData(String email, String password){
        this.email = email;
        this.password = password;
    }

    public UserData(String name, String email, String password){
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public UserData(){

    }

    public String getName(){
        return this.name;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }


    @Override
    public String toString(){
        final StringBuilder builder = new StringBuilder();
        builder.append("UserData [")
                .append("name=").append(this.name)
                .append(", email=").append(this.email)
                .append(", password=").append(this.password)
                .append("]");
        return builder.toString();
    }


}
