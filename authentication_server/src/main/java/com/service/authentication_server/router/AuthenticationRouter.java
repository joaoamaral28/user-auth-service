package com.service.authentication_server.router;

import com.service.authentication_server.handler.AuthenticationHandler;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AuthenticationRouter {

    /**
     * The router configuration for the authentication handler
     * @param authenticationHandler
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> authenticationRoute(AuthenticationHandler authenticationHandler){
        return RouterFunctions
                .route(POST("/user/login").and(accept(MediaType.APPLICATION_JSON)), authenticationHandler::authenticateUser)
                .andRoute(POST("user/register").and(accept(MediaType.APPLICATION_JSON)), authenticationHandler::createUser)
                .andRoute(GET("/user/getAll"), authenticationHandler::getAllUsers)
                .andRoute(DELETE("/user/delete").and(accept(MediaType.APPLICATION_JSON)), authenticationHandler::deleteUser)
                .andRoute(PUT("/user/update").and(accept(MediaType.APPLICATION_JSON)), authenticationHandler::updateUser);

        /*
                .route()
                .nest(path("/user"), bc -> bc
                        .POST("/login", authenticationHandler::authenticateUser)
                        .before(request -> {
                            System.out.printf("Request={%s} received.\n", request.toString());
                            System.out.println(request.bodyToMono(JSONObject.class).toProcessor().peek());
                            return request;
                        })
                        .after((request, response) -> {
                            System.out.printf("Response={%s} sent.\n", response.toString());
                            return response;
                        })).build();
        */

    }

}
