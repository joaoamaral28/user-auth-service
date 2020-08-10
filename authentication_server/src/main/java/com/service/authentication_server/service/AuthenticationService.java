package com.service.authentication_server.service;

import com.service.authentication_server.exception.GenericException.GenericException;
import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
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
import java.util.Optional;

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

        // validate received email syntax
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

    public Mono<Object> updateUser(Mono<UserData> userData){
        log.info("AuthenticationService::updateUser");

        // TODO error handling
        return userData.flatMap( data -> {
            System.out.println(data);
            return userRepository.findById(data.getId())
                    .flatMap( user1 -> {
                        System.out.println(user1);
                        Optional<Map<String, String>> optionalMap = validateAndCheckNewData(data);
                        if(optionalMap.isEmpty()){
                            return Mono.just("No fields to update");
                        }else {
                            Map<String,String> m = optionalMap.get();
                            if (m.containsKey("email")) user1.setEmail(m.get("email"));
                            if (m.containsKey("name")) user1.setName(m.get("name"));
                            if (m.containsKey("password")) {
                                byte[] passwordHash = new byte[0];
                                try {
                                    passwordHash = generatePBKDF2(m.get("password"), user1.getCryptoSalt());
                                } catch (GenericException e) {
                                    if (log.isDebugEnabled())
                                        e.printStackTrace();
                                    return Mono.error(e);
                                }
                                user1.setPassword(passwordHash);
                            }
                            return userRepository.save(user1);
                        }
                    }).map(updatedUser -> {
                        System.out.println(updatedUser.toString());
                        return updatedUser.toString();
                    });
        });
    }

    // check and validate user data
    public Optional<Map<String,String>> validateAndCheckNewData(UserData userData){
        Map<String, String> map = new HashMap<>();

        String newName = userData.getName();
        String newEmail = userData.getEmail();
        String newPassword = userData.getPassword();

        // check if name to be updated
        if(newName != null && !newName.isEmpty()) {
            // check if name is an appropriate string
            if (!regexValidateString(newName, VALID_NAME_REGEX)) {
                log.warn("Name field " + newName + " failed regex validation");
                return Optional.empty(); // TODO error handling
                //return Mono.error(GenericException.throwException(USER, INVALID_NAME_EXCEPTION, "Name format is invalid"));
            }
            log.info("Name to be updated");
            map.put("name", newName);

        // check if email to be updated
        }if(newEmail != null && !newEmail.isEmpty()) {
            // check if email is an appropriate string
            if (!regexValidateString(newEmail, VALID_EMAIL_ADDRESS_REGEX)) {
                log.warn("Email field " + newEmail + " failed regex validation");
                return Optional.empty();
                //return Mono.error(GenericException.throwException(USER, INVALID_EMAIL_EXCEPTION, "Email format is invalid"));
            }
            log.info("Email to be updated");
            map.put("email", newEmail);

        // check if password to be updated
        }if(newPassword != null && !newPassword.isEmpty()){
            log.info("Password to be updated");
            map.put("password", newPassword);
        }

        return Optional.of(map);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    public Flux<User> getAllUsers(){
        log.info("AuthenticationService::getAllUsers");

        return userRepository.findAll().map( p -> {
            User user = new User(p.getName(), p.getEmail(), p.getPassword(), p.getCryptoSalt());
            user.setId(p.getId());
            user.setUserState(p.getUserState());
            return user;
        });

    }

    //@PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteUser(Integer id){
        return userRepository.deleteById(id);
    }

}
