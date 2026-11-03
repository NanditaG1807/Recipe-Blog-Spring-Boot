package com.main.recipeblog.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.main.recipeblog.entity.User;

public interface UserRepository
        extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
