package com.service.authentication_server.service;

import com.service.authentication_server.exception.GenericException.GenericException;
import com.service.authentication_server.exception.GenericException.GenericExceptionEntityType;
import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
//import com.service.authentication_server.repository.UserRepository;
import com.service.authentication_server.model.UserState;
import com.service.authentication_server.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

import static com.service.authentication_server.exception.GenericException.GenericExceptionEntityType.REPOSITORY;
import static com.service.authentication_server.exception.GenericException.GenericExceptionEntityType.USER;
import static com.service.authentication_server.exception.GenericException.GenericExceptionType.*;
import static com.service.authentication_server.utils.CryptoUtils.*;
import static com.service.authentication_server.utils.RegexUtils.regexValidateString;
import static com.service.authentication_server.utils.RegexUtils.VALID_EMAIL_ADDRESS_REGEX;
import static com.service.authentication_server.utils.RegexUtils.VALID_NAME_REGEX;

@Log4j2
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> authenticateUser(UserData userData){
        log.info("AuthenticationService::authenticateUser");

        String email = userData.getEmail();
        String password = userData.getPassword();

        // validate received email
        if(!regexValidateString(email,VALID_EMAIL_ADDRESS_REGEX)){
            log.info("Provided email <" + email + "> composition is invalid");
            return Mono.error(GenericException.throwException(USER,INVALID_EMAIL_EXCEPTION,"Email format is invalid"));
            //throw new GenericException("User data is invalid. Regex validation failed!");
        }

        Mono<User> storedUser = userRepository.getUserByEmail(email);
        // proceed once IO call finishes
        storedUser.subscribeOn(Schedulers.elastic());

        // TODO return authentication JWT

        return storedUser
                // check if given password matches stored password
                .filter(user ->
                        checkPasswordMatch(password, user.getPassword(), user.getCryptoSalt()))
                .switchIfEmpty(Mono.error(GenericException.throwException(USER,INVALID_PASSWORD_EXCEPTION,"Password is incorrect")))
                // checks if account is active
                .filter(user ->
                        user.getUserState() == UserState.ACTIVE
                ).switchIfEmpty(Mono.error(GenericException.throwException(USER,INVALID_ACCOUNT_STATE_EXCEPTION,"Account is not active")))
                .doOnSuccess(value -> log.info("Authentication OK for user: " + value.toStringSimple()));

    }

    public Mono<User> createUser(UserData userData){
        log.info("AuthenticationService::createUser");

        String name = userData.getName();
        String email = userData.getEmail();
        String password = userData.getPassword();

        /* Check for invalid email and name fields by matching to the corresponding
         * regular expression. This validation is usually enforced by the client,
         * however it is double checked here for resilience purposes. */
        if(!regexValidateString(email,VALID_EMAIL_ADDRESS_REGEX)){
            log.warn("Email field " + email + " failed regex validation");
            return Mono.error(GenericException.throwException(USER,INVALID_EMAIL_EXCEPTION,"Email format is invalid"));
        }
        if(!regexValidateString(name,VALID_NAME_REGEX)){
            log.warn("Name field " + name + " failed regex validation");
            return Mono.error(GenericException.throwException(USER,INVALID_NAME_EXCEPTION,"Name format is invalid"));
        }

        // check if there is no account already registered with given email address in database
        //Mono<Boolean> existingUser = userRepository.existsByEmail(email);
        //existingUser.subscribe();
        userRepository.existsByEmail(email).map( value ->
                value ? Mono.error(GenericException.throwException(USER,EXISTING_ACCOUNT_EXCEPTION,"An account with email " + email + " already exists")) : false);

        // Start the password hashing process necessary for its secure storage in the database
        byte[] salt = generateSalt();
        byte[] passwordHash;
        try {
            passwordHash = generatePBKDF2(password, salt);
        } catch (GenericException e) {
            if(log.isDebugEnabled())
                e.printStackTrace();
            return Mono.error(e);
        }

        User user = new User(name, email, passwordHash, salt);

        return userRepository.save(user)
                .doOnSuccess(value -> log.info("Added new user: " + value.toStringSimple()))
                .switchIfEmpty(Mono.error(GenericException.throwException(REPOSITORY, REPOSITORY_STORE_EXCEPTION,  "Failed while storing newly created user into database")));
    }

    public Mono<String> updateUser(Mono<UserData> userData){
        log.info("AuthenticationService::updateUser");

        // TODO get this to work!
        return userData.flatMap( data -> {

            return userRepository.findById(data.getId())
                    .flatMap( user1 -> {
                        Map<String, String> m = checkNewUserData(data);
                        m.forEach(((k,v) -> {
                            switch (k){
                                case "email":
                                    user1.setEmail(v);
                                    break;
                                case "name" :
                                    user1.setName(v);
                                    break;
                                case "password" :
                                    byte[] passwordHash = new byte[0];
                                    try {
                                        passwordHash = generatePBKDF2(v, user1.getCryptoSalt());
                                    } catch (GenericException e) {
                                        if(log.isDebugEnabled())
                                            e.printStackTrace();
                                        // throw  Mono.error(e); TODO: look for a way to propagate the error
                                    }

                                    user1.setPassword(passwordHash);
                                    break;
                            }
                        }));
                        return userRepository.save(user1);
                    }).map(User::toStringSimple);
        });
    }

    // check and validate user data
    public Map<String,String> checkNewUserData(UserData userData){
        Map<String, String> map = new HashMap<>();

        String newName = userData.getName();
        String newEmail = userData.getEmail();
        String newPassword = userData.getPassword();

        // check if new name is present
        if(newName != null && !newName.isEmpty()) {
            // check if name is an appropriate string
            if (!regexValidateString(newName, VALID_NAME_REGEX)) {
                log.warn("Name field " + newName + " failed regex validation");
                return null; // TODO fix return on error
                //return Mono.error(GenericException.throwException(USER, INVALID_NAME_EXCEPTION, "Name format is invalid"));
            }
            log.info("Name to be updated");
            map.put("name", newName);

        // check if email to be updated
        }else if(newEmail != null && !newEmail.isEmpty()) {
            // check if email is an appropriate string
            if (!regexValidateString(newEmail, VALID_EMAIL_ADDRESS_REGEX)) {
                log.warn("Email field " + newEmail + " failed regex validation");
                return null;
                //return Mono.error(GenericException.throwException(USER, INVALID_EMAIL_EXCEPTION, "Email format is invalid"));
            }
            log.info("Email to be updated");
            map.put("email", newEmail);

        // check if password to be updated
        }else if(newPassword != null && !newPassword.isEmpty()){
            log.info("Password to be updated");
            map.put("password", newPassword);
        }else{
            log.info("No fields to update");
            return null;
            //return Mono.empty();
        }

        return map;
    }


    //@PreAuthorize("hasRole('ADMIN')")
    /*
    public Mono<User> getAllUsers(){
        log.info("AuthenticationService::getAllUsers");

        return userRepository.findAll().map( p -> {
            User user = new User(p.getUsername(), p.getEmail(), p.getPassword());
            return user;
        });

    }

    //@PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteUser(UserData userData){
        return userRepository.deleteByEmail(userData.getEmail());
    }
    */

    /*
    //PreAuthorize('hasRole('ADMIN')')
    public Mono<Void> changeUserStatus(UserStatus userStatus){
        return ...;
    }
     */
}
