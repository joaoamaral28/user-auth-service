package com.service.authentication_server.service;

import com.service.authentication_server.exception.GenericException.GenericException;
import com.service.authentication_server.handler.AuthenticationHandler;
import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
//import com.service.authentication_server.repository.UserRepository;
import com.service.authentication_server.model.UserState;
import com.service.authentication_server.repository.UserRepository;
import com.service.authentication_server.router.AuthenticationRouter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static com.service.authentication_server.exception.GenericException.GenericExceptionEntityType.USER;
import static com.service.authentication_server.exception.GenericException.GenericExceptionType.INVALID_ACCOUNT_STATE_EXCEPTION;
import static com.service.authentication_server.exception.GenericException.GenericExceptionType.INVALID_PASSWORD_EXCEPTION;
import static com.service.authentication_server.utils.CryptoUtils.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebFluxTest
@Import({AuthenticationService.class, AuthenticationHandler.class, AuthenticationRouter.class})
class AuthenticationServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private WebTestClient webTestClient;

    private static User user;

    @BeforeAll
    public static void init(){
        UserData userData = new UserData(1,"João", "joaoamaral@gmail.com", "password123");

        String name = userData.getName();
        String email = userData.getEmail();
        String password = userData.getPassword();

        byte[] salt = generateSalt();
        byte[] passwordHash = new byte[0];
        try {
            passwordHash = generatePBKDF2(password, salt);
        } catch (GenericException e) {
            e.printStackTrace();
        }

        user = new User(name, email, passwordHash, salt);
        user.setId(userData.getId());

    }

    @BeforeEach
    public void setup(){
        BDDMockito.when(userRepository.findAll()).thenReturn(Flux.just(user));
        BDDMockito.when(userRepository.save(user)).thenReturn(Mono.just(user));
        BDDMockito.when(userRepository.getUserByEmail(user.getEmail())).thenReturn(Mono.just(user));
        BDDMockito.when(userRepository.deleteByEmail(user.getEmail())).thenReturn(Mono.empty());
        BDDMockito.when(userRepository.existsByEmail(user.getEmail())).thenReturn(Mono.just(Boolean.TRUE));
    }

    @Test
    public void testAuthenticateUserValidCredentials(){

        String credentials = "{ \"email\":\"joaoamaral@gmail.com\", \"password\":\"password123\" }";

        webTestClient.post()
                .uri("/user/login")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(BodyInserters.fromValue(credentials))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testCreateUserValidCredentials(){

        BDDMockito.when(userRepository.existsByEmail(BDDMockito.anyString())).thenReturn(Mono.just(Boolean.FALSE));
        BDDMockito.when(userRepository.save(BDDMockito.any(User.class))).thenReturn(Mono.just(user));

        String credentials = "{ \"name\": \"João\", \"email\":\"joaoamaral@gmail.com\", \"password\":\"password123\" }";

        webTestClient.post()
                .uri("/user/register")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(BodyInserters.fromValue(credentials))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testUpdateUser(){

        BDDMockito.when(userRepository.findById(user.getId())).thenReturn(Mono.just(user));

        String update_all = "{ \"id\": 1, \"name\": \"João\", \"email\":\"jjjj@gmail.com\", \"password\":\"password123\" }";
        String update_email = "{ \"id\": 1, \"email\":\"jjjj@gmail.com\" }";


        EntityExchangeResult<String> result =  webTestClient.put()
                .uri("/user/update")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(BodyInserters.fromValue(update_email))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult();

        System.out.println(result.getResponseBody());

    }

    @Test
    public void testGetAllUsers(){
        webTestClient.get()
                .uri("/user/getAll")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testDeleteUser(){

        BDDMockito.when(userRepository.deleteById(user.getId())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/delete")
                        .queryParam("id", "1")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void testMonoError(){

        UserData wrongUserData = new UserData(1,"João", "joaoamaral@gmail.com", "password");

        String name = wrongUserData.getName();
        String email = wrongUserData.getEmail();
        String password = wrongUserData.getPassword();

        byte[] salt = generateSalt();
        byte[] passwordHash = new byte[0];
        try {
            passwordHash = generatePBKDF2(password, salt);
        } catch (GenericException e) {
            e.printStackTrace();
        }

        User wrongUser = new User(name, email, passwordHash, salt);
        wrongUser.setId(wrongUserData.getId());

        System.out.println(user);
        System.out.println(wrongUser);

        Mono<String> ret = Mono.just(wrongUser)
                .map(user ->
                    //if(!checkPasswordMatch(Arrays.toString(user.getPassword()), wrongUser.getPassword(), wrongUser.getCryptoSalt())){
                    //    System.out.println("Wrong password");
                    //    return Mono.error(GenericException.throwException(USER,INVALID_PASSWORD_EXCEPTION,"Password is incorrect"));
                    //}if(user.getUserState() != UserState.ACTIVE)
                    //    return Mono.error(GenericException.throwException(USER,INVALID_ACCOUNT_STATE_EXCEPTION,"Account is not active"));
                    "OK"
                ).log();

        StepVerifier.create(ret)
                .expectNext("OK")
                //.expectErrorMessage("Password is incorrect")
                //.expectError(GenericException.class)
                .verifyComplete();

    }

}