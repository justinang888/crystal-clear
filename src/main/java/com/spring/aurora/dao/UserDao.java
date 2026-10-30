package com.spring.aurora.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.spring.aurora.model.User;

public interface UserDao extends CrudRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);
}