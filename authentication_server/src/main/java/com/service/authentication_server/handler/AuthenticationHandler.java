package com.service.authentication_server.handler;

import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
import com.service.authentication_server.service.AuthenticationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Log4j2
@Component
public class AuthenticationHandler {

    @Autowired
    private AuthenticationService authenticationService;

    public Mono<ServerResponse> authenticateUser(ServerRequest serverRequest){
        log.info("Handler::authenticateUser");

        Mono<UserData> userData = serverRequest.bodyToMono(UserData.class);

        return userData.flatMap( data -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authenticationService.authenticateUser(data), User.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createUser(ServerRequest serverRequest){
        log.info("Handler::createUser");

        Mono<UserData> userData = serverRequest.bodyToMono(UserData.class);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(authenticationService.createUser(userData), Object.class);
        /*
        return userData.flatMap( data -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authenticationService.createUser(userData), Object.class));

         */
    }

    public Mono<ServerResponse> updateUser(ServerRequest serverRequest){
        log.info("Handler::updateUser");
        return authenticationService.updateUser(serverRequest.bodyToMono(UserData.class))
                .flatMap( data -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(data)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest serverRequest){
        log.info("Handler::getAllUsers");

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authenticationService.getAllUsers(), User.class);
    }

    public Mono<ServerResponse> deleteUser(ServerRequest serverRequest){
        log.info("Handler::deleteUser");
        log.info("Query params: " + serverRequest.queryParams());

        Optional<String> id = serverRequest.queryParam("id");

        if(id.isEmpty()){
            return ServerResponse.badRequest().body("Invalid request", String.class);
        }

        Mono<Void> deletedUser = authenticationService.deleteUser(Integer.parseInt(id.get()));

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(deletedUser, Void.class);
    }

}
