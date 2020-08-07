package com.service.authentication_server.service;

import com.service.authentication_server.exception.GenericException;
import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
import com.service.authentication_server.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.service.authentication_server.utils.CryptoUtils.generatePBKDF2;
import static com.service.authentication_server.utils.CryptoUtils.generateSalt;
import static com.service.authentication_server.utils.RegexUtils.regexValidateString;
import static com.service.authentication_server.utils.RegexUtils.VALID_EMAIL_ADDRESS_REGEX;
import static com.service.authentication_server.utils.RegexUtils.VALID_NAME_REGEX;

@Log4j2
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> authenticateUser(UserData userData){
        System.out.println("Service::authenticateUser()");
        System.out.println(userData);

        String email = userData.getEmail();

        /*
         *  Check for invalid string user fields by matching to the corresponding
         *  regular expression. This validation is usually enforced by the client,
         *  however it is double checked for resilience purposes.
         */
        if(!regexValidateString(email,VALID_EMAIL_ADDRESS_REGEX)){
            //throw new JSONException("User data is invalid. Regex validation failed!");
        }

        return userRepository.getUserByEmail(email);
    }

    public Mono<User> createUser(UserData userData){

        String name = userData.getName();
        String email = userData.getEmail();
        String password = userData.getPassword();

        /*
         *  Check for invalid string user fields by matching to the corresponding
         *  regular expression. This validation is usually enforced by the client,
         *  however it is double checked for resilience purposes.
         */
        if(!(regexValidateString(email,VALID_EMAIL_ADDRESS_REGEX) || regexValidateString(name,VALID_NAME_REGEX))){
            //throw new JSONException("User data is invalid. Regex validation failed!");
        }

        /* Start the password hashing process necessary for its secure storage in the database */
        byte[] salt = generateSalt();
        byte[] passwordHash = new byte[0];
        try {
            passwordHash = generatePBKDF2(password, salt);
        } catch (GenericException e) {
            e.printStackTrace();
        }

        User user = new User(name, email, passwordHash, salt);

        Mono<User> ret;

        try{
            ret = userRepository.save(user);
        }catch (Exception e){
            if(log.isDebugEnabled()){
                e.printStackTrace();
            }
            return null;
            //throw GenericException.throwException(REPOSITORY, REPOSITORY_STORE_EXCEPTION,  "Failed while storing newly created user into local database");
        }

        log.info("Added User: " + user);

        return ret;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Flux<User> getAllUsers(){
        /*
        return userRepository.findAll().map( p -> {
            User user = new User(p.getUsername(), p.getEmail(), p.getPassword());
            return user;
        })
         */


        return null;
    }

}
