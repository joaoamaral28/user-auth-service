package com.service.authentication_server.repository;


import com.service.authentication_server.model.User;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCassandraRepository<User, Integer> {

    Mono<User> getUserByEmail(String email);

    Mono<Void> deleteByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

}

