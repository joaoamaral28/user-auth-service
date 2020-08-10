package com.service.authentication_server.security;
/*
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;


@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration{

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange().anyExchange().permitAll();
        http.csrf().disable();
        http.cors().disable();
        return http.build();
    }

    @Bean
    public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity http) {
        http.csrf().disable();
        http.cors().disable();
        return http.authorizeExchange().anyExchange().permitAll().and().build();
    }

/*
    @Bean
    public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity http) {
        http.csrf().disable();
        return http.authorizeExchange()
                 .anyExchange().authenticated()
                .and().build();

        return http.authorizeExchange()
                .pathMatchers("/","/admin")
                .hasAuthority("ROLE_ADMIN")
                .matchers(EndpointRequest.to(FeaturesEndpoint.class))
                .permitAll()
                .anyExchange()
                .and()
                .formLogin()
                .and()
                .csrf()
                .disable()
                .build();


    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User
                .withUsername("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User
                .withUsername("admin")
                .password(passwordEncoder().encode("password"))
                .roles("ADMIN")
                .build();

        return new MapReactiveUserDetailsService(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
*/

