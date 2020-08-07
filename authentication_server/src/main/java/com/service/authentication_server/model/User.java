package com.service.authentication_server.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Arrays;

@Table("User")
public class User {

    @PrimaryKey("user_id")
    private Integer id;

    private String name;
    private String email;
    private byte[] encryptedPassword;
    private byte[] cryptoSalt;

    public User(String email, byte[] encryptedPassword){
        this.email = email;
        this.encryptedPassword = encryptedPassword;
    }

    public User(String name, String email, byte[] encryptedPassword, byte[] cryptoSalt){
        this.name = name;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
    }

    public User(){

    }

    public String getName(){
        return this.name;
    }

    public String getEmail(){
        return this.email;
    }

    public byte[] getPassword(){
        return this.encryptedPassword;
    }


    @Override
    public String toString(){
        final StringBuilder builder = new StringBuilder();
        builder.append("UserData [")
                .append("name=").append(this.name)
                .append(", email=").append(this.email)
                .append(", password=").append(Arrays.toString(this.encryptedPassword))
                .append(", salt=").append(Arrays.toString(this.cryptoSalt))
                .append("]");
        return builder.toString();
    }

}
