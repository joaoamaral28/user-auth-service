package com.service.authentication_server.service;

import com.service.authentication_server.exception.GenericException.GenericException;
import com.service.authentication_server.handler.AuthenticationHandler;
import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
//import com.service.authentication_server.repository.UserRepository;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.service.authentication_server.utils.CryptoUtils.generatePBKDF2;
import static com.service.authentication_server.utils.CryptoUtils.generateSalt;

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
        UserData userData = new UserData("João", "joaoamaral@gmail.com", "password123");

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

        String credentials = "{ \"name\": \"João\", \"email\":\"joaoamaral@gmail.com\", \"password\":\"password123\" }";

        webTestClient.put()
                .uri("/user/update")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(BodyInserters.fromValue(credentials))
                .exchange()
                .expectStatus()
                .isOk();
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
        webTestClient.delete()
                .uri("/user/delete?id=1")
                .exchange()
                .expectStatus()
                .isOk();
    }

}