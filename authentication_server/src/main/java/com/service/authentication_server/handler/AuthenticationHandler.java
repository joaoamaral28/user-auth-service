package com.service.authentication_server.handler;

import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
import com.service.authentication_server.service.AuthenticationService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationHandler {

    @Autowired
    private AuthenticationService authenticationService;

    public Mono<ServerResponse> authenticateUser(ServerRequest serverRequest){
        System.out.println("Handler::authenticateUser()");

        System.out.println(serverRequest.bodyToMono(JSONObject.class).toProcessor().peek());

        //String email = serverRequest.pathVariable("email");
        //String password = serverRequest.pathVariable("password");

        UserData userData = new UserData();

        Mono<User> authenticateUser = authenticationService.authenticateUser(userData);

        return ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(authenticateUser, UserData.class);
    }

    public Mono<ServerResponse> createUser(ServerRequest serverRequest){
        System.out.println("createUser()");
        Mono<UserData> decodedUserData = serverRequest.bodyToMono(UserData.class);

        Mono<User> user = authenticationService.createUser(null);

        return decodedUserData.flatMap( data ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(user, UserData.class));

    }


}
