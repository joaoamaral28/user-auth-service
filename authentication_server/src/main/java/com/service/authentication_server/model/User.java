package com.service.authentication_server.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Arrays;

@Table("user")
public class User {

    @PrimaryKey("user_id")
    private Integer id;

    private String name;
    private String email;
    private byte[] encryptedPassword;
    private byte[] cryptoSalt;
    private String device2faAddress;

    private UserState userState;

    public User(String email, byte[] encryptedPassword){
        this.email = email;
        this.encryptedPassword = encryptedPassword;
    }

    public User(String name, String email, byte[] encryptedPassword, byte[] cryptoSalt){
        this.name = name;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.cryptoSalt = cryptoSalt;
        this.userState = UserState.ACTIVE;
    }

    public User(){

    }

    public Integer getId(){
        return this.id;
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

    public byte[] getCryptoSalt(){
        return this.cryptoSalt;
    }

    public UserState getUserState(){
        return this.userState;
    }

    public void setId(Integer id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(byte[] password){
        this.encryptedPassword = password;
    }

    public void setSalt(byte[] salt){
        this.cryptoSalt = salt;
    }

    public void setUserState(UserState userState){
        this.userState = userState;
    }

    public String toStringSimple(){
        final StringBuilder builder = new StringBuilder();
        builder.append("User [")
                .append("name=").append(this.name)
                .append(", email=").append(this.email)
                .append(", state=").append(this.userState)
                .append("]");
        return builder.toString();
    }

    @Override
    public String toString(){
        final StringBuilder builder = new StringBuilder();
        builder.append("User [")
                .append("id=").append(this.id)
                .append(", name=").append(this.name)
                .append(", email=").append(this.email)
                .append(", password=").append(Arrays.toString(this.encryptedPassword))
                .append(", salt=").append(Arrays.toString(this.cryptoSalt))
                .append("]");
        return builder.toString();
    }

}
